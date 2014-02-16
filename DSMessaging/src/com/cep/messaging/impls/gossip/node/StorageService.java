package com.cep.messaging.impls.gossip.node;

import java.io.IOError;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.auth.Permission;
import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.KSMetaData;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.keyspace.commands.AddKeyspace;
import com.cep.messaging.impls.gossip.node.state.ApplicationState;
import com.cep.messaging.impls.gossip.node.state.EndpointState;
import com.cep.messaging.impls.gossip.node.state.IEndpointStateChangeSubscriber;
import com.cep.messaging.impls.gossip.node.state.VersionedValue;
import com.cep.messaging.impls.gossip.partitioning.IPartitioner;
import com.cep.messaging.impls.gossip.partitioning.bounds.Range;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.partitioning.token.StringToken;
import com.cep.messaging.impls.gossip.partitioning.token.Token;
import com.cep.messaging.impls.gossip.partitioning.token.TokenMetadata;
import com.cep.messaging.impls.gossip.service.IDarkStarMessagingDaemon;
import com.cep.messaging.impls.gossip.thrift.CfDef;
import com.cep.messaging.impls.gossip.thrift.InvalidRequestException;
import com.cep.messaging.impls.gossip.thrift.KsDef;
import com.cep.messaging.impls.gossip.thrift.ThriftValidation;
import com.cep.messaging.impls.gossip.transport.MessagingService;
import com.cep.messaging.impls.gossip.transport.handlers.GossipDigestAck2VerbHandler;
import com.cep.messaging.impls.gossip.transport.handlers.GossipDigestAckVerbHandler;
import com.cep.messaging.impls.gossip.transport.handlers.GossipDigestSynVerbHandler;
import com.cep.messaging.impls.gossip.transport.handlers.RowMutationVerbHandler;
import com.cep.messaging.impls.gossip.transport.handlers.ClientMessageVerbHandler;
import com.cep.messaging.impls.gossip.transport.handlers.SaveVerbHandler;
import com.cep.messaging.impls.gossip.transport.handlers.SendVerbHandler;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.Stage;
import com.cep.messaging.impls.gossip.util.Verb;
import com.cep.messaging.util.exception.ConfigurationException;
import com.cep.messaging.util.exception.InvalidTokenException;

/*
 * This abstraction contains the token/identifier of this node
 * on the identifier space. This token gets gossiped around.
 * This class will also maintain histograms of the load information
 * of other nodes in the cluster.
 */
@SuppressWarnings({ "rawtypes", "unused" })
public class StorageService implements IEndpointStateChangeSubscriber {
	private static Logger logger_ = LoggerFactory.getLogger(StorageService.class);
	private GossipService gossipService;
	private InetAddress removingNode;
	private boolean initialized;
	private volatile boolean joined = false;
	private String operationMode;
	private Token localToken = null;
	private Range localRange = null;
	private IDarkStarMessagingDaemon daemon;

	public static final int RING_DELAY = 30 * 1000; // delay after which we assume ring has stablized

	public static final Verb[] VERBS = Verb.values();

