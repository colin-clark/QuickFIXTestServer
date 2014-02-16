/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.query;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.client.UpdateListener;

/**
 * @author colin
 *
 */
public class CEPNamedStatement {

	private EPStatement statement;
	private String eplStatement;
	private String queryName;
	private EPStatement populateWindow;

	public CEPNamedStatement(EPAdministrator admin, String queryName, String eplStatement) {
		this.eplStatement = eplStatement;
		this.queryName = queryName;
		System.out.println("Running query:"+this.eplStatement);
		// named statement
		// insert the query into the name so that the query name can be re-used
		this.populateWindow = admin.createEPL("insert into "+this.queryName+" "+this.eplStatement);
		this.statement = admin.createEPL("select * from "+this.queryName);
	}

	// Used to take CEPNamedStatementListener as arg, refactored to get rid of
	// dependency on Telescope classes - MW
	public void addListener(StatementAwareUpdateListener cepStatementListener)	{
		statement.addListener(cepStatementListener);
	}
	
	public void addListener(UpdateListener localExceptionTest) {
		statement.addListener(localExceptionTest);
	}

	// kill the statement
	public void kill() {
		statement.destroy();
		populateWindow.destroy();
	}

	public void stop() {
		statement.stop();
		populateWindow.stop();
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
}
