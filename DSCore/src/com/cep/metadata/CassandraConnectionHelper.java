package com.cep.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.EntityField;
import com.cep.utils.ConfigurationException;

import me.prettyprint.cassandra.model.CqlQuery;
import me.prettyprint.cassandra.model.CqlRows;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;

public class CassandraConnectionHelper implements ConnectionHelper {
	private static Logger logger = Logger.getLogger("com.cep.utils.CassandraConnectionHelper");

	private String node;
	private int port = 9160;
	private String clusterName;
	private String keyspace;
	
	private StringSerializer se = StringSerializer.get();
	private LongSerializer le = LongSerializer.get();
	
	private Cluster cluster;
	private Keyspace keySpace;
	private ConsistencyLevelPolicy policy;
	
	private static final String NODE = "node";
	private static final String PORT = "port";
	private static final String CLUSTER = "cluster";
	private static final String KEYSPACE = "keyspace";
	private static final String STRING = "String";
	private static final String LONG = "Long";
	private static final String DOUBLE = "Double";
	private static final String INTEGER = "Integer";
	
	public CassandraConnectionHelper() {
	}
	
	@Override
	public List<MapEvent> getMapEvents() {
		List<MapEvent> mapEvents = new ArrayList<MapEvent>();
		
		CqlQuery<String,String,String> cqlQuery = new CqlQuery<String,String,String>(keySpace, se, se, se);
		cqlQuery.setQuery("SELECT entity_name FROM query_entity WHERE entity_type = 'MAP'");
		QueryResult<CqlRows<String,String,String>> result = cqlQuery.execute();
		CqlRows<String,String,String> results = result.get();
		if (results != null) {
			for (Row<String, String, String> row : results) {
				String eventName = row.getColumnSlice().getColumnByName("entity_name").getValue();
				MapEvent event = new MapEvent();
				event.setEventName(eventName);
	
				CqlQuery<String,String,String> cqlQuery2 = new CqlQuery<String,String,String>(keySpace, se, se, se);
				cqlQuery2.setQuery("SELECT field_name, field_class FROM query_entity_fields WHERE field_definition = '"+eventName+"'");
				QueryResult<CqlRows<String,String,String>> result2 = cqlQuery2.execute();
				if (result2 != null) {
					for (Row<String, String, String> fieldRow : result2.get()) {
						String name = fieldRow.getColumnSlice().getColumnByName("field_name").getValue();
						String type = fieldRow.getColumnSlice().getColumnByName("field_class").getValue();
						if (type.equals(STRING)) {
							event.addField(name, String.class);
						} else if (type.equals(LONG)) {
							event.addField(name, Long.class);
						} else if (type.equals(DOUBLE)) {
							event.addField(name, Double.class);
						} else if (type.equals(INTEGER)) {
							event.addField(name, Integer.class);
						}
						event.addField("event_name", String.class);
						mapEvents.add(event);
					}
				}
			}
		}
		return mapEvents;
	}

	@Override
	public List<String> getVariantEvents() {
		List<String> variantEvents = new ArrayList<String>();
		
		CqlQuery<String,String,String> cqlQuery = new CqlQuery<String,String,String>(keySpace, se, se, se);
		cqlQuery.setQuery("SELECT entity_name FROM query_entity WHERE entity_type = 'VARIANT'");
		QueryResult<CqlRows<String,String,String>> result = cqlQuery.execute();
		CqlRows<String,String,String> results = result.get();
		if (results != null) {
			for (Row<String, String, String> row : results) {
				variantEvents.add(row.getColumnSlice().getColumnByName("entity_name").getValue());
			}
		}
		return variantEvents;
	}

