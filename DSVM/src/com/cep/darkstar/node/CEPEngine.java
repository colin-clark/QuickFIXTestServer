package com.cep.darkstar.node;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.CassandraHelper;
import com.cep.commons.EventObject;
import com.cep.darkstar.node.configuration.DataType;
import com.cep.darkstar.node.configuration.YamlConfigInfo;
import com.cep.darkstar.query.ICEPEngine;
import com.cep.darkstar.dto.IDTO;
import com.cep.darkstar.events.MamaNBBO;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.util.exception.ConfigurationException;
import com.cep.metadata.ConnectionHelper;
import com.cep.metadata.MapEvent;
import com.cep.metadata.MetadataException;
import com.cep.metadata.MySQLConnectionHelper;
import com.cep.utils.Utils;
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.soda.StreamSelector;

@SuppressWarnings("unused")
public class CEPEngine implements ICEPEngine {
	private static Map<String,Map<String, Object>> fieldTypes = new HashMap<String,Map<String,Object>>();
	private final Configuration cepConfig = new Configuration();
	private final EPServiceProvider cepEngine;
	private static CEPEventsSendRunnable eventPumpThread = null;
	static private String nodeID = null;
	private static boolean mapEventsLoaded = false;
	private static boolean variantEventsLoaded = false;
	private static boolean MDStarted = false;
	private static BlockingQueue<EventObject> queue;
	private static HashMap<String,IDTO> DTOs = new HashMap<String,IDTO>();
	public static final String DEFAULT_DTO = "com.cep.darkstar.dto.DefaultDTO";
	private static Set<String> doubleFields = new HashSet<String>();
	private static Set<String> longFields = new HashSet<String>();
	private IDTO defaultDTO;
	public static final String DS_TIMESTAMP = "ds_timestamp";
	public static final String FUNCTION = "function";
	private static ConnectionHelper connectionHelper;
	
	public static void constructConnectionHelper(String classname, Map<String,String> params) throws ConfigurationException, com.cep.utils.ConfigurationException {
		Logger logger = Logger.getLogger("com.cep.darkstar.node.CEPEngine");
		logger.info("Constructing ConnectionHelper type " + classname);
		connectionHelper = Utils.construct(classname, "ConnectionHelper");
		logger.info("ConnectionHelper type " + classname + " constructed");
		connectionHelper.setParams(params);
	}
	
	public String nodeID() {
		return nodeID;
	}

	public Configuration getConfiguration() {
		return cepConfig;
	}

	public EPServiceProvider getEPServiceProvider() {
		return cepEngine;
	}

	public EPAdministrator getEPAdministrator() {
		return cepEngine.getEPAdministrator();
	}

	public EPRuntime getEPRuntime() {
		return cepEngine.getEPRuntime();
	}

	private void loadVariantEvents() {
		Logger logger = Logger.getLogger("com.cep.darkstar.node.CEPEngine");
		logger.info("Defining variant events.");
		try {
			List<String> eventNames = connectionHelper.getVariantEvents();
			for (String eventName : eventNames) {
				logger.info("Loading event "+eventName+" into engine."); 
				cepEngine.getEPAdministrator().createEPL("create variant schema "+eventName+" as *");
			}
		} catch (MetadataException e) {
			logger.fatal("Exception caught loading variant events", e);
		}
	}

	private void loadMapEvents(Configuration cepConfig) {
		Logger logger = Logger.getLogger("com.cep.darkstar.node.CEPEngine");
		logger.info("Defining MAP events.");
		try {
			List<MapEvent> mapEvents = connectionHelper.getMapEvents();
			for (MapEvent event : mapEvents) {
				String eventName = event.getEventName();
				logger.info("Loading event "+eventName+" into engine.");
				if (logger.isDebugEnabled()) {
					logger.debug("Fields: " + event.getFieldMap());
				}
				cepConfig.addEventType(eventName, event.getFieldMap());
			}
		} catch (MetadataException e) {
			logger.fatal("Exception caught loading map events", e);
		}		
		//TODO: These fields should be set dynamically while reading metadata, this is 
		//      just a shortcut for now.
		doubleFields.add("AvgPx");
	    doubleFields.add("Price");
	    doubleFields.add("LastPx");
	    CassandraHelper.setDoubleFields(doubleFields);
	    
	    longFields.add("LastShares");
	    longFields.add("CumQty");
	    longFields.add("Leaves");
	    longFields.add("OrderQty");
	    longFields.add("Side");
	    longFields.add("ds_timestamp");
	    CassandraHelper.setLongFields(longFields);
	    
		logger.info("MAP events successfully defined.");
	}
	
