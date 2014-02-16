package com.cep.messaging.impls.gossip.node;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.cliffc.high_scale_lib.NonBlockingHashMap;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.net.FailureDetector;
import com.cep.messaging.impls.gossip.net.IFailureDetectionEventListener;
import com.cep.messaging.impls.gossip.net.IFailureDetector;
import com.cep.messaging.impls.gossip.node.state.ApplicationState;
import com.cep.messaging.impls.gossip.node.state.EndpointState;
import com.cep.messaging.impls.gossip.node.state.HeartBeatState;
import com.cep.messaging.impls.gossip.node.state.IEndpointStateChangeSubscriber;
import com.cep.messaging.impls.gossip.node.state.VersionedValue;
import com.cep.messaging.impls.gossip.transport.MessageProducer;
import com.cep.messaging.impls.gossip.transport.MessagingService;
import com.cep.messaging.impls.gossip.transport.messages.GossipDigest;
import com.cep.messaging.impls.gossip.transport.messages.GossipDigestAck2Message;
import com.cep.messaging.impls.gossip.transport.messages.GossipDigestAckMessage;
import com.cep.messaging.impls.gossip.transport.messages.GossipDigestSynMessage;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.Verb;

/**
 * This module is responsible for Gossiping information for the local endpoint.
 * This abstraction maintains the list of live and dead endpoints. Periodically
 * i.e. every 1 second this module chooses a random node and initiates a round
 * of Gossip with it. A round of Gossip involves 3 rounds of messaging. For
 * instance if node A wants to initiate a round of Gossip with node B it starts
 * off by sending node B a GossipDigestSynMessage. Node B on receipt of this
 * message sends node A a GossipDigestAckMessage. On receipt of this message
 * node A sends node B a GossipDigestAck2Message which completes a round of
 * Gossip. This module as and when it hears one of the three above mentioned
 * messages updates the Failure Detector with the liveness information.
 */

public class Gossiper implements IFailureDetectionEventListener {
	private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

	static final ApplicationState[] STATES = ApplicationState.values();
	private ScheduledFuture<?> scheduledGossipTask;
	public final static int intervalInMillis = 1000;
	public final static int QUARANTINE_DELAY = StorageService.RING_DELAY * 2;
	private static Logger logger = Logger.getLogger(Gossiper.class);
	public static final Gossiper instance = new Gossiper();

	private long aVeryLongTime;
	private long FatClientTimeout;
	private Random random = new Random();

	private Comparator<InetAddress> inetcomparator = new Comparator<InetAddress>() {
		public int compare(InetAddress addr1, InetAddress addr2) {
			return addr1.getHostAddress().compareTo(addr2.getHostAddress());
		}
	};

	/* subscribers for interest in EndpointState change */
	private List<IEndpointStateChangeSubscriber> subscribers = new CopyOnWriteArrayList<IEndpointStateChangeSubscriber>();

	/* live member set */
	private Set<InetAddress> liveEndpoints = new ConcurrentSkipListSet<InetAddress>(inetcomparator);

	/* unreachable member set */
	private Map<InetAddress, Long> unreachableEndpoints = new ConcurrentHashMap<InetAddress, Long>();

	/* initial seeds for joining the cluster */
	private Set<InetAddress> seeds = new ConcurrentSkipListSet<InetAddress>(inetcomparator);

	/*
	 * map where key is the endpoint and value is the state associated with the endpoint
	 */
	public Map<InetAddress, EndpointState> endpointStateMap = new ConcurrentHashMap<InetAddress, EndpointState>();

	/*
	 * map where key is endpoint and value is timestamp when this endpoint was
	 * removed from gossip. We will ignore any gossip regarding these endpoints
	 * for QUARANTINE_DELAY time after removal to prevent nodes from falsely
	 * reincarnating during the time when removal gossip gets propagated to all
	 * nodes
	 */
	private Map<InetAddress, Long> justRemovedEndpoints = new ConcurrentHashMap<InetAddress, Long>();

	// protocol versions of the other nodes in the cluster
	private final ConcurrentMap<InetAddress, Integer> versions = new NonBlockingHashMap<InetAddress, Integer>();

	public static ApplicationState[] getStates() {
		return STATES;
	}

