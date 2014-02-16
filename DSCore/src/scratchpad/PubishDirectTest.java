package scratchpad;

import java.io.IOException;

import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.pubsub.pub.PublishDirect;

public class PubishDirectTest {
	
	public static void main(String[] args) throws InterruptedException, IOException {
		PublishDirect publish = new PublishDirect("eventName");
		long end = 0;
		long begin = System.currentTimeMillis();
		int i = 0;
		while (true) {
			i = i + 1;
			try {
				EventObject anEvent = new EventObject("eventName");
				anEvent.put("key", Integer.toString(i));
				publish.publish(anEvent);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if ((i % 5000) == 0) {
				end = System.currentTimeMillis();
				System.out.println("5,000 messages sent in "+Long.toString((end-begin)/1000)+" seconds");
				begin = end;
			}
		}
	}
}