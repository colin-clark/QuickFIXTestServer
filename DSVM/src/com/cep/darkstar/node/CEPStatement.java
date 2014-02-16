package com.cep.darkstar.node;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPStatement;

public class CEPStatement {
	private EPStatement statement;
	private String eplStatement;
	private String queryName;

	public CEPStatement(EPAdministrator admin, String queryName, String eplStatement)
	{
		this.eplStatement = eplStatement;
		this.setQueryName(queryName);
		statement = admin.createEPL(this.eplStatement);
	}

	public void addListener(CEPStatementListener cepStatementListener)
	{
		statement.addListener(cepStatementListener);
	}
	
	// kill the statement
	public void kill() {
		statement.destroy();
	}
	
	public void stop() {
		statement.stop();
	}
	
	public void start() {
		statement.start();
	}
	
	public boolean isRunning() {
		return statement.isStarted();
	}
	
	public boolean isStopped() {
		return statement.isStopped();
	}
	
	public boolean isDestroyed() {
		return statement.isDestroyed();
	}

	/**
	 * @param queryName the queryName to set
	 */
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	/**
	 * @return the queryName
	 */
	public String getQueryName() {
		return queryName;
	}
}