	private class GossipTask implements Runnable {
		public void run() {
			try {
				// wait on messaging service to start listening
				MessagingService.instance().waitUntilListening();

				/* Update the local heartbeat counter. */
				endpointStateMap.get(GossipUtilities.getLocalAddress()).getHeartBeatState().updateHeartBeat();
				if (logger.isDebugEnabled()) {
					logger.debug("My heartbeat is now " + endpointStateMap.get(GossipUtilities.getLocalAddress())
									.getHeartBeatState().getHeartBeatVersion());
				}
				final List<GossipDigest> gDigests = new ArrayList<GossipDigest>();
				Gossiper.instance.makeRandomGossipDigest(gDigests);

				if (gDigests.size() > 0) {
					MessageProducer prod = new MessageProducer() {
						public Message getMessage(Integer version) throws IOException {
							return makeGossipDigestSynMessage(gDigests, version);
						}
					};
					/* Gossip to some random live member */
					boolean gossipedToSeed = doGossipToLiveMember(prod);

					/*
					 * Gossip to some unreachable member with some probability
					 * to check if he is back up
					 */
					doGossipToUnreachableMember(prod);

					/*
					 * Gossip to a seed if we did not do so above, or we have
					 * seen less nodes than there are seeds. This prevents
					 * partitions where each group of nodes is only gossiping to
					 * a subset of the seeds.
					 * 
					 * The most straightforward check would be to check that all
					 * the seeds have been verified either as live or
					 * unreachable. To avoid that computation each round, we
					 * reason that:
					 * 
					 * either all the live nodes are seeds, in which case
					 * non-seeds that come online will introduce themselves to a
					 * member of the ring by definition,
					 * 
					 * or there is at least one non-seed node in the list, in
					 * which case eventually someone will gossip to it, and then
					 * do a gossip to a random seed from the gossipedToSeed
					 * check.
					 */
					if (!gossipedToSeed || liveEndpoints.size() < seeds.size()) {
						doGossipToSeed(prod);
					}
					doStatusCheck();
				}
			} catch (Exception e) {
				logger.error("Gossip error", e);
			}
		}
	}

	private Gossiper() {
		// 3 days
		aVeryLongTime = 259200 * 1000;
		// half of QUARATINE_DELAY, to ensure justRemovedEndpoints has enough
		// leeway to prevent re-gossip
		FatClientTimeout = (long) (QUARANTINE_DELAY / 2);
		/*
		 * register with the Failure Detector for receiving Failure detector
		 * events
		 */
		FailureDetector.instance.registerFailureDetectionEventListener(this);
	}

	/**
	 * Register for interesting state changes.
	 * 
	 * @param subscriber
	 *            module which implements the IEndpointStateChangeSubscriber
	 */
	public void register(IEndpointStateChangeSubscriber subscriber) {
		subscribers.add(subscriber);
	}

	/**
	 * Unregister interest for state changes.
	 * 
	 * @param subscriber
	 *            module which implements the IEndpointStateChangeSubscriber
	 */
	public void unregister(IEndpointStateChangeSubscriber subscriber) {
		subscribers.remove(subscriber);
	}

	public void setVersion(InetAddress address, int version) {
		versions.put(address, version);
	}

	public Integer getVersion(InetAddress address) {
		Integer v = versions.get(address);
		if (v == null) {
			logger.debug("Assuming current protocol version for " + address);
			return MessagingService.version_;
		} else {
			return v;
		}
	}

	public Set<InetAddress> getLiveMembers() {
		Set<InetAddress> liveMbrs = new HashSet<InetAddress>(liveEndpoints);
		if (!liveMbrs.contains(GossipUtilities.getLocalAddress())) {
			liveMbrs.add(GossipUtilities.getLocalAddress());
		}
		return liveMbrs;
	}

	public Set<InetAddress> getUnreachableMembers() {
		return unreachableEndpoints.keySet();
	}

	public long getEndpointDowntime(InetAddress ep) {
		Long downtime = unreachableEndpoints.get(ep);
		if (downtime != null) {
			return System.currentTimeMillis() - downtime;
		} else {
			return 0L;
		}
	}

