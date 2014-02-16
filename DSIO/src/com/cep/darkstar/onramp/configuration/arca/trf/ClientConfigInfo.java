 package com.cep.darkstar.onramp.configuration.arca.trf;


public class ClientConfigInfo {
	private String log4j_file = "log4j.configuration";
	private String darkStarNode = "localhost";
	private int darkStarPort = 9160;
	private String firm = "CEP";
	private String clusterName;
	private String keyspace;
	private int number_of_publishers = 6;
	private int batch_size = 500;
	private String eventName;
	
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
	
	public String getFirm() {
		return firm;
	}
	public void setFirm(String firm) {
		this.firm = firm;
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
	public int getNumber_of_publishers() {
		return number_of_publishers;
	}
	public void setNumber_of_publishers(int number_of_publishers) {
		this.number_of_publishers = number_of_publishers;
	}
	public int getBatch_size() {
		return batch_size;
	}
	public void setBatch_size(int batch_size) {
		this.batch_size = batch_size;
	}
}
