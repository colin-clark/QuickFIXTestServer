package com.cep.quickfix.server.impls;

import static me.prettyprint.hector.api.factory.HFactory.createColumn;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

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

import org.json.JSONException;

import quickfix.ConfigError;
import quickfix.DataDictionaryProvider;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.FixVersions;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogUtil;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.field.Account;
import quickfix.field.ApplVerID;
import quickfix.field.AvgPx;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastShares;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.fix42.OrderCancelRequest;

import com.cep.commons.EventObject;
import com.cep.quickfix.client.util.DefaultConsistencyLevel;
import com.cep.quickfix.server.interfaces.MarketDataProvider;

public class QFJServerApplication extends quickfix.MessageCracker implements quickfix.Application {
	// static constants
	public static final String HOSTNAME = "hostname";
	public static final String CLUSTER_NAME = "clustername";
	private static final String DEFAULT_MARKET_PRICE_KEY = "DefaultMarketPrice";
	private static final String ALWAYS_FILL_LIMIT_KEY = "AlwaysFillLimitOrders";
	private static final String VALID_ORDER_TYPES_KEY = "ValidOrderTypes";
	private static SimpleDateFormat shortFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");

	private final boolean alwaysFillLimitOrders;
	private final HashSet<String> validOrderTypes = new HashSet<String>();
	private MarketDataProvider marketDataProvider;

	private static final String PERCENT_NO_ACK = "PercentNoAck";
	private static final String PERCENT_BUST = "PercentBust";
	private long percentNoAck;
	private long percentBust;
	private String firm = "CEP";

	Random generator = new Random();
	
	// Must be format 192.168.0.14:9159
	protected String darkStarNode;
	protected String clusterName;
	protected String eventName;
	protected long batch_size = 1000;
	protected long count = 0;
	
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

	public QFJServerApplication(SessionSettings settings) throws ConfigError, FieldConvertError {
		initializeValidOrderTypes(settings);
		initializeMarketDataProvider(settings);
		if (settings.isSetting("batch_size")) {
			batch_size = settings.getLong("batch_size");
		}
		if (settings.isSetting("Firm")) {
			firm = settings.getString("Firm");
		}
		if (settings.isSetting(ALWAYS_FILL_LIMIT_KEY)) {
			alwaysFillLimitOrders = settings.getBool(ALWAYS_FILL_LIMIT_KEY);
		} else {
			alwaysFillLimitOrders = false;
		}
		if (settings.isSetting(PERCENT_NO_ACK)) {
			percentNoAck = settings.getLong(PERCENT_NO_ACK);
		} else {
			percentNoAck = 10;
		}
		if (settings.isSetting(PERCENT_BUST)) {
			percentBust = settings.getLong(PERCENT_BUST);
		} else {
			percentBust = 10;
		}
		if (settings.isSetting(HOSTNAME)) {
        	try {
        		darkStarNode = settings.getString(HOSTNAME);
			} catch (Exception e) {
				System.err.println("Exception caught setting darkStarNode, using default of localhost");
				darkStarNode = "localhost";
			} 
        }
        if (settings.isSetting(CLUSTER_NAME)) {
        	try {
        		clusterName = settings.getString(CLUSTER_NAME);
			} catch (Exception e) {
				System.err.println("Exception caught setting clustername, using default of DarkStarCluster");
				clusterName = "DarkStarCluster";
			} 
        }
        init();
	}

	private void initializeMarketDataProvider(SessionSettings settings) throws ConfigError, FieldConvertError {
		if (settings.isSetting(DEFAULT_MARKET_PRICE_KEY)) {
			if (marketDataProvider == null) {
				final double defaultMarketPrice = settings.getDouble(DEFAULT_MARKET_PRICE_KEY);
				marketDataProvider = new MarketDataProvider() {
					public double getAsk(String symbol) {
						return (defaultMarketPrice + 0.1);
					}

					public double getBid(String symbol) {
						return (defaultMarketPrice - 1);
					}
				};
			} else {
				System.out.println("Ignoring " + DEFAULT_MARKET_PRICE_KEY + " since provider is already defined.");
			}
		}
	}