	/**
	 * This method is part of IFailureDetectionEventListener interface. This is
	 * invoked by the Failure Detector when it convicts an end point.
	 * 
	 * param @ endpoint end point that is convicted.
	 */
	public void convict(InetAddress endpoint) {
		EndpointState epState = endpointStateMap.get(endpoint);
		if (epState.isAlive()) {
			markDead(endpoint, epState);
		}
	}

	/**
	 * Return either: the greatest heartbeat or application state
	 * 
	 * @param epState
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public int getMaxEndpointStateVersion(EndpointState epState) {
		int maxVersion = epState.getHeartBeatState().getHeartBeatVersion();
		for (VersionedValue value : epState.getApplicationStateMap().values()) {
			maxVersion = Math.max(maxVersion, value.version);
		}
		return maxVersion;
	}

	/**
	 * Removes the endpoint from unreachable endpoint set
	 * 
	 * @param endpoint
	 *            endpoint to be removed from the current membership.
	 */
	private void evictFromMembership(InetAddress endpoint) {
		unreachableEndpoints.remove(endpoint);
	}

	/**
	 * Removes the endpoint completely from Gossip
	 */
	public void removeEndpoint(InetAddress endpoint) {
		// do subscribers first so anything in the subscriber that depends on
		// gossiper state won't get confused
		for (IEndpointStateChangeSubscriber subscriber : subscribers) {
			subscriber.onRemove(endpoint);
		}
		liveEndpoints.remove(endpoint);
		unreachableEndpoints.remove(endpoint);
		// do not remove endpointState until the quarantine expires
		FailureDetector.instance.remove(endpoint);
		versions.remove(endpoint);
		justRemovedEndpoints.put(endpoint, System.currentTimeMillis());
	}

	/**
	 * The gossip digest is built based on randomization rather than just
	 * looping through the collection of live endpoints.
	 * 
	 * @param gDigests
	 *            list of Gossip Digests.
	 */
	private void makeRandomGossipDigest(List<GossipDigest> gDigests) {
		EndpointState epState;
		int generation = 0;
		int maxVersion = 0;

		// local epstate will be part of endpointStateMap
		List<InetAddress> endpoints = new ArrayList<InetAddress>(endpointStateMap.keySet());
		Collections.shuffle(endpoints, random);
		for (InetAddress endpoint : endpoints) {
			epState = endpointStateMap.get(endpoint);
			if (epState != null) {
				generation = epState.getHeartBeatState().getGeneration();
				maxVersion = getMaxEndpointStateVersion(epState);
			}
			gDigests.add(new GossipDigest(endpoint, generation, maxVersion));
		}

		if (logger.isTraceEnabled()) {
			StringBuilder sb = new StringBuilder();
			for (GossipDigest gDigest : gDigests) {
				sb.append(gDigest);
				sb.append(" ");
			}
			logger.trace("Gossip Digests are : " + sb.toString());
		}
	}

	public boolean isKnownEndpoint(InetAddress endpoint) {
		return endpointStateMap.containsKey(endpoint);
	}

	public int getCurrentGenerationNumber(InetAddress endpoint) {
		return endpointStateMap.get(endpoint).getHeartBeatState().getGeneration();
	}

	Message makeGossipDigestSynMessage(List<GossipDigest> gDigests, int version) throws IOException {
		GossipDigestSynMessage gDigestMessage = new GossipDigestSynMessage(NodeDescriptor.getClusterName(), gDigests);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		GossipDigestSynMessage.serializer().serialize(gDigestMessage, dos, version);
		return new Message(GossipUtilities.getLocalAddress(), Verb.GOSSIP_DIGEST_SYN, bos.toByteArray(), version);
	}

