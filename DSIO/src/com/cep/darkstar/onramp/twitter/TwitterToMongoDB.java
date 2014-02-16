package com.cep.darkstar.onramp.twitter;

import com.cep.commons.EventObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;

public class TwitterToMongoDB {
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		try {
			Mongo m = new Mongo("localhost");
			DB db = m.getDB("twitter");
			DBCollection tweets = db.getCollection("tweets");
			DBCollection users = db.getCollection("users");
			DBCollection places = db.getCollection("places");
		
			// rabbitmq configuration
			String hostName = "localhost";
			int portNumber = AMQP.PROTOCOL.PORT;
			String topicPattern = "twitter.#";
			String exchange = null;
			String queue = null;

			ConnectionFactory connFactory = new ConnectionFactory();
			connFactory.setHost(hostName);
			connFactory.setPort(portNumber);
			Connection  conn = connFactory.newConnection();

			final Channel channel = conn.createChannel();

			if (exchange == null) {
				exchange = "amq.topic";
			} else {
				channel.exchangeDeclare(exchange, "topic");
			}
			if (queue == null) {
				queue = channel.queueDeclare().getQueue();
			} else {
				channel.queueDeclare();
			}
			channel.queueBind(queue, exchange, topicPattern);
			System.out.println("Listening to exchange " + exchange + ", pattern " + topicPattern +
					" from queue " + queue);

			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queue, consumer);
			while (true) {
				try {
					QueueingConsumer.Delivery delivery = consumer.nextDelivery();
					Envelope envelope = delivery.getEnvelope();
					String body = new String(delivery.getBody());
					//System.out.println(envelope.getRoutingKey() + ": " + body);
					channel.basicAck(envelope.getDeliveryTag(), false);
					EventObject twitterEvent = new EventObject(body);
					System.out.println("Received:"+twitterEvent.toString());
					System.out.println("Received and persisting:"+twitterEvent.getEventName()+":"+twitterEvent.toString());
/*					BasicDBObject dbTwitterObject = twitterEvent.toMongoDB();
*/					System.out.println("Event name:'"+twitterEvent.getEventName()+"'");
					/*if (twitterEvent.getEventName().equals("twitter.tweet")) {
						System.out.println("Injecting tweet.");
						tweets.insert(dbTwitterObject);
					}
					if (twitterEvent.getEventName().equals("twitter.user")) {
						System.out.println("Injecting user.");
						users.insert(dbTwitterObject);
					}
					if (twitterEvent.getEventName().equals("twitter.place")) {
						System.out.println("Injecting place.");
						places.insert(dbTwitterObject);
					}*/
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
			}
		} catch (Exception ex) {
			System.err.println("Main thread caught exception: " + ex);
			ex.printStackTrace();
		}
	}
}
