package com.cep.darkstar.utility.cassandra;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.onramp.configuration.DefaultConsistencyLevel;

public class CassandraMessageReaderTest {
	final static Logger logger = Logger.getLogger("com.cep.darkstar.utility.cassandra.CassandraMessageReader");
	@SuppressWarnings("unused")
	private MessageReaderCallback callback;
	
	// Configurable parameters
	private String cassandraNode;
	private int cassandraPort;
	private String clusterName;
	private String keyspace;
	private String partition_on;
	private String cf_name;
	private int message_count;
	private long startTime;
	private long endTime;
	
	private Cluster cluster;
	private Keyspace keySpace;
	private StringSerializer se = StringSerializer.get();
	private LongSerializer le = LongSerializer.get();
	private final ConsistencyLevelPolicy policy = new DefaultConsistencyLevel();
	public static final String PARTITION_ON = "partition_on";
	public static final String DS_TIMESTAMP = "_ds_timestamp";
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
	private long totalRead = 0;
	
	private static Set<String> longTypes = new HashSet<String>();
	
	static {
		longTypes.add("ds_timestamp");
		longTypes.add("OrderQty");
		longTypes.add("Price");
	}

	public CassandraMessageReaderTest(CassandraClientConfigInfo info) {
		logger.info("Creating Cassandra Message Reader");
		cassandraNode = info.getCassandraNode();
		cassandraPort = info.getCassandraPort();
		clusterName = info.getCassandraClusterName();
		keyspace = info.getCassandraKeyspace();
		partition_on = info.getPartition_on();
		cf_name = info.getCf_name();
		message_count = info.getMessage_count();
		if (message_count == 0) {
			message_count = 10000;
		}
		if (info.getStartTime() == null) {
			startTime = 0;
		} else try {
			startTime = format.parse(info.getStartTime()).getTime();
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			startTime = 0;
		}
		if (info.getEndTime() == null) {
			endTime = System.currentTimeMillis();
		} else try {
			endTime = format.parse(info.getEndTime()).getTime();
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			endTime = System.currentTimeMillis();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("MessageReader params:");
			logger.debug("Cassandra Node: " + cassandraNode);
			logger.debug("Cassandra Port: " + cassandraPort);
			logger.debug("Cluster Name: " + clusterName);
			logger.debug("Keyspace: " + keyspace);
			logger.debug("Column Family: " + cf_name);
			logger.debug("Max Message Count: " + message_count);
			logger.debug("Start Time: " + startTime);
			logger.debug("End Time: " + endTime);
		}
	}

	public void read(MessageReaderCallback callback) {
		this.callback = callback;
		/*
		 * Get a connection to Cassandra (as specified in config)
		 * For each entry in specified column family:
		 * 		Send message to callback
		 */
		logger.info("Getting connection to Cassandra Node at " + cassandraNode+":"+cassandraPort);
		CassandraHostConfigurator hostConfig = 
			new CassandraHostConfigurator(cassandraNode+":"+String.valueOf(cassandraPort));
		cluster = HFactory.createCluster(clusterName, hostConfig);
		keySpace = HFactory.createKeyspace(keyspace, cluster, policy);

		// Retrieve the rows from the specified column family
		RangeSlicesQuery<String, String, String> rangeSlicesQuery = HFactory.createRangeSlicesQuery(keySpace, se, se, se);
		rangeSlicesQuery.setColumnFamily(cf_name);
		rangeSlicesQuery.setColumnNames("Firm");
		rangeSlicesQuery.setRowCount(message_count);
		String last_key = null;
		
		while (true) {
			rangeSlicesQuery.setKeys(last_key, null);
		    QueryResult<OrderedRows<String, String, String>> result = rangeSlicesQuery.execute();
		    OrderedRows<String, String, String> orderedRows = result.get();
		    
		    // tell the world where we are
		    logger.info("Rows returned from query: " + orderedRows.getCount() + " Total read so far: " + totalRead);
		    
		    // get last rowkey for next beginning rowkey
            Row<String,String,String> lastRow = orderedRows.peekLast();
            last_key = lastRow.getKey();
		    
            // are we done yet?
		    if (orderedRows.getCount() < message_count) break;
		    totalRead += orderedRows.getCount();
		}
	    logger.info("Rows read: " + totalRead);
	    callback.finished();
	}

	@SuppressWarnings("unused")
	private EventObject convertRowToEvent(List<HColumn<String,String>> columns, long readTotal) {
		if (logger.isDebugEnabled()) {
			logger.debug("Converting row to event");
		}
		EventObject event = new EventObject();
			try {
				event.put("RowCount", readTotal);
				for (HColumn<String,String> column : columns) {
					String name = column.getName();
					if (name.equalsIgnoreCase(DS_TIMESTAMP)) {
						if (!(isGoodTime(column.getValue()))) {
							if (logger.isDebugEnabled()) {
								logger.debug("Returning NULL for bad time");
							}
							return null;
						}
					}
					if (longTypes.contains(name)) {
						ByteBuffer valueBuffer = column.getValueBytes();
						long value = le.fromByteBuffer(valueBuffer);
						event.put(name, value);
					} else {
						event.put(name, column.getValue());
					}
					if (name.equalsIgnoreCase(partition_on)) {
						event.put(PARTITION_ON, column.getValue());
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return event;
	}

	private boolean isGoodTime(String value) {
		long timestamp = Long.valueOf(value);
		return (timestamp > startTime && timestamp < endTime);
	}

}
