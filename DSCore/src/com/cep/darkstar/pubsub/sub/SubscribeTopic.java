/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.pubsub.sub;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * @author colin
 *
 */
public class SubscribeTopic {

	private String queue;
	private final String hostName;
    private final int portNumber;
    private String exchange;
    String routingKey = "";
    String topic = "";
    
    private final Connection conn = null;
    private Channel channel = null;
	QueueingConsumer consumer = null;
	QueueingConsumer.Delivery delivery = null;
    
    private SubscribeTopic(Builder builder) throws IOException {
    	this.hostName = builder.hostName;
    	this.portNumber = builder.portNumber;
    	this.exchange = builder.exchange;
    	this.topic = builder.topic;
    	this.queue = builder.queue;
    	
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(hostName);
		factory.setPort(portNumber);
		Connection conn = factory.newConnection();;
		channel = conn.createChannel();;

		// null queue for testing - defaults to default channel queue
		if (queue == null) {
			queue = channel.queueDeclare().getQueue();
		} else {
			channel.queueDeclare(queue, false, false, false, null);
		}
		channel.queueBind(queue, exchange, topic);

		consumer = new QueueingConsumer(channel);
		// we're setting auto-ack to true for speed - dangerous?
		channel.basicConsume(queue,true,consumer);
    }
    
    public static class Builder {
    	// hostname with default value
    	private String hostName = "localhost";
    	private int portNumber = AMQP.PROTOCOL.PORT;
    	private String exchange = "amq.topic";
    	private String topic = "#";
		private String queue = null;
    	
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
    	
    	public SubscribeTopic build() throws IOException {
    		return new SubscribeTopic(this);
    	}
    }
    
    public byte[] nextDelivery() throws ShutdownSignalException, InterruptedException, IOException {
		delivery = consumer.nextDelivery();
		return delivery.getBody();
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
		channel.close();
		conn.close();
		super.finalize();
	}
}
