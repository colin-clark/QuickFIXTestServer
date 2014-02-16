package com.cep.darkstar.onramp.configuration;

import me.prettyprint.cassandra.service.OperationType;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.HConsistencyLevel;

public class DefaultConsistencyLevel implements ConsistencyLevelPolicy {

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
		return HConsistencyLevel.TWO;
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
