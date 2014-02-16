package com.cep.darkstar.adv;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleResultSet;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.node.CEPEngine;
import com.cep.darkstar.node.CEPEventsSendRunnable;
import com.cep.utils.OracleConnectionHelper;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

/*
 * Handler class to create "adv window" containing current daily volume and 30-day average on a per-symbol basis
 */
public class AdvHandler implements IAdvHandler {
	
	private String url; 
	private String user; 
	private String pwd;
	
	private CEPEngine cepEngine;
	
	private EPStatement schema;
	private EPStatement window;
	private EPStatement merge;	
	private EPStatement update;
	
	/*
	 * String literals
	 */
	private static final String SUFFIX = ".N";
	private static final String QUERY = "SELECT RIC, ADWV FROM SM_ADWV"; 
	private static final String SYMBOL = "Symbol";
	private static final String ADVTODAY = "ADVToday";
	private static final String ADV30DAY = "ADV30Day";
	private static final String ADV = "ADV";
	private static final String DS_TIMESTAMP = "ds_timestamp";
	
	/*
	 * Containers for ADV values
	 */
	private Map<String,Double> averageBySymbol = new HashMap<String,Double>();

	/*
	 * Oracle classes
	 */
	private OracleConnection conn;
	private Statement stmt;
	
	/*
	 * Logging
	 */
	static Logger logger = Logger.getLogger("com.cep.darkstar.adv.AdvHandler");
	
	public AdvHandler() { }
	
	
	private void readAdv() {
		conn = OracleConnectionHelper.getConnection(url, user, pwd);
		try {
			stmt = conn.createStatement();
			OracleResultSet rs = (OracleResultSet) stmt.executeQuery(QUERY);
			while (rs.next()) {
				addSymbol(rs.getString("RIC"), rs.getDouble("ADWV"));
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void addSymbol(String ric, double adwv) {
		if (ric.endsWith(SUFFIX)) {
			String symbol = getSymbolFromRic(ric);
			if (averageBySymbol.containsKey(symbol)) {
				adwv = averageBySymbol.get(symbol) + adwv;
			} 
			averageBySymbol.put(symbol, adwv);
			updateStream(symbol, adwv);
		}
	}

	private String getSymbolFromRic(String ric) {
		return ric.substring(0, (ric.length()-2));
	}
	
	public double getAverageVolume(String symbol) {
		Double val = averageBySymbol.get(symbol);
		return (val == null ? 0.0 : val);
	}
	
	private void updateStream(String symbol, double avgVolume) {
		EventObject anEvent = new EventObject();
		try {
			anEvent.put(SYMBOL, symbol);
			anEvent.put(ADVTODAY, 0.0);
			anEvent.put(ADV30DAY, avgVolume);
			anEvent.setEventName(ADV);
			anEvent.put(DS_TIMESTAMP, System.currentTimeMillis());
			CEPEventsSendRunnable.handleEventObject(anEvent);
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private void createSchema() {
    	String stmt = "create schema ADVAggregateSchema as (Symbol string, ADVToday double, ADV30Day double)";
    	schema = cepEngine.getEPAdministrator().createEPL(stmt);
    	schema.addListener(new ADVListener());
	}
	
	private void createWindow() {
		String stmt = "create window ADVAggregateWin.std:unique(Symbol) as ADVAggregateSchema";
		window = cepEngine.getEPAdministrator().createEPL(stmt);
		window.addListener(new ADVListener());
	}
	
	private void createMerge() {
		String stmt = "on ADV adv merge ADVAggregateWin advwin where adv.Symbol = advwin.Symbol " +
    			"when matched then update set advwin.ADVToday = advwin.ADVToday + adv.ADVToday, advwin.ADV30Day = advwin.ADV30Day + adv.ADV30Day " +
    			"when not matched then insert(Symbol, ADVToday, ADV30Day) select Symbol, ADVToday, ADV30Day";
		merge = cepEngine.getEPAdministrator().createEPL(stmt);
		merge.addListener(new ADVListener());
	}
	
	private void createUpdate() {
		String stmt = "on FIXExecutionReport(LastShares > 0) fix merge ADVAggregateWin adv where fix.Symbol = adv.Symbol " +
				"when matched then update set adv.ADVToday = adv.ADVToday + fix.LastShares";
			
		update = cepEngine.getEPAdministrator().createEPL(stmt);
		update.addListener(new ADVListener());
	}
	
	public void generateEpl(CEPEngine cepEngine) {
		this.cepEngine = cepEngine;
		createSchema();
		createWindow();
		createMerge();
		createUpdate();
	}
	
	public class ADVListener implements UpdateListener {
    	@SuppressWarnings("unchecked")
		@Override
    	public void update(EventBean[] arg0, EventBean[] arg1) {
    		if (logger.isDebugEnabled()) {
    			logger.debug("ADV listener update: \n");
    			for (EventBean event : arg0) {
    				Map<String, Object> aMap = (HashMap<String, Object>) event.getUnderlying();
    				logger.debug(aMap);
    			}
    		}
    	}
    }

	@Override
	public void run() {
		readAdv();		
	}


	@Override
	public void setParams(String url, String user, String pwd) {
		this.url = url;
		this.user = user;
		this.pwd = pwd;		
	}

}
