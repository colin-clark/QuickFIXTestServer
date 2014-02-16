package com.cep.darkstar.pubsub.pub;

import java.io.IOException;

import org.json.JSONException;

import com.cep.commons.EventObject;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class PublishDirect {
	String hostName = "localhost";
    int portNumber = AMQP.PROTOCOL.PORT;
    String exchange = "amq.direct";
    String routingKey = "SawTestRabbitMQQueue";
    Connection conn = null;
    Channel ch = null;
	
	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	// constructor
	// rabbitmq information goes here
	public PublishDirect(String eventName) throws IOException {
		ConnectionFactory factory = new ConnectionFactory();
    	factory.setPort(portNumber);
    	factory.setHost(hostName);
    	conn = factory.newConnection();
        ch = conn.createChannel();

        if (routingKey == "") {
        	routingKey = "local";
        }
        
        if (exchange == "") {
            ch.queueDeclare();
        }
	}
	
	// let's clean up after ourselves and be good citizens
	protected void finalize() throws Throwable {
		ch.close();
		conn.close();
		super.finalize();
	}
	
		// specific message/event generation goes here
	public void publish(EventObject eventObject) throws IOException, JSONException {
		// here we publish to RabbitMQ
        //System.out.println("Sending to Exchange:" + exchange + ", Event Type:" + eventObject.getEventName());
        ch.basicPublish(exchange, "SawTestRabbitMQQueue", null, eventObject.toString().getBytes());
	}
}