	public static boolean isDoubleField(String field) {
		return doubleFields.contains(field);
	}
	
	public static boolean isLongField(String field) {
		return longFields.contains(field);
	}
	
	public static DataType getFieldType(String eventType, String fieldName) {
		DataType returnType = DataType.STRING;
		Map<String,Object> fields = fieldTypes.get(eventType);
		if (fields != null) {
			Object type = fields.get(fieldName);
			if (type != null) {
				if(type == Long.class){
					returnType = DataType.LONG;
				} else if (type == Double.class) {
					returnType = DataType.DOUBLE;
				} else if (type == Integer.class) {
					returnType = DataType.INTEGER;
				}
			}
		}
		return returnType;
	}

	private void loadConfiguration(Configuration cepConfig) {
		// default to istream only
		cepConfig.getEngineDefaults().getStreamSelection().setDefaultStreamSelector(StreamSelector.ISTREAM_ONLY);
		//cepConfig.addPlugInView("SAX", "saxify", SAXPlugInViewFactory.class.getName());
		cepConfig.addImport("com.cep.imports.CEP");

		// turn on threading
		// set inbound threads to 2
		cepConfig.getEngineDefaults().getThreading().setThreadPoolInbound(true);
		cepConfig.getEngineDefaults().getThreading().setThreadPoolInboundNumThreads(2);
		// cepConfig.getEngineDefaults().getThreading().setThreadPoolInboundCapacity(10000);
		// set outbound threads to 2
		cepConfig.getEngineDefaults().getThreading().setThreadPoolOutbound(true);
		cepConfig.getEngineDefaults().getThreading().setThreadPoolOutboundNumThreads(2);
		// set event routing on with 2
		cepConfig.getEngineDefaults().getThreading().setThreadPoolRouteExec(true);
		cepConfig.getEngineDefaults().getThreading().setThreadPoolRouteExecNumThreads(2);
		cepConfig.getEngineDefaults().getThreading().setThreadPoolRouteExecCapacity(1000);
		// set external timer to 2 for windows
		cepConfig.getEngineDefaults().getThreading().setThreadPoolTimerExec(true);
		cepConfig.getEngineDefaults().getThreading().setThreadPoolTimerExecNumThreads(2);
		// cepConfig.getEngineDefaults().getThreading().setThreadPoolTimerExecNumThreads(1000);

		// load some pojo events
		cepConfig.addEventType(MamaNBBO.class);
		
		// If we are using external time disable internal timing in Esper
		if (DarkStarNode.isExternal_timing()) {
			cepConfig.getEngineDefaults().getThreading().setInternalTimerEnabled(false);
		}
	}

	public CEPEngine(String nodeID, YamlConfigInfo configInfo, BlockingQueue<EventObject> queue) {
		Logger dsLog = Logger.getLogger("com.cep.darkstar.node.CEPEngine");
		// who am I?
		CEPEngine.nodeID = nodeID;
		CEPEngine.queue = queue;
	

		if (configInfo != null) {
			loadConfiguration(cepConfig, configInfo);
		} else {
			loadConfiguration(cepConfig);
		}
		// load events up if not already defined
		if (mapEventsLoaded == false) {
			loadMapEvents(cepConfig);
			mapEventsLoaded = true;
		}

		// let's get a cep engine with configuration
		cepEngine = EPServiceProviderManager.getProvider("DarkStar", cepConfig);
		
		// load events up if not already defined
		if (variantEventsLoaded == false) {
			loadVariantEvents();
			variantEventsLoaded = true;
		}
		
		try {
			defaultDTO = Utils.construct(DEFAULT_DTO, "DTO");
			new Thread(defaultDTO).start();
		} catch (com.cep.utils.ConfigurationException e) {
			dsLog.error(e.getMessage(), e);
		}
		
		// start event pump if not already started
		if (eventPumpThread == null) {
			try {
				eventPumpThread = new CEPEventsSendRunnable(cepEngine, "EVENT", queue);
				eventPumpThread.start();
			} catch (IOException e) {
				dsLog.error(e.getMessage(), e);
			}
		}
	}

