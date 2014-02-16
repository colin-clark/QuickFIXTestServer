/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.node;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.cassandra.service.EmbeddedCassandraService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;

import com.cep.commons.CassandraHelper;
import com.cep.commons.EventObject;
import com.cep.darkstar.adv.IAdvHandler;
import com.cep.darkstar.node.configuration.YamlConfigInfo;
import com.cep.darkstar.node.configuration.YamlConfiguration;
import com.cep.darkstar.node.configuration.version.DarkStarVersion;
import com.cep.darkstar.pubsub.sub.SubscribeTopic;
import com.cep.darkstar.query.ICEPEngine;
import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.node.GossipService;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.interfaces.IMessagingService;
import com.espertech.esper.client.EPException;
import com.rabbitmq.client.ShutdownSignalException;

import flex.messaging.util.UUIDUtils;

/**
 * @author colin
 * 
 */
public class DarkStarNode {
	final static Logger dsLog = Logger.getLogger("com.cep.darkstar.node");
	private static String supervisorHost;
	private static int listenPort;
	private static String listenTopic;
	private static String listenExchange = "amq.topic";
	private static String gatePath;
	// holds active queries
	private static Map<String, QueryInt> queryMap = new HashMap<String, QueryInt>();
	private static CEPEngine cepEngine; // our engine
	private static IMessagingService messagingService  = GossipService.instance();	
	private static final DarkStarVersion version = new DarkStarVersion();
	
	private static EmbeddedCassandraService cassandraService;
	private static IAdvHandler advHandler;
	// ugh - let's clap our hands, ride the shortbus, and build an app...
	private static boolean cluster_surveillance = false;
	private static String HASupervisorHost;
	private static boolean external_timing = false;
	
	private static SubscribeTopic listenFor = null;
	private static SubscribeTopic HAlistenFor = null;
	
	public static String getGatePath() {
		return gatePath;
	}

	// dump active queries
	public static void dumpQueryMap(String nodeID) {
		Logger dsLog = Logger.getLogger("com.cep.darkstar.node");
		dsLog.info("Active Queries on node:" + nodeID);
		for (String aKey : queryMap.keySet()) {
			dsLog.info("Query Name:" + aKey + ": Query ID:"
					+ queryMap.get(aKey).getQueryID() + ":"
					+ queryMap.get(aKey).getQuery());
		}
	}

	// submit query to engine for execution
	public static String submitRemoteQuery(String queryID, String telescopeQuery, String nodeID, EventObject queryEvent) 
			throws JSONException, IOException {
		Query aQuery = new Query(queryID, telescopeQuery, cepEngine);
		aQuery.extractPublishingInfo(queryEvent);
		aQuery.startCEPEngine();
		if (dsLog.isDebugEnabled()) {
			dsLog.debug("Listener added for statement " + queryID);
		}
		// we've got a queryid mismatch here that needs to be cleaned up in the future
		queryMap.put(queryID, aQuery);
		return (aQuery.getQueryID());
	}

	// submit statement for execution
	private static void submitRemoteStatement(String statement) throws IOException {
		Statement aStatement = new Statement(statement, cepEngine);
		aStatement.submitStatement();
	}

	// submit a named query for execution - create name & execute
	public static String submitRemoteNamedQuery(String queryID, String telescopeQuery, EventObject queryEvent) 
			throws JSONException, IOException {
		NamedQuery aQuery = new NamedQuery(queryID, telescopeQuery, cepEngine);
		aQuery.extractPublishingInfo(queryEvent);
		aQuery.startCEPEngine();
		queryMap.put(queryID, aQuery);
		dsLog.info("Query->returning queryID");
		return (aQuery.getQueryID());
	}

	// kill active query
	public static void killQuery(String queryID) {
		QueryInt aQuery = queryMap.get(queryID);
		if (aQuery != null) {
			aQuery.kill();
			queryMap.remove(queryID);
		}
		aQuery = null;
	}

	// stop a query from running if possible
	public static void stopQuery(String queryID) {
		QueryInt aQuery = queryMap.get(queryID);
		if (aQuery != null) {
			// aQuery.stop();
		}
	}

	// start a query
	public static void startQuery(String queryID) {
		QueryInt aQuery = queryMap.get(queryID);
		if (aQuery != null) {
			// aQuery.start();
		}
	}
		
	public static String getSupervisorHostname() {
		return supervisorHost;
	}
	
	public static String getHASupervisorHostname() {
		return HASupervisorHost;
	}
	
	public static int getListenPort() {
		return listenPort;
	}

	public static String getListenTopic() {
		return listenTopic;
	}

	public static String getListenExchange() {
		return listenExchange;
	}

	public static boolean isCluster_surveillance() {
		return cluster_surveillance;
	}

	public static void setCluster_surveillance(boolean cluster_surveillance) {
		DarkStarNode.cluster_surveillance = cluster_surveillance;
	}

