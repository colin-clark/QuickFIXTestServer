package com.cep.darkstar.utility.cassandra;

import java.util.List;

public class CassandraClientConfigInfo {
	protected String log4j_file;
	protected String darkStarNode;
	protected int darkStarPort;
	protected String clusterName;
	protected String keyspace;
	protected String eventName;
	protected String cassandraNode;
	protected int cassandraPort;
	protected String cassandraClusterName;
	protected String cassandraKeyspace;
	protected String partition_on;
	protected String cf_name;
	protected int message_count;
	protected String callback;
	protected String file;
	protected String startTime;
	protected String endTime;
	protected List<String> headers;
	protected boolean useTransactTime = true;
	protected String inputFile;
	protected String columnNames;
	
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
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
	public String getCassandraNode() {
		return cassandraNode;
	}
	public void setCassandraNode(String cassandraNode) {
		this.cassandraNode = cassandraNode;
	}
	public int getCassandraPort() {
		return cassandraPort;
	}
	public void setCassandraPort(int cassandraPort) {
		this.cassandraPort = cassandraPort;
	}
	public String getCassandraClusterName() {
		return cassandraClusterName;
	}
	public void setCassandraClusterName(String cassandraClusterName) {
		this.cassandraClusterName = cassandraClusterName;
	}
	public String getCassandraKeyspace() {
		return cassandraKeyspace;
	}
	public void setCassandraKeyspace(String cassandraKeyspace) {
		this.cassandraKeyspace = cassandraKeyspace;
	}
	public String getPartition_on() {
		return partition_on;
	}
	public void setPartition_on(String partition_on) {
		this.partition_on = partition_on;
	}
	public String getCf_name() {
		return cf_name;
	}
	public void setCf_name(String cf_name) {
		this.cf_name = cf_name;
	}
	public int getMessage_count() {
		return message_count;
	}
	public void setMessage_count(int message_count) {
		this.message_count = message_count;
	}
	public List<String> getHeaders() {
		return headers;
	}
	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}
	public boolean isUseTransactTime() {
		return useTransactTime;
	}
	public void setUseTransactTime(boolean useTransactTime) {
		this.useTransactTime = useTransactTime;
	}
	public String getInputFile() {
		return inputFile;
	}
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}
	public String getColumnNames() {
		return columnNames;
	}
	public void setColumnNames(String columnNames) {
		this.columnNames = columnNames;
	}
	
}
