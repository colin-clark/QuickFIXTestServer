/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.node;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPStatement;

/**
 * @author colin
 *
 */
public class CEPNamedStatement {

	private EPStatement statement;
	private String eplStatement;
	private String queryName;
	private EPStatement populateWindow;

	public CEPNamedStatement(EPAdministrator admin, String queryName, String eplStatement)
	{
		this.eplStatement = eplStatement;
		this.setQueryName(queryName);
		System.out.println("Running query:"+this.eplStatement);
		this.populateWindow = admin.createEPL("insert into "+queryName+" "+eplStatement);
		this.statement = admin.createEPL("select * from "+queryName);
	}

	public void addListener(CEPNamedStatementListener cepStatementListener)
	{
		statement.addListener(cepStatementListener);
	}

	// kill the statement
	public void kill() {
		statement.destroy();
	}

	public void stop() {
		populateWindow.stop();
		statement.stop();
	}

	public void start() {
		statement.start();
		populateWindow.start();
		
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