	// main command loop for worker node
	public static void main(String[] args) throws JSONException, InterruptedException {
		YamlConfigInfo info = null;
		try {
			URL url = NodeDescriptor.getStorageConfigURL();
			dsLog.info("Loading settings from " + url);

			InputStream input = null;
			try {
				input = url.openStream();
			} catch (IOException e) {
				throw new AssertionError(e);
			}

			info = YamlConfiguration.getConfiguration(input);
			
			// logger
			String log4j_file = (info == null ? "log4j.configuration" : info.getLog4j_file()); 
			PropertyConfigurator.configure(log4j_file);
			messagingService.setLog4jFile(log4j_file);
			
			// we store schema information in DB type decided @ runtime
			CEPEngine.constructConnectionHelper(info.getConnection_handler(), info.getConnection_handler_parameters());
			
			supervisorHost = info.getSupervisor_host();
			listenPort = info.getListen_port();
			listenTopic = info.getListen_topic();
			listenExchange = info.getListen_exchange();
			external_timing = info.isExternal_timing();
			dsLog.info("Running with external timing = " + external_timing);
			
			// THE FOLLOWING CODE NEEDS TO BE EXAMINED AND REFACTORED
			// WE HAVE CUSTOM CUSTOMER CODE IN THE CORE ENGINE
			// THIS FUNCTIONALITY NEEDS TO BE LOADED AT RUN TIME!!!!
			// CPC 03022012
			
			
			// HA for NYSE Surveillance cluster
			cluster_surveillance = info.cluster_surveillance;
			if (cluster_surveillance) {
				HASupervisorHost = info.HASupervisor_host;
				if (HASupervisorHost == null) {
					dsLog.info("No HA Supervisor defined");
				}
			}
			
			// gate path for NLP stack
			gatePath = info.getGatePath();
			
			// ADV For surveillance
			if (info.ADVHandler != null) {
				dsLog.info("Constructing ADV Handler class " + info.ADVHandler);
				try {
				    advHandler = GossipUtilities.<IAdvHandler>construct(info.ADVHandler, "AdvHandler");
				    advHandler.setParams(info.adv_oracle_url, info.adv_oracle_user, info.adv_oracle_pwd);
				    dsLog.info("ADV Handler " + info.ADVHandler + " successfully constructed");
				} catch (Exception e) {
					dsLog.error(e.getMessage(), e);
				}
			}
		} catch (Exception e1) {
			dsLog.fatal("Fatal configuration exception:\n");
			dsLog.error(e1.getMessage(), e1);
			System.exit(1);
		}
	
		// tell the world who we are
		String versionNumber = version.getVersion();
		if (versionNumber == null) {
			dsLog.info("Unable to determine version of this DarkStar Node");
		} else {
			dsLog.info("DarkStar Worker Node Version " + versionNumber);
		}
		dsLog.info("Copyright Cloud Event Processing, 2009-2012");
		
		// node id
		String nodeID = UUIDUtils.createUUID();
		dsLog.info("DarkStar Node ID:"+nodeID+" is now entering command loop:");
		
		// queue for persistent hand off for events we'd like to persist
		BlockingQueue<EventObject> queue = new ArrayBlockingQueue<EventObject>(1000);
		
		/*
		 *  Either run Cassandra internally or get a connection to the instance
		 *  specified in the yaml.
		 */
		dsLog.info("Activating storage container...");
		if (info.getEmbed_cassandra()) {
			dsLog.info("Attemtping internal connection.");
			cassandraService = new EmbeddedCassandraService();
			try {
				cassandraService.start();
			} catch (Exception e) {
				dsLog.error("Embedded Cassandra Service could not be started " +
						"due to Exception: " + e.getMessage(), e);
			}
		}
		final ConsistencyLevelPolicy mcl = new MyConsistencyLevel();
		Cluster cluster = HFactory.getOrCreateCluster(info.getPersistence_cluster(), info.getPersistence_ip()+":"+info.getPersistence_port());
		Keyspace ko = HFactory.createKeyspace(info.getPersistence_cf(), cluster, mcl);
		
		// set up an executor for persistence via shared queue
		final Executor toCassandra = Executors.newFixedThreadPool(1);
		toCassandra.execute(new PersistEvent(queue, ko, info.getPersistence_batch_size(), external_timing));
		CassandraHelper.init(cluster, ko, info.getPersistence_batch_size(), info.getPersistence_queue_size());

		/*
		 *  start up and configure CEP engine
		 */
		dsLog.info("Attempting CEP engine as daemon thread.");
		cepEngine = new CEPEngine(nodeID, info, queue);
		
		// internode messaging
		messagingService.registerCEPEngine((ICEPEngine) cepEngine);
		messagingService.start();
		
		if (info.ADVHandler != null) {
			advHandler.generateEpl(cepEngine);
			final Executor advExecutor = Executors.newFixedThreadPool(1);
			advExecutor.execute(advHandler);
		}
		
		SubscribeTopic activeSubscription = getActiveSubscription();
		if (activeSubscription == null) {
			dsLog.fatal("Unable to connect to any supervisor host, process will exit");
			System.exit(1);
		}

		// Load In Order:
		// Plugins- OffRamps may need Plugins
		// OffRamps - make sure we load, init, and run OffRamps so that we're
		// ready to do something with data before turning on the
		// OnRamps - finally load, init, and run onRamps
		executeCommandLoop(nodeID, activeSubscription);
	}

