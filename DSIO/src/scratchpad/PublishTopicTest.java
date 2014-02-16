package scratchpad;

import java.io.IOException;

import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.pubsub.pub.PublishTopic;

public class PublishTopicTest {

	public static void main(String[] args) throws JSONException, IOException {
		PublishTopic publish = new PublishTopic.Builder().hostName("64.21.255.72").build();
		long begin = System.currentTimeMillis();
		long end = 0;
		int i =0;
		String aTopic = "";
		// and publish a test object
		EventObject anEvent = new EventObject();

		while (true) {
			i = i + 1;
			
			if ((i % 3) == 0) {aTopic = "JOHN.IBM";}
			if ((i % 3) == 1) {aTopic = "JOHN.AAPL";}
			if ((i % 3) == 2) {aTopic = "JOHN.AIG";}
			
			anEvent.setEventName("stock.price");
			anEvent.put("price",i);
			anEvent.put("instrument", aTopic);
			publish.publish(anEvent);
			if ((i % 50000) == 0) {
				end = System.currentTimeMillis();
				System.out.println("50,000 messages sent in "+Long.toString((end-begin)/1000)+" seconds");
				begin = end;
			}
			//take a break, we can do 100 events/second
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}