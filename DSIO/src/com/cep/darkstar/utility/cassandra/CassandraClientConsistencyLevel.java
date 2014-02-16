package com.cep.darkstar.utility.cassandra;

import me.prettyprint.cassandra.service.OperationType;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.HConsistencyLevel;

public class CassandraClientConsistencyLevel implements ConsistencyLevelPolicy {

	@Override
	public HConsistencyLevel get(OperationType op, String arg1) {
		switch(op) {
			case READ: return HConsistencyLevel.ALL;
			case WRITE: return HConsistencyLevel.ANY;
		}
		return HConsistencyLevel.ONE;
	}

	@Override
	public HConsistencyLevel get(OperationType op) {
		switch(op) {
			case READ: return HConsistencyLevel.ALL;
			case WRITE: return HConsistencyLevel.ANY;
		}
		return HConsistencyLevel.ONE;
	}
}