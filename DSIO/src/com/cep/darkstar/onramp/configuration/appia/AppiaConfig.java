package com.cep.darkstar.onramp.configuration.appia;

import java.util.List;

import com.cep.darkstar.onramp.configuration.BaseConfig;

public class AppiaConfig extends BaseConfig {
    private String host;
    private int port;
    private String uniqueID;
    private boolean globalMessages;
    private boolean applicationMessages;
    private boolean sessionMessages;
    private boolean sessionEvents;
    private boolean messageValidatedEvents;
    private boolean messageCommitEvents;
    private boolean messageSentEvents;
    private String partition_key;
    private List<FixEventMapping> eventMapping;
    private String firm;
    private int queueSize;
    
	public String getPartition_key() {
		return partition_key;
	}
	public void setPartition_key(String partition_key) {
		this.partition_key = partition_key;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUniqueID() {
		return uniqueID;
	}
	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}
	public boolean isGlobalMessages() {
		return globalMessages;
	}
	public void setGlobalMessages(boolean globalMessages) {
		this.globalMessages = globalMessages;
	}
	public boolean isApplicationMessages() {
		return applicationMessages;
	}
	public void setApplicationMessages(boolean applicationMessages) {
		this.applicationMessages = applicationMessages;
	}
	public boolean isSessionMessages() {
		return sessionMessages;
	}
	public void setSessionMessages(boolean sessionMessages) {
		this.sessionMessages = sessionMessages;
	}
	public boolean isSessionEvents() {
		return sessionEvents;
	}
	public void setSessionEvents(boolean sessionEvents) {
		this.sessionEvents = sessionEvents;
	}
	public boolean isMessageValidatedEvents() {
		return messageValidatedEvents;
	}
	public void setMessageValidatedEvents(boolean messageValidatedEvents) {
		this.messageValidatedEvents = messageValidatedEvents;
	}
	public boolean isMessageCommitEvents() {
		return messageCommitEvents;
	}
	public void setMessageCommitEvents(boolean messageCommitEvents) {
		this.messageCommitEvents = messageCommitEvents;
	}
	public boolean isMessageSentEvents() {
		return messageSentEvents;
	}
	public void setMessageSentEvents(boolean messageSentEvents) {
		this.messageSentEvents = messageSentEvents;
	}
	public String getFirm() {
		return firm;
	}
	public void setFirm(String firm) {
		this.firm = firm;
	}
	public List<FixEventMapping> getEventMapping() {
		return eventMapping;
	}
	public void setEventMapping(List<FixEventMapping> eventMapping) {
		this.eventMapping = eventMapping;
	}
	public int getQueueSize() {
		return queueSize;
	}
	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}
    
}
