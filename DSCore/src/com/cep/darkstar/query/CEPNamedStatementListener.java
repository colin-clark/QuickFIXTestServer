/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.query;

import java.util.HashMap;
import java.util.Map;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;

/**
 * @author colin
 *
 */

@SuppressWarnings("unused")

public class CEPNamedStatementListener implements StatementAwareUpdateListener {
	private String queryID = null;
	private long ts;
	private AbstractCEPEngine cepEngine;				// our engine
	private static Map<String, String> events = new HashMap<String, String>();

	public CEPNamedStatementListener(String queryID, AbstractCEPEngine cepEngine) {
		this.queryID = queryID;
		this.cepEngine = cepEngine;
	}

	@Override
	public void update(EventBean[] newData, EventBean[] oldData, EPStatement statement, EPServiceProvider epServiceProvider) {
/*		// new event
		System.out.println("------------------------------------------");
		System.out.println("Named Statement (saved query):"+statement.getName());
		System.out.println("Actual Statement:"+statement.getText());
		System.out.println("Service Provider:"+epServiceProvider.toString());
		//System.out.println("Underlying      :"+newData[0].getUnderlying());

		EventObject anEvent = new EventObject();
		if (newData != null) {
			// if we haven't seen the event, send out the field names
			if (events.containsKey(queryID)==false) {
				StringBuilder buf = new StringBuilder();
				boolean first = true;
				Map<?, ?> aMap = (HashMap<?, ?>) newData[0].getUnderlying();
				System.out.println("Sending schema for:"+queryID+" as:"+aMap.toString());
				for (Object name : aMap.keySet()) {
					if (first) {
						buf.append(name.toString());
						first=false;
					} else {
						buf.append(":"+name.toString());
					}
				}

				//StringBuilder buf = new StringBuilder();
				//boolean first = true;
				//for (String name : newData[0].getEventType().getPropertyNames()) {
				//	if (first) {buf.append(name); first=false;}
				//	else {buf.append(":"+name);}
				//}
				try {
					anEvent.put("fields",buf);
					ts = System.currentTimeMillis();
					anEvent.put("data_action", "fields");
					anEvent.put("query_id", queryID);
					anEvent.put("t_ts", ts);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				System.out.println("Broadcasting field names for "+statement.getText());
				publishToBlazeDS(anEvent);
				// define the event so we don't broadcast the fields again
				events.put(queryID, "defined" );
				//events.put(statement.getName(), statement.getText());
			}
			
			for(int i=0;i<newData.length;i++) {
				try {
					Map<?, ?> aMap = (HashMap<?, ?>) newData[i].getUnderlying();
					anEvent = new EventObject(aMap);
				} catch (Exception e) {
					anEvent = new EventObject();
					for (String name : newData[i].getEventType().getPropertyNames()) {
						try {
							anEvent.put(name, newData[i].get(name));
						} catch (PropertyAccessException e1) {
							// 
							e1.printStackTrace();
						} catch (JSONException e1) {
							// 
							e1.printStackTrace();
						}
					}
				}
				try {
					ts = System.currentTimeMillis();
					anEvent.put("data_action", "new");
					anEvent.put("query_id", queryID);
					anEvent.put("t_ts", ts);
					//System.out.println("Sending from named statement listener:"+anEvent.toString());
					publishToBlazeDS(anEvent);
				} catch (JSONException e) {
					// 
					e.printStackTrace();
				}
			}
		}*/
	}
	
	
}			