	private static void executeCommandLoop(String nodeID, SubscribeTopic activeSubscription) 
			throws JSONException, InterruptedException {
		String queryString;
		String queryID;
		final Log dsLog = LogFactory.getLog(DarkStarNode.class);
		while (activeSubscription != null) {
			try {
				while (true) {
					try {
						EventObject query = new EventObject(new String(activeSubscription.nextDelivery()));
						// new unnamed query
						if (query.getString("cmd").equalsIgnoreCase("QUERY") || query.getString("cmd").equalsIgnoreCase("NEW")) {
							dsLog.info("Received command NEW:" + query.toString());
							queryID = query.getString("NqID");
							queryString = query.getString("Nq");
							String aQueryID = submitRemoteQuery(queryID, queryString, nodeID, query);
							dsLog.info("Executing query:" + aQueryID);
							continue;
						}					
	
						// names query
						if (query.getString("cmd").equalsIgnoreCase("NAMED_QUERY")) {
							dsLog.info("Received command NEW:" + query.toString());
							queryID = query.getString("NqID");
							queryString = query.getString("Nq");
							String aQueryID = submitRemoteNamedQuery(queryID, queryString, query);
							dsLog.info("Executing query:" + aQueryID);
							continue;
						}

						// epl statement
						if (query.getString("cmd").equalsIgnoreCase("STATEMENT")) {
							// touch this later
							dsLog.info("Received command STATEMENT:" + query.toString());
							submitRemoteStatement(query.getString("Nq"));
							dsLog.info("Executing statement:" + query.toString());
							continue;
						}
	
						// stop query
						if (query.getString("cmd").equalsIgnoreCase("STOP")) {
							dsLog.info("Received command STOP:" + query.toString());
							stopQuery(query.getString("NqID"));
							continue;
						}
	
						// kill query
						if (query.getString("cmd").equalsIgnoreCase("KILL")) {
							dsLog.info("Received command KILL:" + query.toString());
							killQuery(query.getString("NqID"));
							continue;
						}

						// resume statement
						if (query.getString("cmd").equalsIgnoreCase("START")) {
							dsLog.info("Received command START:" + query.toString());
							startQuery(query.getString("NqID"));
							continue;
						}
	
						// query status
						if (query.getString("cmd").equalsIgnoreCase("QUERY_DUMP")) {
							dsLog.info("Received command QUERY_DUMP:" + query.toString());
							dumpQueryMap(nodeID);
							continue;
						}
	
						// received unknown command if we ever get here
						dsLog.warn("Received UNKNOWN command:" + query.toString());
	
					} catch (EPException e) {
						dsLog.error("Error in command loop:" + e.getStackTrace());
						dsLog.error("Re-entering command loop.");
					}
				}
			} catch (ShutdownSignalException e) {
				dsLog.error("Shutdown signal received in command loop from currently active subscription", e);
			} catch (IOException e) {
				dsLog.fatal("IO Exception received in command loop:" + e.getStackTrace());
				System.exit(1);
			}
			activeSubscription = getActiveSubscription();
		}
		dsLog.fatal("Unable to connect to RabbitMQ, process will exit");
		System.exit(1);
	}

	private static SubscribeTopic getActiveSubscription() {
		dsLog.info("Attempting connection to DarkStar supervisor at:" + supervisorHost);
		try {
			listenFor = new SubscribeTopic.Builder().hostName(supervisorHost).topic(listenTopic)
				.portNumber(listenPort).exchange(listenExchange).build();
			dsLog.info("Connected to DarkStar supervisor at:" + supervisorHost);
			return listenFor;
		} catch (IOException e) {
			dsLog.error("Unable to connect to DarkStar supervisor at:" + supervisorHost, e);
		}
		
		if (HASupervisorHost != null) {
			dsLog.info("Attempting connection to DarkStar supervisor at:" + HASupervisorHost);
			try {
				HAlistenFor = new SubscribeTopic.Builder().hostName(HASupervisorHost).topic(listenTopic)
					.portNumber(listenPort).exchange(listenExchange).build();
				dsLog.info("Connected to DarkStar supervisor at:" + HASupervisorHost);
				return HAlistenFor;
			} catch (IOException e) {
				dsLog.error("Unable to connect to DarkStar supervisor at:" + HASupervisorHost, e);
			}
		}
		return null;
	}

	public static boolean isExternal_timing() {
		return external_timing;
	}
	
}
