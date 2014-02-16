package com.cep.darkstar.onramp.configuration.weatherstats;

import com.cep.darkstar.onramp.configuration.BaseConfig;


public class WeatherStatsConfigInfo extends BaseConfig {
	
	private String log4j_file = "log4j.configuration";
	private String darkStarNode = "localhost";
	private int darkStarPort = 9160;
	private String clusterName = "DarkStarCluster";
	private String keyspace = "system";
	private String eventName = "WeatherStats";
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
	public String getLog4j_file() {
		return log4j_file;
	}
	public void setLog4j_file(String log4j_file) {
		this.log4j_file = log4j_file;
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
	
}
