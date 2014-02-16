package com.cep.simpleclient;

import static me.prettyprint.hector.api.factory.HFactory.createColumn;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;

import org.apache.log4j.Logger;
import org.json.JSONException;

import quickfix.FieldNotFound;
import quickfix.field.Account;
import quickfix.field.OrdType;
import quickfix.field.Price;

import com.cep.commons.EventObject;
import com.cep.quickfix.client.util.DefaultConsistencyLevel;

public class FIXMsgsToDarkstar {
	final static Logger logger = Logger.getLogger("com.cep.simpleclient.FIXMsgsToDarkstar");
	// Must be format 192.168.0.14:9159
	protected final String darkStarNode;
	protected final String clusterName;
	protected String eventName;
	protected final String firm;
	protected long batch_size = 1000;
	
	// Client interface vars
	protected Mutator<ByteBuffer> mutator;
	protected Cluster cluster;
	protected Keyspace keyspace;
	protected CassandraHostConfigurator hostConfig;

	protected StringSerializer se = StringSerializer.get();
	protected LongSerializer ls = LongSerializer.get();
	protected ByteBufferSerializer bfs = ByteBufferSerializer.get();
	protected BytesArraySerializer bas = BytesArraySerializer.get();
	protected final ConsistencyLevelPolicy policy = new DefaultConsistencyLevel();
	
	private static SimpleDateFormat shortFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
	
	protected long count = 0;
	
	public FIXMsgsToDarkstar(String darkStarNode, String clusterName, String firm, long batch_size) {
		this.darkStarNode = darkStarNode;
		this.clusterName = clusterName;
		this.firm = firm;
		this.batch_size = batch_size;
		init();
	}
	
	public FIXMsgsToDarkstar(String darkStarNode, String clusterName, String firm) {
		this.darkStarNode = darkStarNode;
		this.clusterName = clusterName;
		this.firm = firm;
		init();
	}

	private synchronized void init() {
		hostConfig = new CassandraHostConfigurator(darkStarNode);
		cluster = HFactory.createCluster(clusterName, hostConfig);
		keyspace = HFactory.createKeyspace("system", cluster, policy);
		mutator = HFactory.createMutator(keyspace, bfs);
	}
	
	public void onMessage(quickfix.fix42.NewOrderSingle message) {
		try {
			EventObject anEvent = new EventObject();	
			anEvent.put("_onrcvTS", System.currentTimeMillis());			
			anEvent.put("SenderCompID", "FromSC");
			anEvent.put("TargetCompID", "ToSC");
			anEvent.put("MsgType", "D");			
			anEvent.put("ClOrdID", String.valueOf(message.getInt(11)));
			anEvent.put("Side", message.getInt(54));
			anEvent.put("Symbol", message.getString(55));
			anEvent.put("OrderQty", message.getInt(38));
			anEvent.put("TimeInForce", String.valueOf(message.getInt(59)));
			if (message.isSetPrice()) {
				anEvent.put("Price", message.getDouble(Price.FIELD));
			}
			if (message.isSetAccount()) {
				anEvent.put("Account", message.getString(Account.FIELD));
			}
			if (message.isSetOrdType()) {
				anEvent.put("OrdType", String.valueOf(message.getChar(OrdType.FIELD)));
			}
			if (message.isSetTransactTime()) {
				Date date = message.getTransactTime().getValue();
				String dateStr = shortFormat.format(date);
				anEvent.put("TransactTime", dateStr);
			}
			anEvent.put("Firm", firm);
			anEvent.put("partition_on", message.getString(55));
			anEvent.setEventName("FIXNewOrderSingle");
			if (logger.isDebugEnabled()) {
				logger.debug(anEvent);
			}
			send(anEvent);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (FieldNotFound e) {
			e.printStackTrace();
		}
	}
	
    protected void send(EventObject event) throws JSONException {
    	boolean doExecute = (++count % batch_size == 0); 
		ByteBuffer rowKey = se.toByteBuffer(event.getString("partition_on"));
		if (doExecute) {
			event.put("_onsndTS", System.currentTimeMillis());
		}
		mutator.addInsertion(rowKey, "system", createColumn(event.getEventName(), event.toString(), se, se));
		if  (doExecute){
			logger.info("Mutator is executing on message number " + count);
			mutator.execute();
		}
	}
}

