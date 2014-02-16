package com.cep.darkstar.onramp.arca.trf;

import static me.prettyprint.hector.api.factory.HFactory.createColumn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.OperationType;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.MutationResult;
import me.prettyprint.hector.api.mutation.Mutator;

import com.cep.commons.EventObject;
import com.cep.darkstar.onramp.configuration.arca.trf.ClientConfigInfo;
import com.cep.darkstar.onramp.configuration.arca.trf.ClientConfiguration;

public class TRFLogReader {
	
	final static Logger logger = Logger.getLogger("com.cep.darkstar.onramp.arca.trf.TRFLogReader");
	
	static char version = 'V';
	static char message = 'M';
	static String longType = "L";
	static String stringType = "S";
	private static boolean injectAll = false;
	
	static String log4j_file;
	static String darkStarNode;
	static int darkStarPort;
	static String firm;
	static String clusterName;
	static String keyspace;
	static int number_of_publishers;
	static int batch_size;
	static String eventName;

	protected static Mutator<ByteBuffer> mutator;

	protected static StringSerializer se = StringSerializer.get();
	protected static LongSerializer ls = LongSerializer.get();
	protected static ByteBufferSerializer bfs = ByteBufferSerializer.get();
	protected static BytesArraySerializer bas = BytesArraySerializer.get();

	protected static long commit = 0;
	protected EventObject event;
	protected ByteBuffer rowKey;
	protected MutationResult mr;
	protected String key;
	protected Iterator<?> iterator;
	
	public static void main(String[] args) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String str;
		String key = null;
		String value = null;
		boolean skip = false;
		
		try {
			setProperties(args[0]);
		} catch (Exception e1) {
			System.out.println("Unable to use "+args[0]+" as a valid configuration file.");
			e1.printStackTrace();
		}
		PropertyConfigurator.configure(log4j_file);
			
		logger.info("trfLogReader v0.1.0");
		// Hector stuff
		final ConsistencyLevelPolicy mcl = new MyConsistencyLevel();
		Cluster cluster;
		Keyspace ko;
		
		// initialize darkstar connection and pass keyspace around
		CassandraHostConfigurator hostConfig = 
			new CassandraHostConfigurator(darkStarNode+":"+String.valueOf(darkStarPort));
		
		//hostConfig.setAutoDiscoverHosts(true);
		cluster = HFactory.createCluster(clusterName, hostConfig);
		ko = HFactory.createKeyspace(keyspace, cluster, mcl);
		
		// and a mutator
		mutator = HFactory.createMutator(ko, bfs);
		
		logger.info("trfLogReader prepared for input");

