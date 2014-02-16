/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.node;

/**
 * @author colin
 *
 */
public interface QueryInt {
	public static final String PUBLISH_PORT = "publishPort";
	public static final String EXCHANGE = "exchange";
	public static final String TOPIC = "topic";
	
	String getQueryID();
	
	String getQuery();
	
	String getQueryName();
	
	void stop();
	
	void kill();
	
	void start();

	boolean isRunning();
	
	boolean isStopped();
	
	boolean isDestroyed();
}
