package com.cep.darkstar.onramp.configuration;

public class BaseConfig {
	protected String log4j_file;

	protected String darkStarNode;
	protected int darkStarPort;
	protected String clusterName;
	protected String keyspace;
	protected String eventName;
	
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public String getDarkStarNode() {
		return darkStarNode;
	}
	public void setDarkStarNode(String darkStarNode) {
		this.darkStarNode = darkStarNode;
	}
	public int getDarkStarPort() {
		return darkStarPort;
	}
	public void setDarkStarPort(int darkStarPort) {
		this.darkStarPort = darkStarPort;
	}
		public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public String getKeyspace() {
		return keyspace;
	}
	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}
	public String getLog4j_file() {
		return log4j_file;
	}
	public void setLog4j_file(String log4j_file) {
		this.log4j_file = log4j_file;
	}
}