	private void loadConfiguration(Configuration cepConfig, YamlConfigInfo configInfo) {
		cepConfig.getEngineDefaults().getStreamSelection().setDefaultStreamSelector(StreamSelector.ISTREAM_ONLY);
		//cepConfig.addPlugInView("SAX", "saxify", SAXPlugInViewFactory.class.getName());
		cepConfig.addImport("com.cep.imports.CEP");
		
		cepConfig.getEngineDefaults().getThreading().setThreadPoolInbound(configInfo.isThread_pool_inbound());
		cepConfig.getEngineDefaults().getThreading().setThreadPoolInboundNumThreads(configInfo.getThread_pool_inbound_threads());
		cepConfig.getEngineDefaults().getThreading().setThreadPoolInboundCapacity(configInfo.getThread_pool_inbound_capacity());
		// set outbound threads to 2
		cepConfig.getEngineDefaults().getThreading().setThreadPoolOutbound(configInfo.isThread_pool_outbound());
		cepConfig.getEngineDefaults().getThreading().setThreadPoolOutboundNumThreads(configInfo.getThread_pool_outbound_threads());
		// set event routing on with 2
		cepConfig.getEngineDefaults().getThreading().setThreadPoolRouteExec(configInfo.isThread_pool_route_exec());
		cepConfig.getEngineDefaults().getThreading().setThreadPoolRouteExecNumThreads(configInfo.getThread_pool_route_exec_threads());
		cepConfig.getEngineDefaults().getThreading().setThreadPoolRouteExecCapacity(configInfo.getThread_pool_route_exec_capacity());
		// set external timer to 2 for windows
		cepConfig.getEngineDefaults().getThreading().setThreadPoolTimerExec(configInfo.isThread_pool_timer_exec());
		cepConfig.getEngineDefaults().getThreading().setThreadPoolTimerExecNumThreads(configInfo.getThread_pool_timer_exec_threads());

		// load some pojo events
		cepConfig.addEventType(MamaNBBO.class);
	}

	@Override
	public void handleEventObject(EventObject event) {
		Logger dsLog = Logger.getLogger("com.cep.darkstar.node.CEPEngine");
		try {
			if (!event.has(DS_TIMESTAMP)) {
				event.put(DS_TIMESTAMP, System.currentTimeMillis());
			}
		} catch (JSONException e1) {
			dsLog.error(e1.getMessage(), e1);
		}
		
		if (dsLog.isDebugEnabled()) {
			dsLog.debug("handling event object " + event);
		}
		String dtoName = DEFAULT_DTO;
		if (event.has(FUNCTION)) {
			try {
				dtoName = event.getString(FUNCTION);
				if (dtoName == null) { 
					dtoName = DEFAULT_DTO;
				}
			} catch (JSONException e) {
				dsLog.error(e.getMessage(), e);
			}
		}
		if (dsLog.isDebugEnabled()) {
			dsLog.debug("Loading DTO " + dtoName);
		}
		callDTO(dtoName, event);
	}

	private void callDTO(String dto, EventObject event) {
		Logger dsLog = Logger.getLogger("com.cep.darkstar.node.CEPEngine");
		try {
			if (dto.equals(DEFAULT_DTO)) {
				try {
					defaultDTO.getQueue().put(event);
				} catch (InterruptedException e) {
					dsLog.error(e.getMessage(), e);
				}
			} else {
				IDTO DTO = DTOs.get(dto);
				if (DTO == null) {
					synchronized(DTOs) {
						DTO = DTOs.get(dto);
						if (DTO == null) {
							DTO = Utils.construct(dto, "DTO");
							new Thread(DTO).start();
							DTOs.put(dto, DTO);
						}
					}
				}
				try {
					DTO.getQueue().put(event);
				} catch (InterruptedException e) {
					dsLog.error(e.getMessage(), e);
				}
			}
		} catch (com.cep.utils.ConfigurationException e) {
			dsLog.error(e.getMessage(), e);
		} 
	}

	@Override
	public void send(EventObject e) {
		CEPEventsSendRunnable.handleEventObject(e);
	}

}
