package com.cep.darkstar.query;

import java.util.HashMap;
import java.util.Map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public abstract class CEPStatementListener implements UpdateListener {
	protected String queryID = null;
	protected long ts;
	protected Map<String, String> events = new HashMap<String, String>();
	
	public CEPStatementListener(String queryID) {
		this.queryID = queryID;
	}
	
	public abstract void update(EventBean[] newData, EventBean[] oldData);
	
}