	private void initializeValidOrderTypes(SessionSettings settings)
			throws ConfigError, FieldConvertError {
		if (settings.isSetting(VALID_ORDER_TYPES_KEY)) {
			List<String> orderTypes = 
					Arrays.asList(settings.getString(VALID_ORDER_TYPES_KEY).trim().split("\\s*,\\s*"));
			validOrderTypes.addAll(orderTypes);
		} else {
			validOrderTypes.add(OrdType.LIMIT + "");
		}
	}

	public void onCreate(SessionID sessionID) {
		Session.lookupSession(sessionID).getLog().onEvent("Valid order types: " + validOrderTypes);
	}

	public void onLogon(SessionID sessionID) {
		System.out.println("Session " + sessionID + " logged on");
	}

	public void onLogout(SessionID sessionID) {
		System.out.println("Session " + sessionID + " logged out");
	}

	public void toAdmin(quickfix.Message message, SessionID sessionID) {
	}

	public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {
	}

	public void fromAdmin(quickfix.Message message, SessionID sessionID)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
	}

	public void fromApp(quickfix.Message message, SessionID sessionID)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		crack(message, sessionID);
	}

 
	final ExecTransType CancelExecTransType = new ExecTransType(ExecTransType.CANCEL);
	final OrdStatus FilledOrdStatus = new OrdStatus(OrdStatus.FILLED);
	final OrdStatus CancelledOrdStatus = new OrdStatus(OrdStatus.CANCELED);

	private boolean isOrderExecutable(Message order, Price price) throws FieldNotFound {
		if (order.getChar(OrdType.FIELD) == OrdType.LIMIT) {
			BigDecimal limitPrice = new BigDecimal(order.getString(Price.FIELD));
			char side = order.getChar(Side.FIELD);
			BigDecimal thePrice = new BigDecimal("" + price.getValue());

			return (side == Side.BUY && thePrice.compareTo(limitPrice) <= 0)
					|| ((side == Side.SELL || side == Side.SELL_SHORT) && thePrice.compareTo(limitPrice) >= 0);
		}
		return true;
	}

	private Price getPrice(Message message) throws FieldNotFound {
		Price price;
		if (message.getChar(OrdType.FIELD) == OrdType.LIMIT && alwaysFillLimitOrders) {
			price = new Price(message.getDouble(Price.FIELD));
		} else {
			if (marketDataProvider == null) {
				throw new RuntimeException("No market data provider specified for market order");
			}
			char side = message.getChar(Side.FIELD);
			if (side == Side.BUY) {
				price = new Price(marketDataProvider.getAsk(message.getString(Symbol.FIELD)));
			} else if (side == Side.SELL || side == Side.SELL_SHORT) {
				price = new Price(marketDataProvider.getBid(message.getString(Symbol.FIELD)));
			} else {
				throw new RuntimeException("Invalid order side: " + side);
			}
		}
		return price;
	}

	private void sendMessage(SessionID sessionID, Message message) {
		try {
			Session session = Session.lookupSession(sessionID);
			if (session == null) {
				throw new SessionNotFound(sessionID.toString());
			}

			DataDictionaryProvider dataDictionaryProvider = session.getDataDictionaryProvider();
			if (dataDictionaryProvider != null) {
				try {
					dataDictionaryProvider
						.getApplicationDataDictionary(getApplVerID(session, message)).validate(message, true);
				} catch (Exception e) {
					LogUtil.logThrowable(sessionID, "Outgoing message failed validation: "
									+ e.getMessage(), e);
					return;
				}
			}
			System.out.println("Sending Message:\n" + message);
			session.send(message);
		} catch (SessionNotFound e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private ApplVerID getApplVerID(Session session, Message message) {
		String beginString = session.getSessionID().getBeginString();
		if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
			return new ApplVerID(ApplVerID.FIX50);
		} else {
			return MessageUtils.toApplVerID(beginString);
		}
	}
	final ExecTransType NewExecTransType = new ExecTransType(ExecTransType.NEW);
	final OrdStatus NewOrdStatus = new OrdStatus(OrdStatus.NEW);
	final LastShares NoLastShares = new LastShares(0);
	final LastPx NoLastPx = new LastPx(0);
	final CumQty NoCumQty = new CumQty(0);
	final AvgPx NoAvgPx = new AvgPx(0);
	final ExecType FillExecType = new ExecType(ExecType.FILL);
	final LeavesQty NoLeavesQty = new LeavesQty(0);
	final Symbol noSymbol = new Symbol();
	final Side noSide = new Side();
	final OrderID noOrderID = new OrderID();
	final ExecID noExecID = new ExecID();
	final ExecType CancelledExecType = new ExecType(ExecType.CANCELED);
	
	// create stub ack execution report to reduce overhead of creating object each time
	quickfix.fix42.ExecutionReport accept = new quickfix.fix42.ExecutionReport(noOrderID,
			noExecID, NewExecTransType, FillExecType,
			NewOrdStatus, noSymbol, noSide,
			NoLeavesQty, NoCumQty, NoAvgPx);
	
	// create stub execution report ...
	quickfix.fix42.ExecutionReport executionReport = new quickfix.fix42.ExecutionReport(noOrderID,
			noExecID, NewExecTransType, FillExecType,
			NewOrdStatus, noSymbol, noSide,
			NoLeavesQty, NoCumQty, NoAvgPx);
	// creaet stub bust ...
	quickfix.fix42.ExecutionReport bust = null;
	quickfix.fix42.ExecutionReport cancelConfirmation = null;

	private synchronized void init() {
		hostConfig = new CassandraHostConfigurator(darkStarNode);
		hostConfig.setAutoDiscoveryDelayInSeconds(1);
		hostConfig.setAutoDiscoverHosts(true);
		cluster = HFactory.createCluster(clusterName, hostConfig);
		keyspace = HFactory.createKeyspace("system", cluster, policy);
		mutator = HFactory.createMutator(keyspace, bfs);
	}

	public void messageToRabbitMQ(quickfix.fix42.NewOrderSingle message, SessionID sessionID) {
		try {
			System.out.println("Received New Order:\n" + message);
			EventObject anEvent = new EventObject();	
			// we want the symbol, shares, side, etc.
			// we need to add a toJSON to this so that we just output an EventObject
			anEvent.put("TargetCompID", sessionID.getTargetCompID());
			anEvent.put("SenderCompID", sessionID.getSenderCompID());
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
			Date date;
			if (message.isSetTransactTime()) {
				date = message.getTransactTime().getValue();
			} else {
				date = new Date();
			}
			String dateStr = shortFormat.format(date);
			anEvent.put("TransactTime", dateStr);
			anEvent.put("Firm", firm);
			anEvent.put("partition_on", message.getString(55));
			anEvent.setEventName("FIXNewOrderSingle");
			System.out.println("Sending Order to DarkStar");
			send(anEvent);
			System.out.println("Order sent to DarkStar");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (FieldNotFound e) {
			e.printStackTrace();
		}	
	}
	
	public void messageToRabbitMQ(quickfix.fix42.OrderCancelRequest message, SessionID sessionID) {
		try {
			System.out.println("Received Cancel Request:\n" + message);
			EventObject anEvent = new EventObject();	
			anEvent.put("TargetCompID", sessionID.getTargetCompID());
			anEvent.put("SenderCompID", sessionID.getSenderCompID());
			anEvent.put("ClOrdID", String.valueOf(message.getInt(11)));
			anEvent.put("Side", message.getInt(Side.FIELD));
			anEvent.put("Symbol", message.getString(Symbol.FIELD));
			anEvent.put("OrderQty", message.getInt(OrderQty.FIELD));
			if (message.isSetTransactTime()) {
				Date date = message.getTransactTime().getValue();
				String dateStr = shortFormat.format(date);
				anEvent.put("TransactTime", dateStr);
			}
			anEvent.put("Firm", firm);
			anEvent.put("partition_on", message.getString(Symbol.FIELD));
			anEvent.setEventName("FIXOrderCancel");
			System.out.println("Sending Cancel Request to DarkStar");
			send(anEvent);
			System.out.println("Cancel Request sent to DarkStar");
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
			event.put("_ds_timestamp", System.currentTimeMillis());
		}
		mutator.addInsertion(rowKey, "system", createColumn(event.getEventName(), event.toString(), se, se));
		if (doExecute) {
			mutator.execute();
		}
	}
    
    public void onMessage(OrderCancelRequest cancel, SessionID sessionID) 
    		throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
    	try {
			messageToRabbitMQ(cancel, sessionID);
			cancelConfirmation = new quickfix.fix42.ExecutionReport(
					genOrderID(),
					genExecID(), 
					CancelExecTransType,
					CancelledExecType, 
					CancelledOrdStatus,
					cancel.getSymbol(), 
					cancel.getSide(),
					NoLeavesQty, 
					new CumQty(cancel.getDouble(OrderQty.FIELD)),
					new AvgPx(0.0));
			cancelConfirmation.set(cancel.getClOrdID());
			cancelConfirmation.set(cancel.getOrderQty());
			cancelConfirmation.set(new LastShares(cancel.getOrderQty().getValue()));
			cancelConfirmation.set(new LastPx(0.0));
			sendMessage(sessionID, cancelConfirmation);
			cancelConfirmation = null;
		} catch (RuntimeException e) {
			LogUtil.logThrowable(sessionID, e.getMessage(), e);
		}
	}
	
	public void onMessage(quickfix.fix42.NewOrderSingle order,
			SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
		try {
			validateOrder(order);
			messageToRabbitMQ(order, sessionID);
			int randomIndex = generator.nextInt(100); // generates 0 - 99

			if (randomIndex >= percentNoAck) {

				OrderQty orderQty = order.getOrderQty();
				Price price = getPrice(order);

				// set fields & send accept
				accept.set(genOrderID());
				accept.set(genExecID());
				accept.set(order.getSymbol());
				accept.set(order.getSide());
				accept.set(order.getClOrdID());
				sendMessage(sessionID, accept);
				//accept = null;

				if (isOrderExecutable(order, price)) {
					// set fields & send execution report
					executionReport.set(genOrderID());
					executionReport.set(genExecID());
					executionReport.set(order.getSymbol());
					executionReport.set(order.getSide());
					executionReport.set(new CumQty(orderQty.getValue()));
					executionReport.set(new AvgPx(price.getValue()));
					executionReport.set(order.getClOrdID());
					executionReport.set(orderQty);
					executionReport.set(new LastShares(orderQty.getValue()));
					executionReport.set(new LastPx(price.getValue()));
					sendMessage(sessionID, executionReport);
					//executionReport = null;

					// send a bust
					randomIndex = generator.nextInt(100);
					if (randomIndex < percentBust) {
						bust = new quickfix.fix42.ExecutionReport(genOrderID(),
								genExecID(), CancelExecTransType,
								CancelledExecType, FilledOrdStatus,
								order.getSymbol(), order.getSide(),
								NoLeavesQty, new CumQty(orderQty.getValue()),
								new AvgPx(price.getValue()));
						bust.set(order.getClOrdID());
						bust.set(orderQty);
						bust.set(new LastShares(orderQty.getValue()));
						bust.set(new LastPx(price.getValue()));
						sendMessage(sessionID, bust);
						bust = null;
					}
				}
			}
			order = null;
		} catch (RuntimeException e) {
			LogUtil.logThrowable(sessionID, e.getMessage(), e);
		}
	}

	private void validateOrder(Message order) throws IncorrectTagValue, FieldNotFound {
		OrdType ordType = new OrdType(order.getChar(OrdType.FIELD));
		if (!validOrderTypes.contains(Character.toString(ordType.getValue()))) {
			System.err.println("Order type not in ValidOrderTypes setting");
			throw new IncorrectTagValue(ordType.getField());
		}
		if (ordType.getValue() == OrdType.MARKET && marketDataProvider == null) {
			System.err.println("DefaultMarketPrice setting not specified for market order");
			throw new IncorrectTagValue(ordType.getField());
		}
	}

	public OrderID genOrderID() {
		return new OrderID(Integer.valueOf(++m_orderID).toString());
	}

	public ExecID genExecID() {
		return new ExecID(Integer.valueOf(++m_execID).toString());
	}

	/**
	 * Allows a custom market data provider to be specified.
	 * 
	 * @param marketDataProvider
	 */
	public void setMarketDataProvider(MarketDataProvider marketDataProvider) {
		this.marketDataProvider = marketDataProvider;
	}

	private int m_orderID = 0;
	private int m_execID = 0;
}