	@SuppressWarnings("serial")
	public static final EnumMap<Verb, Stage> verbStages = new EnumMap<Verb, Stage>(Verb.class) {
		{
			put(Verb.MUTATION, Stage.MUTATION);
			put(Verb.BINARY, Stage.MUTATION);
			put(Verb.READ_REPAIR, Stage.MUTATION);
			put(Verb.READ, Stage.READ);
			put(Verb.REQUEST_RESPONSE, Stage.REQUEST_RESPONSE);
			put(Verb.STREAM_REPLY, Stage.MISC);
			put(Verb.STREAM_REQUEST, Stage.STREAM);
			put(Verb.RANGE_SLICE, Stage.READ);
			put(Verb.BOOTSTRAP_TOKEN, Stage.MISC);
			put(Verb.TREE_REQUEST, Stage.ANTI_ENTROPY);
			put(Verb.TREE_RESPONSE, Stage.ANTI_ENTROPY);
			put(Verb.GOSSIP_DIGEST_ACK, Stage.GOSSIP);
			put(Verb.GOSSIP_DIGEST_ACK2, Stage.GOSSIP);
			put(Verb.GOSSIP_DIGEST_SYN, Stage.GOSSIP);
			put(Verb.DEFINITIONS_UPDATE, Stage.READ);
			put(Verb.TRUNCATE, Stage.MUTATION);
			put(Verb.SCHEMA_CHECK, Stage.MIGRATION);
			put(Verb.INDEX_SCAN, Stage.READ);
			put(Verb.REPLICATION_FINISHED, Stage.MISC);
			put(Verb.INTERNAL_RESPONSE, Stage.INTERNAL_RESPONSE);
			put(Verb.COUNTER_MUTATION, Stage.MUTATION);
			put(Verb.SAVE, Stage.MUTATION);
			put(Verb.SEND, Stage.MUTATION);
			put(Verb.UNUSED_3, Stage.INTERNAL_RESPONSE);
		}
	};
	

	/**
	 * This pool is used for periodic short (sub-second) tasks.
	 */
	public static final ScheduledThreadPoolExecutor scheduledTasks = new ScheduledThreadPoolExecutor(1);

	/**
	 * This pool is used by tasks that can have longer execution times, and
	 * usually are non periodic.
	 */
	public static final ScheduledThreadPoolExecutor tasks = new ScheduledThreadPoolExecutor(1);

	/**
	 * This abstraction maintains the token/endpoint metadata information
	 */
	private TokenMetadata tokenMetadata_ = new TokenMetadata();

	/**
	 * Construct a custom partitioner based on the class specified in the yaml
	 * file
	 */
	private IPartitioner<?> partitioner = NodeDescriptor.getPartitioner();

	/**
	 * This creates the abstraction which represents the state associated with a
	 * particular node which an application wants to make available to the rest
	 * of the nodes in the cluster.
	 */
	public VersionedValue.VersionedValueFactory valueFactory = new VersionedValue.VersionedValueFactory(partitioner);
	
	/**
	 * For a single VM GossipService must be run in either server or client mode.  
	 * Once set the mode cannot be changed.
	 */
	private boolean isClientMode;

	/**
	 * Singleton
	 */
	public static final StorageService instance = new StorageService();

	public static IPartitioner getPartitioner() {
		return instance.partitioner;
	}

	public void setToken(Token token) {
		if (logger_.isDebugEnabled()) {
			logger_.debug("Setting token to " + token);
		}
		localToken = token;
		tokenMetadata_.updateNormalToken(token, GossipUtilities.getLocalAddress());
		Gossiper.instance.addLocalApplicationState(ApplicationState.STATUS, valueFactory.normal(getLocalToken()));
		setMode("Normal", false);
	}

	private StorageService() {
		MessagingService.instance().registerVerbHandlers(Verb.GOSSIP_DIGEST_SYN, new GossipDigestSynVerbHandler());
		MessagingService.instance().registerVerbHandlers(Verb.GOSSIP_DIGEST_ACK, new GossipDigestAckVerbHandler());
		MessagingService.instance().registerVerbHandlers(Verb.GOSSIP_DIGEST_ACK2, new GossipDigestAck2VerbHandler());
		//TODO: For right now we're replacing the RowMutationVerbHandler with the new ClientMessageVerbHandler,
		//      but technically speaking we should really introduce a new Verb and use that instead.
		MessagingService.instance().registerVerbHandlers(Verb.MUTATION, new ClientMessageVerbHandler());
		MessagingService.instance().registerVerbHandlers(Verb.SAVE, new SaveVerbHandler());
		MessagingService.instance().registerVerbHandlers(Verb.SEND, new SendVerbHandler());
	}

