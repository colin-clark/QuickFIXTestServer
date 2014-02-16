package com.cep.darkstar.query;

import java.io.IOException;

import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.soda.StreamSelector;

public class DefaultCEPEngine extends AbstractCEPEngine {

	public DefaultCEPEngine() {
		cepConfig.getEngineDefaults().getStreamSelection().setDefaultStreamSelector(StreamSelector.RSTREAM_ISTREAM_BOTH);
		// we should load plugin's dynamically
		//cepConfig.addPlugInView("SAX", "saxify", SAXPlugInViewFactory.class.getName());
		
		// load events up if not already defined
		if (mapEventsLoaded==false) {
			loadMapEvents(cepConfig);
			mapEventsLoaded=true;
		}
		
		// let's get a cep engine
		cepEngine = EPServiceProviderManager.getProvider("DarkStar", cepConfig);
	
		// load events up if not already defined
		if (variantEventsLoaded==false) {
			loadVariantEvents(cepConfig);
			variantEventsLoaded=true;
		}
		
		// start event pump if not already started
		if (eventPumpThread==null) {
			try {
				eventPumpThread = new CEPEventsSendRunnable(cepEngine, "EVENT");
				eventPumpThread.start();
			} catch (IOException e) {
				logger.fatal(e.getMessage(), e);
			}
		}
	}
}
