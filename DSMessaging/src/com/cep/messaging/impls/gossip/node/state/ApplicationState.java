package com.cep.messaging.impls.gossip.node.state;

public enum ApplicationState {
	STATUS, LOAD, SCHEMA, DC, RACK, RELEASE_VERSION,
	// pad to allow adding new states to existing cluster
	X1, X2, X3, X4, X5,
}