	public void stopClient() {
		Gossiper.instance.unregister(this);
		Gossiper.instance.stop();
		MessagingService.instance().shutdown();
		// give it a second so that task accepted before the MessagingService
		// shutdown gets submitted to the stage (to avoid RejectedExecutionException)
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
		}
	}

	public boolean isInitialized() {
		return initialized;
	}
	
    public synchronized void initClient() throws IOException, ConfigurationException {
        if (initialized) {
            if (!isClientMode) {
                throw new UnsupportedOperationException("StorageService does not support switching modes.");
            }
            return;
        }
        initialized = true;
        isClientMode = true;
        logger_.info("Starting up client gossip");
        setMode("Client", false);
        Gossiper.instance.register(this);
        Gossiper.instance.start((int)(System.currentTimeMillis() / 1000)); 
        MessagingService.instance().listen(GossipUtilities.getLocalAddress());
        
        // sleep a while to allow gossip to warm up (the other nodes need to know about this one before they can reply).
        try {
            Thread.sleep(RING_DELAY);
        } catch (Exception ex) {
            throw new IOError(ex);
        }
    }

	public synchronized void initServer() throws IOException, ConfigurationException {
		if (!initialized) {
			initialized = true;
			isClientMode = false;
			joinTokenRing();
		}
	}

	private void joinTokenRing() throws IOException, ConfigurationException {
		logger_.info("Gossip Node " + GossipUtilities.getLocalAddress() + " joining token ring");
		joined = true;
		Gossiper.instance.register(this);
		Gossiper.instance.start((int) (System.currentTimeMillis() / 1000));

		MessagingService.instance().listen(GossipUtilities.getLocalAddress());
		Gossiper.instance.addLocalApplicationState(ApplicationState.RELEASE_VERSION, valueFactory.releaseVersion());

		Token token;
		String initialToken = NodeDescriptor.getInitialToken();
		if (initialToken == null) {
			throw new ConfigurationException("No Token specified for this node");
		} else {
			token = partitioner.getTokenFactory().fromString(initialToken);
			logger_.info("Using " + token + " from configuration");
		}
		setToken(token);
	}

	public boolean isJoined() {
		return joined;
	}

	private void setMode(String m, boolean log) {
		operationMode = m;
		if (log)
			logger_.info(m);
		else if (logger_.isDebugEnabled())
			logger_.debug(m);
	}

	public TokenMetadata getTokenMetadata() {
		return tokenMetadata_;
	}

	public Map<Token, String> getTokenToEndpointMap() {
		Map<Token, InetAddress> mapInetAddress = tokenMetadata_.getTokenToEndpointMap();
		Map<Token, String> mapString = new HashMap<Token, String>(mapInetAddress.size());
		for (Map.Entry<Token, InetAddress> entry : mapInetAddress.entrySet()) {
			mapString.put(entry.getKey(), entry.getValue().getHostAddress());
		}
		return mapString;
	}

	/*
	 * onChange only ever sees one ApplicationState piece change at a time, so
	 * we perform a kind of state machine here. We are concerned with two
	 * events: knowing the token associated with an endpoint, and knowing its
	 * operation mode. Nodes can start in either bootstrap or normal mode, and
	 * from bootstrap mode can change mode to normal. A node in bootstrap mode
	 * needs to have pendingranges set in TokenMetadata; a node in normal mode
	 * should instead be part of the token ring.
	 * 
	 * Normal MOVE_STATE progression of a node should be like this:
	 * STATE_BOOTSTRAPPING,token if bootstrapping. stays this way until all
	 * files are received. STATE_NORMAL,token ready to serve reads and writes.
	 * STATE_NORMAL,token,REMOVE_TOKEN,token specialized normal state in which
	 * this node acts as a proxy to tell the cluster about a dead node whose
	 * token is being removed. this value becomes the permanent state of this
	 * node (unless it coordinates another removetoken in the future).
	 * STATE_LEAVING,token get ready to leave the cluster as part of a
	 * decommission or move STATE_LEFT,token set after decommission or move is
	 * completed. STATE_MOVE,token set if node if currently moving to a new
	 * token in the ring
	 * 
	 * Note: Any time a node state changes from STATE_NORMAL, it will not be
	 * visible to new nodes. So it follows that you should never bootstrap a new
	 * node during a removetoken, decommission or move.
	 */
	public void onChange(InetAddress endpoint, ApplicationState state, VersionedValue value) {
		if (logger_.isDebugEnabled()) {
			logger_.debug("Change notification received from node at " + endpoint.getHostAddress());
		}
		switch (state) {
		case RELEASE_VERSION:
			break;
		case STATUS:
			String apStateValue = value.value;
			String[] pieces = apStateValue.split(VersionedValue.DELIMITER_STR, -1);
			String moveName = pieces[0];

			if (moveName.equals(VersionedValue.STATUS_NORMAL)) {
				handleStateNormal(endpoint, pieces);
			} else if (moveName.equals(VersionedValue.STATUS_LEAVING)) {
				handleStateLeaving(endpoint, pieces);
			} else if (moveName.equals(VersionedValue.STATUS_LEFT)) {
				handleStateLeft(endpoint, pieces);
			}
		}
	}

	/**
	 * Handle node move to normal state. That is, node is entering token ring
	 * and participating in reads.
	 * 
	 * @param endpoint
	 *            node
	 * @param pieces
	 *            STATE_NORMAL,token[,other_state,token]
	 */
	private void handleStateNormal(InetAddress endpoint, String[] pieces) {
		Token token = getPartitioner().getTokenFactory().fromString(pieces[1]);

		if (logger_.isDebugEnabled()) {
			logger_.debug("Node " + endpoint + " state normal, token " + token);
		}
		if (tokenMetadata_.isMember(endpoint)) {
			logger_.info("Node " + endpoint + " state jump to normal");
		}
		// we don't want to update if this node is responsible for the token and
		// it has a later startup time than endpoint.
		InetAddress currentOwner = tokenMetadata_.getEndpoint(token);
		if (currentOwner == null) {
			logger_.info("A new node at " + endpoint.getHostAddress() + " using token " + token + " has joined the ring");
			tokenMetadata_.updateNormalToken(token, endpoint);
		} else if (endpoint.equals(currentOwner)) {
			// set state back to normal, since the node may have tried to leave,
			// but failed and is now back up
			// no need to persist, token/ip did not change
			tokenMetadata_.updateNormalToken(token, endpoint);
		} else if (Gossiper.instance.compareEndpointStartup(endpoint, currentOwner) > 0) {
			logger_.info(String.format("Nodes %s and %s have the same token %s.  %s is the new owner",
					endpoint, currentOwner, token, endpoint));
			tokenMetadata_.updateNormalToken(token, endpoint);
		} else {
			logger_.info(String.format("Nodes %s and %s have the same token %s.  Ignoring %s",
					endpoint, currentOwner, token, endpoint));
		}

		if (tokenMetadata_.isMoving(endpoint)) {
			tokenMetadata_.removeFromMoving(endpoint);
		}
	}

	/**
	 * Handle node preparing to leave the ring
	 * 
	 * @param endpoint
	 *            node
	 * @param pieces
	 *            STATE_LEAVING,token
	 */
	private void handleStateLeaving(InetAddress endpoint, String[] pieces) {
		assert pieces.length == 2;
		String moveValue = pieces[1];
		Token token = getPartitioner().getTokenFactory().fromString(moveValue);

		if (logger_.isDebugEnabled()) {
			logger_.debug("Node " + endpoint + " state leaving, token " + token);
		}
		// If the node is previously unknown or tokens do not match, update tokenmetadata to
		// have this node as 'normal' (it must have been using this token before the
		// leave). This way we'll get pending ranges right.
		if (!tokenMetadata_.isMember(endpoint)) {
			logger_.info("Node " + endpoint + " state jump to leaving");
			tokenMetadata_.updateNormalToken(token, endpoint);
		} else if (!tokenMetadata_.getToken(endpoint).equals(token)) {
			logger_.warn("Node " + endpoint + " 'leaving' token mismatch. Long network partition?");
			tokenMetadata_.updateNormalToken(token, endpoint);
		}

		// at this point the endpoint is certainly a member with this token, so
		// let's proceed normally
		tokenMetadata_.addLeavingEndpoint(endpoint);
	}

	/**
	 * Handle node leaving the ring. This will happen when a node is
	 * decommissioned
	 * 
	 * @param endpoint
	 *            If reason for leaving is decommission, endpoint is the leaving
	 *            node.
	 * @param pieces
	 *            STATE_LEFT,token
	 */
	private void handleStateLeft(InetAddress endpoint, String[] pieces) {
		assert pieces.length == 2;
		Token token = getPartitioner().getTokenFactory().fromString(pieces[1]);

		if (logger_.isDebugEnabled()) {
			logger_.debug("Node " + endpoint + " state left, token " + token);
		}
		excise(token, endpoint);
	}

	private void excise(Token token, InetAddress endpoint) {
		Gossiper.instance.removeEndpoint(endpoint);
		tokenMetadata_.removeEndpoint(endpoint);
		tokenMetadata_.removeBootstrapToken(token);
		logger_.info("Removing token " + token + " for " + endpoint);
	}

	@SuppressWarnings("deprecation")
	public void onJoin(InetAddress endpoint, EndpointState epState) {
		for (Map.Entry<ApplicationState, VersionedValue> entry : epState.getApplicationStateMap().entrySet()) {
			onChange(endpoint, entry.getKey(), entry.getValue());
		}
	}

	public void onRemove(InetAddress endpoint) {
		tokenMetadata_.removeEndpoint(endpoint);
	}

	public void onDead(InetAddress endpoint, EndpointState state) {
		MessagingService.instance().convict(endpoint);
		refreshLocalRange();
	}

	public Token getLocalToken() {
		return localToken;
	}

	public Range getLocalRange() {
		return localRange;
	}

	public void refreshLocalRange() {
		localRange = partitioner.getLocalRange(localToken, getLiveTokens(tokenMetadata_.sortedTokens()));
		logger_.info("Local range being handled by this node is now " + localRange);
		if (logger_.isDebugEnabled()) {
			for (Token token : tokenMetadata_.sortedTokens()) {
				logger_.debug(token + " : " + tokenMetadata_.getEndpoint(token));
			}
		}
	}
	
	private ArrayList<Token> getLiveTokens(ArrayList<Token> tokens) {
		Set<InetAddress> liveNodes = Gossiper.instance.getLiveMembers();
		ArrayList<Token> live = new ArrayList<Token>();
		for (Token token : tokens) {
			if (liveNodes.contains(tokenMetadata_.getEndpoint(token))) {
				live.add(token);
			}
		}
		return live;
	}

	/**
	 * This method returns the predecessor of the endpoint ep on the identifier
	 * space.
	 */
	InetAddress getPredecessor(InetAddress ep) {
		Token token = tokenMetadata_.getToken(ep);
		return tokenMetadata_.getEndpoint(tokenMetadata_.getPredecessor(token));
	}

	/*
	 * This method returns the successor of the endpoint ep on the identifier
	 * space.
	 */
	public InetAddress getSuccessor(InetAddress ep) {
		Token token = tokenMetadata_.getToken(ep);
		return tokenMetadata_.getEndpoint(tokenMetadata_.getSuccessor(token));
	}

	/**
	 * Get the primary range for the specified endpoint.
	 * 
	 * @param ep
	 *            endpoint we are interested in.
	 * @return range for the specified endpoint.
	 */
	public Range getPrimaryRangeForEndpoint(InetAddress ep) {
		return tokenMetadata_.getPrimaryRangeFor(tokenMetadata_.getToken(ep));
	}

	/**
	 * Get all ranges that span the ring given a set of tokens. All ranges are
	 * in sorted order of ranges.
	 * 
	 * @return ranges in sorted order
	 */
	public List<Range> getAllRanges(List<Token> sortedTokens) {
		if (logger_.isDebugEnabled()) {
			logger_.debug("computing ranges for " + StringUtils.join(sortedTokens, ", "));
		}
		if (sortedTokens.isEmpty()) {
			return Collections.emptyList();
		}
		List<Range> ranges = new ArrayList<Range>();
		int size = sortedTokens.size();
		for (int i = 1; i < size; ++i) {
			Range range = new Range(sortedTokens.get(i - 1), sortedTokens.get(i));
			ranges.add(range);
		}
		Range range = new Range(sortedTokens.get(size - 1), sortedTokens.get(0));
		ranges.add(range);

		return ranges;
	}

	public void setLog4jLevel(String classQualifier, String rawLevel) {
		Level level = Level.toLevel(rawLevel);
		org.apache.log4j.Logger.getLogger(classQualifier).setLevel(level);
		logger_.info("set log level to " + level + " for classes under '"
				+ classQualifier + "' (if the level doesn't look like '"
				+ rawLevel + "' then log4j couldn't parse '" + rawLevel + "')");
	}

	/**
	 * Broadcast leaving status and update local tokenMetadata_ accordingly
	 */
	private void startLeaving() {
		Gossiper.instance.addLocalApplicationState(ApplicationState.STATUS, valueFactory.leaving(getLocalToken()));
		tokenMetadata_.addLeavingEndpoint(GossipUtilities.getLocalAddress());
	}

	public void decommission() throws InterruptedException {
		if (!tokenMetadata_.isMember(GossipUtilities.getLocalAddress())) {
			throw new UnsupportedOperationException("local node is not a member of the token ring yet");
		}
		if (tokenMetadata_.cloneAfterAllLeft().sortedTokens().size() < 2) {
			throw new UnsupportedOperationException("no other normal nodes in the ring; decommission would be pointless");
		}
		if (logger_.isDebugEnabled()) {
			logger_.debug("DECOMMISSIONING");
		}
		startLeaving();
		setMode("Leaving: sleeping " + RING_DELAY + " ms for pending range setup", true);
		Thread.sleep(RING_DELAY);
		leaveRing();
		new Runnable() {
			public void run() {
				Gossiper.instance.stop();
				MessagingService.instance().shutdown();
				setMode("Decommissioned", true);
				// let op be responsible for killing the process
			}
		}.run();
	}

	private void leaveRing() {
		tokenMetadata_.removeEndpoint(GossipUtilities.getLocalAddress());
		Gossiper.instance.addLocalApplicationState(ApplicationState.STATUS, valueFactory.left(getLocalToken()));
		logger_.info("Announcing that I have left the ring for " + RING_DELAY + "ms");
		try {
			Thread.sleep(RING_DELAY);
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		}
	}

	public String getOperationMode() {
		return operationMode;
	}

	@Override
	public void onAlive(InetAddress endpoint, EndpointState state) {
	}

	@SuppressWarnings("unchecked")
	public InetAddress getDestinationFor(String partitionField) throws InvalidTokenException {
		Set<InetAddress> liveNodes = Gossiper.instance.getLiveMembers();
		Token incoming = new StringToken(partitionField.toUpperCase());
		if (logger_.isDebugEnabled()) {
			logger_.debug("Getting destination for message with token of " + incoming);
		}
		Iterator<Token> it = tokenMetadata_.sortedTokens().iterator();
		Token comparator = null;
		Token lastAlive = null;
		InetAddress currentNode = null;
		while (it.hasNext()) {
			comparator = it.next();
			currentNode = tokenMetadata_.getEndpoint(comparator);
			if (liveNodes.contains(currentNode)) {
				lastAlive = comparator;
				if (incoming.compareTo(comparator) <= 0) {
					break;
				}
			} else { // Dead node, skip it.
				continue;
			}
		}
		if (lastAlive !=  null) {
			if (logger_.isDebugEnabled()) {
				logger_.debug("Message token " + incoming + " belongs to node with token of " + 
						lastAlive + " at " + tokenMetadata_.getEndpoint(lastAlive));
			}
			return tokenMetadata_.getEndpoint(lastAlive);
		} else {
			throw new InvalidTokenException("Token " + incoming + " cannot be sent because " +
					"there are no live nodes");
		}
	}

	public boolean handleLocally(String token) {
		return localRange.contains(new StringToken(token.toUpperCase()));
	}
	
    /**
     * for a keyspace, return the ranges and corresponding hosts for a given keyspace.
     * @param keyspace
     * @return
     */
    public Map<Range, List<String>> getRangeToEndpointMap(String keyspace)
    {
        // some people just want to get a visual representation of things. Allow null and set it to the first
        // non-system table.
        if (keyspace == null)
            keyspace = NodeDescriptor.getNonSystemTables().get(0);

        /* All the ranges for the tokens */
        Map<Range, List<String>> map = new HashMap<Range, List<String>>();
        for (Map.Entry<Range,List<InetAddress>> entry : getRangeToAddressMap(keyspace).entrySet())
        {
            map.put(entry.getKey(), stringify(entry.getValue()));
        }
        return map;
    }
    
    public Map<Range, List<InetAddress>> getRangeToAddressMap(String keyspace)
    {
        List<Range> ranges = getAllRanges(tokenMetadata_.sortedTokens());
        return constructRangeToEndpointMap(keyspace, ranges);
    }

    /**
     * Construct the range to endpoint mapping based on the true view
     * of the world.
     * @param ranges
     * @return mapping of ranges to the replicas responsible for them.
    */
    private Map<Range, List<InetAddress>> constructRangeToEndpointMap(String keyspace, List<Range> ranges)
    {
        Map<Range, List<InetAddress>> rangeToEndpointMap = new HashMap<Range, List<InetAddress>>();
        for (Range range : ranges)
        {
            rangeToEndpointMap.put(range, Table.open(keyspace).getReplicationStrategy().getNaturalEndpoints(range.right));
        }
        return rangeToEndpointMap;
    }

    private List<String> stringify(Iterable<InetAddress> endpoints)
    {
        List<String> stringEndpoints = new ArrayList<String>();
        for (InetAddress ep : endpoints)
        {
            stringEndpoints.add(ep.getHostAddress());
        }
        return stringEndpoints;
    }

    /**
     * @return list of Tokens (_not_ keys!) breaking up the data this node is responsible for into pieces of roughly keysPerSplit
     */ 
    public List<Token> getSplits(String table, String cfName, Range range, int keysPerSplit)
    {
        List<Token> tokens = new ArrayList<Token>();
        // we use the actual Range token for the first and last brackets of the splits to ensure correctness
        tokens.add(range.left);

        List<DecoratedKey> keys = new ArrayList<DecoratedKey>();
        Table t = Table.open(table);
        ColumnFamilyStore cfs = t.getColumnFamilyStore(cfName);
        for (DecoratedKey sample : cfs.allKeySamples())
        {
            if (range.contains(sample.token))
                keys.add(sample);
        }
        GossipUtilities.sortSampledKeys(keys, range);
        int splits = keys.size() * NodeDescriptor.getIndexInterval() / keysPerSplit;

        if (keys.size() >= splits)
        {
            for (int i = 1; i < splits; i++)
            {
                int index = i * (keys.size() / splits);
                tokens.add(keys.get(index).token);
            }
        }

        tokens.add(range.right);
        return tokens;
    }

	public boolean isClientMode() {
		return isClientMode;
	}

    public void registerDaemon(IDarkStarMessagingDaemon daemon)
    {
        this.daemon = daemon;
    }

	public List<InetAddress> getNaturalEndpoints(String table, ByteBuffer key) {
		return null;
	}

	public boolean useEfficientCrossDCWrites() {
		return false;
	}

	public List<InetAddress> getLiveNaturalEndpoints(String table,
			ByteBuffer key) {
		return null;
	}

	public boolean isBootstrapMode() {
		return false;
	}

	public List<InetAddress> getLiveNaturalEndpoints(String keyspace,
			Token right) {
		return null;
	}

	public Collection<Range> getLocalRanges(String name) {
		return null;
	}
}