	public Message makeGossipDigestAckMessage(GossipDigestAckMessage gDigestAckMessage, int version) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		GossipDigestAckMessage.serializer().serialize(gDigestAckMessage, dos, version);
		return new Message(GossipUtilities.getLocalAddress(), Verb.GOSSIP_DIGEST_ACK, bos.toByteArray(), version);
	}

	public Message makeGossipDigestAck2Message(GossipDigestAck2Message gDigestAck2Message, int version) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		GossipDigestAck2Message.serializer().serialize(gDigestAck2Message, dos, version);
		return new Message(GossipUtilities.getLocalAddress(), Verb.GOSSIP_DIGEST_ACK2, bos.toByteArray(), version);
	}

	/**
	 * Returns true if the chosen target was also a seed. False otherwise
	 * 
	 * @param prod
	 *            produces a message to send
	 * @param epSet
	 *            a set of endpoint from which a random endpoint is chosen.
	 * @return true if the chosen endpoint is also a seed.
	 */
	private boolean sendGossip(MessageProducer prod, Set<InetAddress> epSet) {
		int size = epSet.size();
		/* Generate a random number from 0 -> size */
		List<InetAddress> liveEndpoints = new ArrayList<InetAddress>(epSet);
		int index = (size == 1) ? 0 : random.nextInt(size);
		InetAddress to = liveEndpoints.get(index);
		if (logger.isDebugEnabled()) {
			logger.debug("Sending a GossipDigestSynMessage to " + to);
		}
		try {
			MessagingService.instance().sendOneWay(prod.getMessage(getVersion(to)), to);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		return seeds.contains(to);
	}

	/*
	 * Sends a Gossip message to a live member and returns true if the recipient
	 * was a seed
	 */
	private boolean doGossipToLiveMember(MessageProducer prod) {
		int size = liveEndpoints.size();
		if (size == 0) {
			return false;
		}
		return sendGossip(prod, liveEndpoints);
	}

	/* Sends a Gossip message to an unreachable member */
	private void doGossipToUnreachableMember(MessageProducer prod) {
		double liveEndpointCount = liveEndpoints.size();
		double unreachableEndpointCount = unreachableEndpoints.size();
		if (unreachableEndpointCount > 0) {
			/* based on some probability */
			double prob = unreachableEndpointCount / (liveEndpointCount + 1);
			double randDbl = random.nextDouble();
			if (randDbl < prob) {
				sendGossip(prod, unreachableEndpoints.keySet());
			}
		}
	}

	/* Gossip to a seed for facilitating partition healing */
	private void doGossipToSeed(MessageProducer prod) {
		int size = seeds.size();
		if (size > 0) {
			if (size == 1 && seeds.contains(GossipUtilities.getLocalAddress())) {
				return;
			}
			if (liveEndpoints.size() == 0) {
				sendGossip(prod, seeds);
			} else {
				/* Gossip with the seed with some probability. */
				double probability = seeds.size() / (double) (liveEndpoints.size() + unreachableEndpoints.size());
				double randDbl = random.nextDouble();
				if (randDbl <= probability)
					sendGossip(prod, seeds);
			}
		}
	}

	private void doStatusCheck() {
		long now = System.currentTimeMillis();

		Set<InetAddress> eps = endpointStateMap.keySet();
		for (InetAddress endpoint : eps) {
			if (endpoint.equals(GossipUtilities.getLocalAddress()))
				continue;

			FailureDetector.instance.interpret(endpoint);
			EndpointState epState = endpointStateMap.get(endpoint);
			if (epState != null) {
				long duration = now - epState.getUpdateTimestamp();

				// check if this is a fat client. fat clients are removed
				// automatically from
				// gosip after FatClientTimeout
				if (!epState.hasToken() && !epState.isAlive() && (duration > FatClientTimeout)) {
					if (StorageService.instance.getTokenMetadata().isMember(endpoint)) {
						epState.setHasToken(true);
					} else {
						if (!justRemovedEndpoints.containsKey(endpoint)) {
							logger.info("FatClient " + endpoint
									+ " has been silent for "
									+ FatClientTimeout
									+ "ms, removing from gossip");
							removeEndpoint(endpoint); 
						}
					}
				}

				if (!epState.isAlive() && (duration > aVeryLongTime)) {
					evictFromMembership(endpoint);
				}
			}
		}

		if (!justRemovedEndpoints.isEmpty()) {
			for (Entry<InetAddress, Long> entry : justRemovedEndpoints.entrySet()) {
				if ((now - entry.getValue()) > QUARANTINE_DELAY) {
					if (logger.isDebugEnabled()) {
						logger.debug(QUARANTINE_DELAY + " elapsed, " + entry.getKey() + " gossip quarantine over");
					}
					justRemovedEndpoints.remove(entry.getKey());
					endpointStateMap.remove(entry.getKey());
				}
			}
		}
	}

	public EndpointState getEndpointStateForEndpoint(InetAddress ep) {
		return endpointStateMap.get(ep);
	}

	public Set<Entry<InetAddress, EndpointState>> getEndpointStates() {
		return endpointStateMap.entrySet();
	}

	@SuppressWarnings("deprecation")
	public EndpointState getStateForVersionBiggerThan(InetAddress forEndpoint, int version) {
		EndpointState epState = endpointStateMap.get(forEndpoint);
		EndpointState reqdEndpointState = null;

		if (epState != null) {
			/*
			 * Here we try to include the Heart Beat state only if it is greater
			 * than the version passed in. It might happen that the heart beat
			 * version maybe lesser than the version passed in and some
			 * application state has a version that is greater than the version
			 * passed in. In this case we also send the old heart beat and throw
			 * it away on the receiver if it is redundant.
			 */
			int localHbVersion = epState.getHeartBeatState().getHeartBeatVersion();
			if (localHbVersion > version) {
				reqdEndpointState = new EndpointState(epState.getHeartBeatState());
				if (logger.isTraceEnabled()) {
					logger.trace("local heartbeat version " + localHbVersion
							+ " greater than " + version + " for " + forEndpoint);
				}
			}
			/*
			 * Accumulate all application states whose versions are greater than
			 * "version" variable
			 */
			for (Entry<ApplicationState, VersionedValue> entry : epState.getApplicationStateMap().entrySet()) {
				VersionedValue value = entry.getValue();
				if (value.version > version) {
					if (reqdEndpointState == null) {
						reqdEndpointState = new EndpointState(epState.getHeartBeatState());
					}
					final ApplicationState key = entry.getKey();
					if (logger.isTraceEnabled()) {
						logger.trace("Adding state " + key + ": " + value.value);
					}
					reqdEndpointState.addApplicationState(key, value);
				}
			}
		}
		return reqdEndpointState;
	}

	/** determine which endpoint started up earlier */
	public int compareEndpointStartup(InetAddress addr1, InetAddress addr2) {
		EndpointState ep1 = getEndpointStateForEndpoint(addr1);
		EndpointState ep2 = getEndpointStateForEndpoint(addr2);
		assert ep1 != null && ep2 != null;
		return ep1.getHeartBeatState().getGeneration() - ep2.getHeartBeatState().getGeneration();
	}

	public void notifyFailureDetector(List<GossipDigest> gDigests) {
		for (GossipDigest gDigest : gDigests) {
			notifyFailureDetector(gDigest.endpoint, endpointStateMap.get(gDigest.endpoint));
		}
	}

	public void notifyFailureDetector(Map<InetAddress, EndpointState> remoteEpStateMap) {
		for (Entry<InetAddress, EndpointState> entry : remoteEpStateMap.entrySet()) {
			notifyFailureDetector(entry.getKey(), entry.getValue());
		}
	}

	void notifyFailureDetector(InetAddress endpoint, EndpointState remoteEndpointState) {
		IFailureDetector fd = FailureDetector.instance;
		EndpointState localEndpointState = endpointStateMap.get(endpoint);
		/*
		 * If the local endpoint state exists then report to the FD only if the
		 * versions workout.
		 */
		if (localEndpointState != null) {
			int localGeneration = localEndpointState.getHeartBeatState().getGeneration();
			int remoteGeneration = remoteEndpointState.getHeartBeatState().getGeneration();
			if (remoteGeneration > localGeneration) {
				fd.report(endpoint);
				return;
			}

			if (remoteGeneration == localGeneration) {
				int localVersion = getMaxEndpointStateVersion(localEndpointState);
				int remoteVersion = remoteEndpointState.getHeartBeatState().getHeartBeatVersion();
				if (remoteVersion > localVersion) {
					fd.report(endpoint);
				}
			}
		}
	}

	private void markAlive(InetAddress addr, EndpointState localState) {
		if (logger.isTraceEnabled()) {
			logger.trace("marking as alive " + addr);
		}
		localState.markAlive();
		liveEndpoints.add(addr);
		unreachableEndpoints.remove(addr);
		logger.info("InetAddress {" + addr + "} is now UP ");
		for (IEndpointStateChangeSubscriber subscriber : subscribers) {
			subscriber.onAlive(addr, localState);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Notified " + subscribers);
		}
	}

	private void markDead(InetAddress addr, EndpointState localState) {
		if (logger.isTraceEnabled()) {
			logger.trace("marking as dead " + addr);
		}
		localState.markDead();
		liveEndpoints.remove(addr);
		unreachableEndpoints.put(addr, System.currentTimeMillis());
		logger.info("InetAddress " + addr + " is now dead.");
		for (IEndpointStateChangeSubscriber subscriber : subscribers) {
			subscriber.onDead(addr, localState);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Notified " + subscribers);
		}
	}

	/**
	 * This method is called whenever there is a "big" change in ep state (a
	 * generation change for a known node).
	 * 
	 * @param ep
	 *            endpoint
	 * @param epState
	 *            EndpointState for the endpoint
	 */
	private void handleMajorStateChange(InetAddress ep, EndpointState epState) {
		if (endpointStateMap.get(ep) != null) {
			logger.info("Node " + ep + " has restarted, now UP again");
		} else {
			logger.info("Node " + ep + " is now part of the cluster");
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Adding endpoint state for " + ep);
		}
		endpointStateMap.put(ep, epState);
		if (epState.isAlive()) {
			// the node restarted before we ever marked it down, so we'll report
			// it as dead briefly so maintenance like resetting the connection
			// pool can occur
			for (IEndpointStateChangeSubscriber subscriber : subscribers) {
				subscriber.onDead(ep, epState);
			}
		}
		markAlive(ep, epState);
		for (IEndpointStateChangeSubscriber subscriber : subscribers) {
			subscriber.onJoin(ep, epState);
		}
	}

	public void applyStateLocally(Map<InetAddress, EndpointState> epStateMap) {
		for (Entry<InetAddress, EndpointState> entry : epStateMap.entrySet()) {
			InetAddress ep = entry.getKey();
			if (ep.equals(GossipUtilities.getLocalAddress()))
				continue;
			if (justRemovedEndpoints.containsKey(ep)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Ignoring gossip for " + ep + " because it is quarantined");
				}
				continue;
			}

			EndpointState localEpStatePtr = endpointStateMap.get(ep);
			EndpointState remoteState = entry.getValue();
			/*
			 * If state does not exist just add it. If it does then add it if
			 * the remote generation is greater. If there is a generation tie,
			 * attempt to break it by heartbeat version.
			 */
			if (localEpStatePtr != null) {
				int localGeneration = localEpStatePtr.getHeartBeatState().getGeneration();
				int remoteGeneration = remoteState.getHeartBeatState().getGeneration();
				if (logger.isTraceEnabled()) {
					logger.trace(ep + "local generation " + localGeneration + ", remote generation " + remoteGeneration);
				}
				if (remoteGeneration > localGeneration) {
					if (logger.isTraceEnabled()) {
						logger.trace("Updating heartbeat state generation to "
								+ remoteGeneration + " from " + localGeneration + " for " + ep);
					}
					// major state change will handle the update by inserting
					// the remote state directly
					handleMajorStateChange(ep, remoteState);
				} else if (remoteGeneration == localGeneration) {
					/* find maximum state */
					int localMaxVersion = getMaxEndpointStateVersion(localEpStatePtr);
					int remoteMaxVersion = getMaxEndpointStateVersion(remoteState);
					if (remoteMaxVersion > localMaxVersion) {
						// apply states, but do not notify since there is no major change
						applyNewStates(ep, localEpStatePtr, remoteState);
					} else if (logger.isTraceEnabled()) {
						logger.trace("Ignoring remote version "
								+ remoteMaxVersion + " <= " + localMaxVersion + " for " + ep);
					}
					if (!localEpStatePtr.isAlive()) {
						markAlive(ep, localEpStatePtr);
					}
				} else {
					if (logger.isTraceEnabled()) {
						logger.trace("Ignoring remote generation " + remoteGeneration + " < " + localGeneration);
					}
				}
			} else {
				// this is a new node
				handleMajorStateChange(ep, remoteState);
			}
		}
	}

	@SuppressWarnings({ "unused", "deprecation" })
	private void applyNewStates(InetAddress addr, EndpointState localState, EndpointState remoteState) {
		// don't assert here, since if the node restarts the version will go back to zero
		int oldVersion = localState.getHeartBeatState().getHeartBeatVersion();
		Map<ApplicationState, VersionedValue> localAppStateMap = localState.getApplicationStateMap();

		localState.setHeartBeatState(remoteState.getHeartBeatState());
		if (logger.isTraceEnabled()) {
			logger.trace("Updating heartbeat state version to "
					+ localState.getHeartBeatState().getHeartBeatVersion()
					+ " from " + oldVersion + " for " + addr + " ...");
		}
		for (Entry<ApplicationState, VersionedValue> remoteEntry : remoteState.getApplicationStateMap().entrySet()) {
			ApplicationState remoteKey = remoteEntry.getKey();
			VersionedValue remoteValue = remoteEntry.getValue();

			assert remoteState.getHeartBeatState().getGeneration() == localState.getHeartBeatState().getGeneration();
			localState.addApplicationState(remoteKey, remoteValue);
			doNotifications(addr, remoteKey, remoteValue);
		}
	}

	// notify that an application state has changed
	private void doNotifications(InetAddress addr, ApplicationState state, VersionedValue value) {
		for (IEndpointStateChangeSubscriber subscriber : subscribers) {
			subscriber.onChange(addr, state, value);
		}
	}

	/* Request all the state for the endpoint in the gDigest */
	private void requestAll(GossipDigest gDigest,
			List<GossipDigest> deltaGossipDigestList, int remoteGeneration) {
		/*
		 * We are here since we have no data for this endpoint locally so
		 * request everthing.
		 */
		deltaGossipDigestList.add(new GossipDigest(gDigest.getEndpoint(), remoteGeneration, 0));
		if (logger.isTraceEnabled()) {
			logger.trace("requestAll for " + gDigest.getEndpoint());
		}
	}

	/* Send all the data with version greater than maxRemoteVersion */
	private void sendAll(GossipDigest gDigest, Map<InetAddress, EndpointState> deltaEpStateMap, int maxRemoteVersion) {
		EndpointState localEpStatePtr = getStateForVersionBiggerThan(gDigest.getEndpoint(), maxRemoteVersion);
		if (localEpStatePtr != null) {
			deltaEpStateMap.put(gDigest.getEndpoint(), localEpStatePtr);
		}
	}

	/*
	 * This method is used to figure the state that the Gossiper has but
	 * Gossipee doesn't. The delta digests and the delta state are built up.
	 */
	public void examineGossiper(List<GossipDigest> gDigestList, List<GossipDigest> deltaGossipDigestList,
			Map<InetAddress, EndpointState> deltaEpStateMap) {
		for (GossipDigest gDigest : gDigestList) {
			int remoteGeneration = gDigest.getGeneration();
			int maxRemoteVersion = gDigest.getMaxVersion();
			/* Get state associated with the end point in digest */
			EndpointState epStatePtr = endpointStateMap.get(gDigest.getEndpoint());
			/*
			 * Here we need to fire a GossipDigestAckMessage. If we have some
			 * data associated with this endpoint locally then we follow the
			 * "if" path of the logic. If we have absolutely nothing for this
			 * endpoint we need to request all the data for this endpoint.
			 */
			if (epStatePtr != null) {
				int localGeneration = epStatePtr.getHeartBeatState().getGeneration();
				/*
				 * get the max version of all keys in the state associated with this endpoint
				 */
				int maxLocalVersion = getMaxEndpointStateVersion(epStatePtr);
				if (remoteGeneration == localGeneration && maxRemoteVersion == maxLocalVersion) {
					continue;
				}
				if (remoteGeneration > localGeneration) {
					/* we request everything from the gossiper */
					requestAll(gDigest, deltaGossipDigestList, remoteGeneration);
				} else if (remoteGeneration < localGeneration) {
					/*
					 * send all data with generation = localgeneration and
					 * version > 0
					 */
					sendAll(gDigest, deltaEpStateMap, 0);
				} else if (remoteGeneration == localGeneration) {
					/*
					 * If the max remote version is greater then we request the
					 * remote endpoint send us all the data for this endpoint
					 * with version greater than the max version number we have
					 * locally for this endpoint. If the max remote version is
					 * lesser, then we send all the data we have locally for
					 * this endpoint with version greater than the max remote
					 * version.
					 */
					if (maxRemoteVersion > maxLocalVersion) {
						deltaGossipDigestList.add(new GossipDigest(gDigest.getEndpoint(), remoteGeneration, maxLocalVersion));
					} else if (maxRemoteVersion < maxLocalVersion) {
						/*
						 * send all data with generation = localgeneration and
						 * version > maxRemoteVersion
						 */
						sendAll(gDigest, deltaEpStateMap, maxRemoteVersion);
					}
				}
			} else {
				/*
				 * We are here since we have no data for this endpoint locally
				 * so request everything.
				 */
				requestAll(gDigest, deltaGossipDigestList, remoteGeneration);
			}
		}
	}

	/**
	 * Start the gossiper
	 */
	public void start(int startTime) {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting gossiper with time " + startTime);
		}
		/* Get the seeds from the config and initialize them. */
		Set<InetAddress> seedHosts = NodeDescriptor.getSeeds();
		for (InetAddress seed : seedHosts) {
			if (seed.equals(GossipUtilities.getLocalAddress())) {
				continue;
			}
			logger.info("Adding node " + seed + " to seeds");
			seeds.add(seed);
		}

		/* initialize the heartbeat state for this localEndpoint */
		maybeInitializeLocalState(startTime);
		EndpointState localState = endpointStateMap.get(GossipUtilities.getLocalAddress());

		// notify snitches that Gossiper is about to start
		if (logger.isTraceEnabled()) {
			logger.trace("gossip started with generation " + localState.getHeartBeatState().getGeneration());
		}
		scheduledGossipTask = executor.scheduleWithFixedDelay(new GossipTask(),
				Gossiper.intervalInMillis, Gossiper.intervalInMillis, TimeUnit.MILLISECONDS);
	}

	// initialize local HB state if needed.
	public void maybeInitializeLocalState(int startTime) {
		EndpointState localState = endpointStateMap.get(GossipUtilities.getLocalAddress());
		if (localState == null) {
			HeartBeatState hbState = new HeartBeatState(startTime);
			localState = new EndpointState(hbState);
			localState.markAlive();
			endpointStateMap.put(GossipUtilities.getLocalAddress(), localState);
		}
	}

	/**
	 * Add an endpoint we knew about previously, but whose state is unknown
	 */
	public void addSavedEndpoint(InetAddress ep) {
		EndpointState epState = endpointStateMap.get(ep);
		if (epState == null) {
			epState = new EndpointState(new HeartBeatState(0));
			epState.markDead();
			epState.setHasToken(true);
			endpointStateMap.put(ep, epState);
			unreachableEndpoints.put(ep, System.currentTimeMillis());
			if (logger.isTraceEnabled()) {
				logger.trace("Adding saved endpoint " + ep + " "
						+ epState.getHeartBeatState().getGeneration());
			}
		}
	}

	public void addLocalApplicationState(ApplicationState state, VersionedValue value) {
		EndpointState epState = endpointStateMap.get(GossipUtilities.getLocalAddress());
		assert epState != null;
		if (logger.isDebugEnabled()) {
			logger.debug("Gossiper setting local application state to " + value);
		}
		epState.addApplicationState(state, value);
	}

	public void stop() {
		scheduledGossipTask.cancel(false);
	}

	public boolean isEnabled() {
		return !scheduledGossipTask.isCancelled();
	}

	/**
	 * This should *only* be used for testing purposes.
	 */
	public void initializeNodeUnsafe(InetAddress addr, int generationNbr) {
		/* initialize the heartbeat state for this localEndpoint */
		EndpointState localState = endpointStateMap.get(addr);
		if (localState == null) {
			HeartBeatState hbState = new HeartBeatState(generationNbr);
			localState = new EndpointState(hbState);
			localState.markAlive();
			endpointStateMap.put(addr, localState);
		}
	}
}
