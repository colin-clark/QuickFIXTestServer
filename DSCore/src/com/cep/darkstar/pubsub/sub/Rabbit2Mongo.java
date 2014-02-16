/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.pubsub.sub;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;

/**
 * @author colin
 *
 */
public class Rabbit2Mongo {
	static Logger logger = Logger.getLogger("com.cep.darkstar.offramp.Rabbit2Mongo");

	class Producer implements Runnable {

		@SuppressWarnings("unused")
		private final BlockingQueue<BasicDBObject> eventQueue;

		Producer(BlockingQueue<BasicDBObject> q) {eventQueue = q;}

		public void run() {
			try {
				long begin = System.currentTimeMillis();
				long end = 0;
				final String hostName = "localhost";
				final int portNumber = AMQP.PROTOCOL.PORT;
				final String topicPattern = "#";
				ConnectionFactory factory = new ConnectionFactory();
				factory.setHost(hostName);
				factory.setPort(portNumber);
				final Connection conn = factory.newConnection();
				final Channel channel = conn.createChannel();
				final String exchange = "amq.topic";
				final String queue = channel.queueDeclare().getQueue();
				channel.queueBind(queue, exchange, topicPattern);
				logger.info("Listening to exchange " + exchange + ", pattern " + topicPattern +" from queue " + queue);

				QueueingConsumer consumer = new QueueingConsumer(channel);
				channel.basicConsume(queue, consumer);
				
				long i = 0;
				logger.info(Thread.currentThread().toString()+" entering listening loop.");
				while (true) {
					QueueingConsumer.Delivery delivery = consumer.nextDelivery();
					Envelope envelope = delivery.getEnvelope();
					String body = new String(delivery.getBody());
					channel.basicAck(envelope.getDeliveryTag(), false);
					@SuppressWarnings("unused")
					EventObject event = new EventObject(body);
//					BasicDBObject dbObject = event.toMongoDB();
//					eventQueue.put(dbObject);
					i=i+1;
					if ((i % 50000) == 0) {
						end = System.currentTimeMillis();
						logger.info("50,000 messages grabbed from RabbitMQ in "+Long.toString((end-begin)/1000)+" seconds");
						begin = end;
					}
				}
				
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			} catch (JSONException e) {
				logger.error(e.getMessage(), e);
			}		
		}
	}

	class Consumer implements Runnable {

		private final BlockingQueue<BasicDBObject> eventQueue;
		Consumer(BlockingQueue<BasicDBObject> q) { eventQueue = q; }
		public void run() {
			try {
				long begin = System.currentTimeMillis();
				long end = 0;
				Mongo m = new Mongo("localhost");
				DB db = m.getDB("testDB");
				DBCollection coll = db.getCollection("testCollection");
				long i = 0;
				logger.info(Thread.currentThread().toString()+" entering injection loop.");
				while (true) {
					coll.insert(eventQueue.take());
					i=i+1;
					if ((i % 50000) == 0) {
						end = System.currentTimeMillis();
						logger.info("50,000 messages sent to MongoDB in "+Long.toString((end-begin)/1000)+" seconds");
						begin = end;
					}
				}
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}		
		}
	}
	
	public void main() {
		// initialize log4j
		PropertyConfigurator.configure("log4j.properties");
		
		BlockingQueue<BasicDBObject> q = new ArrayBlockingQueue<BasicDBObject>(1000);
		final Executor fromRabbit = Executors.newFixedThreadPool(1);
		final Executor toMongo = Executors.newFixedThreadPool(2);
		
		fromRabbit.execute(new Producer(q));
		toMongo.execute(new Consumer(q));
	}
}