		int count = 0;
		int bad = 0;
		try {
			while ((str = in.readLine()) != null) {
				try {
					if (str.charAt(0)==message) {
						// split on ctrl-E
						int fixBeginsAt = str.indexOf("=");
						String restOfLine = str.substring(fixBeginsAt-1);
						String[] logFields = restOfLine.split("\\u0001");
						if (logger.isDebugEnabled()) {
							logger.debug("inbound array = "+logFields.toString());
						}
						EventObject trfIn = new EventObject();
						boolean reportingObligation = false;
						String SecondaryTrdType = null;
						// the last field has data we're not interested in
						for (int i=0;i<logFields.length-1;i++) {
							// for each field, split it, look up FIX mnemonic, substitute and inject
							// it with the correct typing
							String[] aField = logFields[i].split("=");
							key = aField[0];
							value = aField[1];
							if (logger.isDebugEnabled()) {
								logger.debug("key/value pair = "+key+"/"+value);
							}
							// discard these values
							// beginstring, length, checksum, msg seq num
							if (!skip) {
								if (key.equalsIgnoreCase("8") || key.equalsIgnoreCase("9") ||
										key.equalsIgnoreCase("10") || key.equalsIgnoreCase("34")) {
									skip = true;
									continue;
								}
							}
							
							// avg price
							if (key.equalsIgnoreCase("6")) {
								trfIn.put("AvgPx", Double.parseDouble(value));
								continue;
							}
							
							// clordid
							if (key.equalsIgnoreCase("11")) {
								trfIn.put("ClOrdID", value);
								continue;
							}
							
							// clordid
							if (key.equalsIgnoreCase("14")) {
								trfIn.put("Shares", Long.parseLong(value));
								continue;
							}
							
							// exec id#
							if (key.equalsIgnoreCase("17")) {
								trfIn.put("ExecID", value);
								continue;
							}
							
							// exec trans type
							if (key.equalsIgnoreCase("20")) {
								trfIn.put("ExecTransType", value);
								continue;
							}
							
							// message type
							if (key.equalsIgnoreCase("35")) {
								trfIn.put("MsgType", value);
								continue;
							}
							
							// Order Id
							if (key.equalsIgnoreCase("37")) {
								trfIn.put("OrderID", value);
								continue;
							}
							
							// order status
							if (key.equalsIgnoreCase("39")) {
								trfIn.put("OrdStatus", value);
								continue;
							}
							
							// sender comp id
							if (key.equalsIgnoreCase("49")) {
								trfIn.put("SenderCompID", value);
								continue;
							}
							
							// sending time
							if (key.equalsIgnoreCase("52")) {
								trfIn.put("SendingTime", value);
								continue;
							}
							// side
							if (key.equalsIgnoreCase("54")) {
								trfIn.put("Side", value);
								continue;
							}
							
							// symbol
							if (key.equalsIgnoreCase("55")) {
								trfIn.put("Symbol", value);
								continue;
							}
							// target compi id
							if (key.equalsIgnoreCase("56")) {
								trfIn.put("TargetCompID", value);
								continue;
							}
							
							// transact time
							if (key.equalsIgnoreCase("60")) {
								trfIn.put("TransactTime", value);
								continue;
							}			
							
							// trade date
							if (key.equalsIgnoreCase("75")) {
								trfIn.put("TradeDate", value);
								continue;
							}	
							
							// process code
							if (key.equalsIgnoreCase("81")) {
								trfIn.put("ProcessCode", Long.parseLong(value));
								continue;
							}
							
							// exec type
							if (key.equalsIgnoreCase("150")) {
								trfIn.put("ExecType", value);
								continue;
							}
							
							// leaves qty
							if (key.equalsIgnoreCase("151")) {
								trfIn.put("LeavesQty", Long.parseLong(value));
								continue;
							}
							
							
							// order capacity
							if (key.equalsIgnoreCase("528")) {
								trfIn.put("OrderCapacity", value.toUpperCase());
								continue;
							}
	
							// side
							if (key.equalsIgnoreCase("571")) {
								trfIn.put("TradeReportID", value);
								continue;
							}
							
							// clearing instructoin
							if (key.equalsIgnoreCase("577")) {
								trfIn.put("ClearingInstruction", Long.parseLong(value));
								continue;
							}
							
							if (key.equalsIgnoreCase("700")) {
								trfIn.put("ReversalIndicator", value.toUpperCase());
								continue;
							}
							
							// trade type
							if (key.equalsIgnoreCase("828")) {
								trfIn.put("TrdType", Long.parseLong(value));
								continue;
							}	
							
							// trade sub type
							if (key.equalsIgnoreCase("829")) {
								trfIn.put("TrdSubType", Long.parseLong(value));
								continue;
							}	
							
							// publish trade indicator
							if (key.equalsIgnoreCase("852")) {
								trfIn.put("PublishTrdIndicator", value.toUpperCase());
								continue;
							}
							
							// short sale reason
							if (key.equalsIgnoreCase("853")) {
								trfIn.put("ShortSaleReason", Long.parseLong(value));
								continue;
							}
							
							if (key.equalsIgnoreCase("855")) {
								SecondaryTrdType = value;
								if (" ".equals(value)) {
									value = "9";
								}
								trfIn.put("SecondaryTrdType", value.toUpperCase());
								continue;
							}
											
							// Trade report type
							if (key.equalsIgnoreCase("856")) {
									trfIn.put("TradeReportType", Long.parseLong(value));
									continue;
							}
							
							// trade report status
							if (key.equalsIgnoreCase("939")) {
								trfIn.put("TrdRptStatus", Long.parseLong(value));
								continue;
							}
							
							// as of indicator
							if (key.equalsIgnoreCase("5080")) {
								trfIn.put("AsOfIndicator", value.toUpperCase());
								continue;
							}
							
							// trf price override
							if (key.equalsIgnoreCase("8201")) {
								logger.info("Received FIX message with 8201="+value.toUpperCase());
								trfIn.put("TRFPO", value.toUpperCase());
								continue;
							}
							
							// branch seq #
							if (key.equalsIgnoreCase("9861")) {
								trfIn.put("BranchSeqNbr", value);
								continue;
							}
							
							// contra trade
							if (key.equalsIgnoreCase("9862")) {
								trfIn.put("ContraTradePA", value);
								continue;
							}
							
							// Trade Type Category 2
							if (key.equalsIgnoreCase("8217")) {
								trfIn.put("TradeTypeCategory2", Long.parseLong(value));
								continue;
							}
							
							// Trade Type Category 3
							if (key.equalsIgnoreCase("8218")) {
								trfIn.put("TradeTypeCategory3", Long.parseLong(value));
								continue;
							}
							
							// Trade Type Category 4
							if (key.equalsIgnoreCase("8219")) {
								trfIn.put("TradeTypeCategory4", Long.parseLong(value));
							}
	
							// number of paricipant ID's
							if (key.equalsIgnoreCase("453")) {
								int num = Integer.parseInt(value);
								trfIn.put("NoPartyIDs", num);
								int j = 1;
								for (j=1; j<=(num*2); j++) {
									String[] anInnerField = logFields[i+j].split("=");
									key = anInnerField[0];
									value = anInnerField[1];
									// party id
									if (key.equalsIgnoreCase("448")) {
										trfIn.put("PartyID."+(int)((j+1)/2), value);
										continue;
									}
									// party role
									if (key.equalsIgnoreCase("452")) {
										trfIn.put("PartyRole."+(int)((j+1)/2), value);
										if ("55".equals(value)) {
											reportingObligation = true;											
										} 
										continue;
									}
								}
								// self modifying for loop
								// should never do this!
								// but we're professionals coding in a closed ide
								// so it's cool
								i = i + (j-1);
								continue;
							}	
							
							// get here, we don't know what it is, but don't want to lose it
							if (injectAll) {
								trfIn.put(key,value);
							}
						}
						if (!reportingObligation) {
							throw new Exception("Message contains no party with reporting obligation (452=55)");
						}
						if (SecondaryTrdType == null) {
							trfIn.put("TradeType", 8);
						} else if (SecondaryTrdType == "0") {
							trfIn.put("TradeType", 33);
						} else if (SecondaryTrdType == "1") {
							trfIn.put("TradeType", 35);
						} else if (SecondaryTrdType == "2") {
							trfIn.put("TradeType", 36);
						}
						// add the event type
						trfIn.put("event_name", eventName);
						trfIn.put("Firm", firm);
						trfIn.put("partition_on", firm);
						
						// i think this might be a bad idea - need a time based UUID to maintain uniqueness of entry
						ByteBuffer rowKey = se.toByteBuffer(firm);
						trfIn.put("_onRampSent", System.currentTimeMillis());
						if (logger.isDebugEnabled()) {
							logger.debug("Sending event " + trfIn);
						}
						mutator.addInsertion(rowKey, "system", createColumn(eventName, trfIn.toString(), se, se));
						mutator.execute();
						// nice and tidy
						trfIn = null;
						
						// let's have some visibility for testing please
						if (++count%1000==0) {
							logger.info(count + " messages sent with " + bad + " malformed messages skipped");
							// take a little break
							Thread.sleep(100);
						}
					}
				} catch (Exception e) {
					logger.error("Exception caught processing log message: " + str, e);
					bad++;
				}
			}	
		} catch (Exception e) {
			logger.error("Fatal exception caught by trfLogReader: " + e.getMessage(), e);
			System.exit(1);
		}
		logger.info(count + " total messages sent with " + bad + " total malformed messages skipped");
		System.exit(0);
	}
	
	private static void setProperties(String configFile) throws Exception {
		ClientConfigInfo info = null;
		info = ClientConfiguration.getConfiguration(configFile);
		System.out.println("TRF Log Reader is being configured using yaml config file " + configFile);

		log4j_file = info.getLog4j_file();
		darkStarNode = info.getDarkStarNode();
		darkStarPort = info.getDarkStarPort();
		firm = info.getFirm();
		clusterName = info.getClusterName();
		keyspace = info.getKeyspace();
		number_of_publishers = info.getNumber_of_publishers();
		batch_size = info.getBatch_size();
		eventName = info.getEventName();
	}
	
	// inside classes
	private static class MyConsistencyLevel implements ConsistencyLevelPolicy {
		@Override
		public HConsistencyLevel get(OperationType op, String arg1) {
			switch(op) {
				case READ: return HConsistencyLevel.ONE;
				case WRITE: return HConsistencyLevel.ANY;
			case META_READ:
				break;
			case META_WRITE:
				break;
			default:
				break;
			}
			return HConsistencyLevel.ONE;
		}

		@Override
		public HConsistencyLevel get(OperationType op) {
			switch(op) {
				case READ: return HConsistencyLevel.ONE;
				case WRITE: return HConsistencyLevel.ANY;
			case META_READ:
				break;
			case META_WRITE:
				break;
			default:
				break;
			}
			return HConsistencyLevel.ONE;
		}
	}
}
