package com.cep.darkstar.onramp.configuration.cnn;

public class ClientConfigInfo {
	private String source = "CNN";
	private String input_function = "com.cep.darkstar.dto.CNNNews";
	private String news_directory;
	private String processed_directory;
	
	protected String log4j_file;

	protected String darkStarNode;
	protected int darkStarPort;
	protected String clusterName;
	protected String keyspace;
	protected String eventName;
	
	private int delay = 0;
	
	public int getDelay() {
		return delay;
	}
	
	public void setDelay(int delay) {
		this.delay = delay;
	}
	
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
	
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getInput_function() {
		return input_function;
	}
	public void setInput_function(String input_function) {
		this.input_function = input_function;
	}
	public String getNews_directory() {
		return news_directory;
	}
	public void setNews_directory(String news_directory) {
		this.news_directory = news_directory;
	}

	public String getProcessed_directory() {
		return processed_directory;
	}

	public void setProcessed_directory(String processed_directory) {
		this.processed_directory = processed_directory;
	}
}