	@Override
	public void setParams(Map<String, String> params) throws ConfigurationException {
		if(params.containsKey(NODE)) {
			this.node = (params.get(NODE));
			if (logger.isDebugEnabled()) {
				logger.debug("node" + params.get(NODE));
			}
		} else {
			throw new ConfigurationException("Parameter node must be specified");
		}
		if(params.containsKey(CLUSTER)) {
			this.clusterName = (params.get(CLUSTER));
			if (logger.isDebugEnabled()) {
				logger.debug("Cluster name set to " + params.get(CLUSTER));
			}
		} else {
			throw new ConfigurationException("Parameter password must be specified");
		}
		if(params.containsKey(KEYSPACE)) {
			this.keyspace = (params.get(KEYSPACE));
			if (logger.isDebugEnabled()) {
				logger.debug("Keyspace set to " + params.get(KEYSPACE));
			}
		} else {
			throw new ConfigurationException("Parameter keyspace must be specified");
		}

		port = (params.containsKey(PORT)) ? Integer.valueOf(params.get(PORT)) : 9160;
		if (logger.isDebugEnabled()) {
			logger.debug("port set to " + params.get(PORT));
		}

		CassandraHostConfigurator hostConfig = new CassandraHostConfigurator(node+":"+String.valueOf(port));
		cluster = HFactory.createCluster(clusterName, hostConfig);
		policy = HFactory.createDefaultConsistencyLevelPolicy();
		keySpace = HFactory.createKeyspace(keyspace, cluster, policy);
		
		logger.info("Connection to Cassandra established");
	}

	@Override
	public QueryEntity[] getQueryEntities() {
		List<QueryEntity> list = new ArrayList<QueryEntity>();
		QueryEntity[] queryEntities;
		CqlQuery<String,String,String> cqlQuery = new CqlQuery<String,String,String>(keySpace, se, se, se);
		cqlQuery.setQuery("SELECT entity_name, entity_description, entity_query_key, entity_field_definition FROM query_entity");
		QueryResult<CqlRows<String,String,String>> result = cqlQuery.execute();
		CqlRows<String,String,String> results = result.get();
		if (results != null) {
			for (Row<String, String, String> row : results) {
				try {
					list.add(new QueryEntity(row.getColumnSlice().getColumnByName("entity_name").getValue(),
							row.getColumnSlice().getColumnByName("entity_description").getValue(),
							row.getColumnSlice().getColumnByName("entity_query_key").getValue(),
							row.getColumnSlice().getColumnByName("entity_field_definition").getValue()));
				} catch (JSONException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		queryEntities = new QueryEntity[list.size()];
		queryEntities = list.toArray(queryEntities);
		
		return queryEntities;
	}

	@Override
	public EntityField[] getQueryEntityFields(String entityFieldDefinition) {
		List<EntityField> list = new ArrayList<EntityField>();
		EntityField[] queryEntityFields;
		CqlQuery<String,String,String> cqlQuery = new CqlQuery<String,String,String>(keySpace, se, se, se);
		cqlQuery.setQuery("SELECT field_name, field_type FROM query_entity_fields WHERE field_definition='" + entityFieldDefinition + "'");
		QueryResult<CqlRows<String,String,String>> result = cqlQuery.execute();
		CqlRows<String,String,String> results = result.get();
		if (results != null) {
			for (Row<String, String, String> row : results) {
				CqlQuery<String,String,Long> lengthQuery = new CqlQuery<String,String,Long>(keySpace, se, se, le);
				lengthQuery.setQuery("SELECT field_length FROM query_entity_fields WHERE field_definition='" + entityFieldDefinition + "' " +
						"and field_name='" + row.getColumnSlice().getColumnByName("field_name").getValue() + "'");
				QueryResult<CqlRows<String,String,Long>> lengthResult = lengthQuery.execute();
				long length = lengthResult.get().iterator().next().getColumnSlice().getColumnByName("field_length").getValue();
				list.add(new EntityField(
						row.getColumnSlice().getColumnByName("field_name").getValue(),
						"FIELD DESCRIPTION",
						row.getColumnSlice().getColumnByName("field_type").getValue(),
						(int) length ));
			}
		}
		queryEntityFields = new EntityField[list.size()];
		queryEntityFields = list.toArray(queryEntityFields);
		
		return queryEntityFields;
	}

}
