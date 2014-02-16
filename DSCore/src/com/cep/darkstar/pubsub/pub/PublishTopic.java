package com.cep.darkstar.pubsub.pub;

import java.io.IOException;

import org.json.JSONException;

import com.cep.commons.EventObject;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class PublishTopic {
	private final String hostName;
    private final int portNumber;
    private final String exchange;
    String routingKey = "";
    String topic = "";
    
    private final Connection conn;
    private final Channel ch;
    
    private PublishTopic(Builder builder) throws IOException {
    	this.hostName = builder.hostName;
    	this.portNumber = builder.portNumber;
    	this.exchange = builder.exchange;
    	this.topic = builder.topic;
    	    	
    	ConnectionFactory factory = new ConnectionFactory();
    	factory.setPort(portNumber);
    	factory.setHost(hostName);
    	this.conn = factory.newConnection();
        this.ch = conn.createChannel();
    }
    
    public static class Builder {
    	// hostname with default value
    	private String hostName = "localhost";
    	private int portNumber = AMQP.PROTOCOL.PORT;
    	private String exchange = "amq.topic";
    	private String topic = "topic";
    	
    	public Builder hostName(String hostName) {
    		this.hostName = hostName;
    		return this;
    	}
    	
    	public Builder portNumber(int portNumber) {
    		this.portNumber = portNumber;
    		return this;
    	}
    	    	
    	public Builder exchange(String exchange) {
    		this.exchange = exchange;
    		return this;
    	}
    	
    	public Builder topic(String topic) {
    		this.topic = topic;
    		return this;
    	}
    	
    	public PublishTopic build() throws IOException {
    		return new PublishTopic(this);
    	}
    }
    
	public String getHostName() {
		return hostName;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public String getExchange() {
		return exchange;
	}
		
	// let's clean up after ourselves and be good citizens
	protected void finalize() throws Throwable {
		ch.close();
		conn.close();
		super.finalize();
	}
		 
	// let's send a message...
	public void publish(EventObject eventObject) throws IOException, JSONException {
		// no ack required
        ch.basicPublish(exchange, topic , null, eventObject.toString().getBytes());
	}
}
