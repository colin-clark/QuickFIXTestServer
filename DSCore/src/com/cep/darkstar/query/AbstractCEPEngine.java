package com.cep.darkstar.query;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cep.metadata.ConnectionHelper;
import com.cep.metadata.MapEvent;
import com.cep.metadata.MetadataException;
import com.cep.utils.ConfigurationException;
import com.cep.utils.Utils;
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;

public abstract class AbstractCEPEngine {
	static Logger logger = Logger.getLogger("com.cep.darkstar.query.CEPEngine");
	
	protected final Configuration cepConfig = new Configuration();
	protected static EPServiceProvider cepEngine;
	protected static Thread eventPumpThread = null;
	protected static boolean mapEventsLoaded = false;
	protected static boolean variantEventsLoaded = false;
	protected static ConnectionHelper connectionHelper;
	
	public static void constructConnectionHelper(String classname, Map<String,String> params) throws ConfigurationException {
		logger.info("Constructing ConnectionHelper type " + classname);
		connectionHelper = Utils.construct(classname, "ConnectionHelper");
		logger.info("ConnectionHelper type " + classname + " constructed");
		connectionHelper.setParams(params);
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
		
	protected void loadMapEvents(Configuration cepConfig) {
		logger.info("Defining MAP events.");
		try {
			List<MapEvent> mapEvents = connectionHelper.getMapEvents();
			for (MapEvent event : mapEvents) {
				String eventName = event.getEventName();
				logger.info("Loading event "+eventName+" into engine."); 
				cepConfig.addEventType(eventName, event.getFieldMap());
			}
		} catch (MetadataException e) {
			logger.fatal("Exception caught loading map events", e);
		}
	}
	
	protected void loadVariantEvents(Configuration cepConfig) {
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
	
}
