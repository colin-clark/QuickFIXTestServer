package scratchpad;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.json.JSONObject;


public class OnRamp_old {
	 static Logger logger = Logger.getLogger("com.cep.darkstar.onramp.OnRamp");
	 	
	// generate events/messages here
	static abstract class Producer implements Runnable {
		// data queue
		private final ConcurrentLinkedQueue<JSONObject> queue;
		
		// constructor
		Producer(ConcurrentLinkedQueue<JSONObject> q) {
			queue = q;
		}
		
		public void run() {
		   while (true) { queue.offer(produce()); }
		}
		 
		// specific message/event generation goes here
		abstract public JSONObject produce();
	}

	static class Consumer implements Runnable {
		 private final ConcurrentLinkedQueue<JSONObject> queue;
		 
		 Consumer(ConcurrentLinkedQueue<JSONObject> q) { queue = q; }
		 
		 public void run() {
			 while (true) {
				 if (!queue.isEmpty()) {
					 consume(queue.remove());
				 } else {
					 try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						continue;
					}
			
				 }
			 }
		 }
		 
		 void consume(JSONObject x) {
			 logger.info("Injecting event into the DarkStar cloud:"+x.toString());
		 }
	}
}
