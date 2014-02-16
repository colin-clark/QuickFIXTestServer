/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.query;

/**
 * @author colin
 *
 */
public interface QueryInt {
	String getQueryID();
	
	String getQuery();
	
	void stop();
	
	void kill();
	
	void start();

	boolean isRunning();
	
	boolean isStopped();
	
	boolean isDestroyed();
}
