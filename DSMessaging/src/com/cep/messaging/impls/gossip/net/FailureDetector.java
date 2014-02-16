package com.cep.messaging.impls.gossip.net;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.node.Gossiper;
import com.cep.messaging.impls.gossip.node.state.ApplicationState;
import com.cep.messaging.impls.gossip.node.state.EndpointState;
import com.cep.messaging.impls.gossip.node.state.VersionedValue;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.util.collection.BoundedStatsDeque;

/**
 * This FailureDetector is an implementation of the paper titled
 * "The Phi Accrual Failure Detector" by Hayashibara. Check the paper and the
 * <i>IFailureDetector</i> interface for details.
 */
public class FailureDetector implements IFailureDetector {
	public static final IFailureDetector instance = new FailureDetector();
	private static Logger logger_ = LoggerFactory.getLogger(FailureDetector.class);
	private static final int sampleSize_ = 1000;
	private static int phiConvictThreshold_;

	private Map<InetAddress, ArrivalWindow> arrivalSamples_ = new Hashtable<InetAddress, ArrivalWindow>();
	private List<IFailureDetectionEventListener> fdEvntListeners_ = new ArrayList<IFailureDetectionEventListener>();

	public FailureDetector() {
		phiConvictThreshold_ = NodeDescriptor.getPhiConvictThreshold();
	}

	public String getAllEndpointStates() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<InetAddress, EndpointState> entry : Gossiper.instance.endpointStateMap.entrySet()) {
			sb.append(entry.getKey()).append("\n");
			for (Map.Entry<ApplicationState, VersionedValue> state : entry.getValue().applicationState.entrySet()) {
				sb.append("  ").append(state.getKey()).append(":").append(state.getValue().value).append("\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Dump the inter arrival times for examination if necessary.
	 */
	public void dumpInterArrivalTimes() {
		OutputStream os = null;
		try {
			File file = File.createTempFile("failuredetector-", ".dat");
			os = new BufferedOutputStream(new FileOutputStream(file, true));
			os.write(toString().getBytes());
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	public void setPhiConvictThreshold(int phi) {
		phiConvictThreshold_ = phi;
	}

	public int getPhiConvictThreshold() {
		return phiConvictThreshold_;
	}

	public boolean isAlive(InetAddress ep) {
		if (ep.equals(GossipUtilities.getLocalAddress())) {
			return true;
		}
		EndpointState epState = Gossiper.instance.getEndpointStateForEndpoint(ep);
		if (epState == null) {
			logger_.error("unknown endpoint " + ep);
		}
		return epState != null && epState.isAlive();
	}

	public void report(InetAddress ep) {
		if (logger_.isTraceEnabled()) {
			logger_.trace("reporting " + ep);
		}
		long now = System.currentTimeMillis();
		ArrivalWindow heartbeatWindow = arrivalSamples_.get(ep);
		if (heartbeatWindow == null) {
			heartbeatWindow = new ArrivalWindow(sampleSize_);
			arrivalSamples_.put(ep, heartbeatWindow);
		}
		heartbeatWindow.add(now);
	} 

	public void interpret(InetAddress ep) {
		ArrivalWindow hbWnd = arrivalSamples_.get(ep);
		if (hbWnd == null) {
			return;
		}
		long now = System.currentTimeMillis();
		double phi = hbWnd.phi(now);
		if (logger_.isTraceEnabled()) {
			logger_.trace("PHI for " + ep + " : " + phi);
		}
		if (phi > phiConvictThreshold_) {
			for (IFailureDetectionEventListener listener : fdEvntListeners_) {
				listener.convict(ep);
			}
		}
	}

	public void remove(InetAddress ep) {
		arrivalSamples_.remove(ep);
	}

	public void registerFailureDetectionEventListener(IFailureDetectionEventListener listener) {
		fdEvntListeners_.add(listener);
	}

	public void unregisterFailureDetectionEventListener(IFailureDetectionEventListener listener) {
		fdEvntListeners_.remove(listener);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		Set<InetAddress> eps = arrivalSamples_.keySet();

		sb.append("-----------------------------------------------------------------------");
		for (InetAddress ep : eps) {
			ArrivalWindow hWnd = arrivalSamples_.get(ep);
			sb.append(ep + " : ");
			sb.append(hWnd.toString());
			sb.append(System.getProperty("line.separator"));
		}
		sb.append("-----------------------------------------------------------------------");
		return sb.toString();
	}

	public static void main(String[] args) throws Throwable {
	}
}

class ArrivalWindow {
	@SuppressWarnings("unused")
	private static Logger logger_ = LoggerFactory.getLogger(ArrivalWindow.class);
	private double tLast_ = 0L;
	private BoundedStatsDeque arrivalIntervals_;

	// this is useless except to provide backwards compatibility in phi_convict_threshold,
	// because everyone seems pretty accustomed to the default of 8, and users who have
	// already tuned their phi_convict_threshold for their own environments won't need to change.
	private final double PHI_FACTOR = 1.0 / Math.log(10.0);

	ArrivalWindow(int size) {
		arrivalIntervals_ = new BoundedStatsDeque(size);
	}

	synchronized void add(double value) {
		double interArrivalTime;
		if (tLast_ > 0L) {
			interArrivalTime = (value - tLast_);
		} else {
			interArrivalTime = Gossiper.intervalInMillis / 2;
		}
		tLast_ = value;
		arrivalIntervals_.add(interArrivalTime);
	}

	synchronized double sum() {
		return arrivalIntervals_.sum();
	}

	synchronized double sumOfDeviations() {
		return arrivalIntervals_.sumOfDeviations();
	}

	synchronized double mean() {
		return arrivalIntervals_.mean();
	}

	synchronized double variance() {
		return arrivalIntervals_.variance();
	}

	double stdev() {
		return arrivalIntervals_.stdev();
	}

	void clear() {
		arrivalIntervals_.clear();
	}

	double phi(long tnow) {
		int size = arrivalIntervals_.size();
		double t = tnow - tLast_;
		return (size > 0) ? PHI_FACTOR * t / mean() : 0.0;
	}

	public String toString() {
		return StringUtils.join(arrivalIntervals_.iterator(), " ");
	}
}
