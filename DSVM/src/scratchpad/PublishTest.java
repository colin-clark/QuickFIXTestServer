/**
 * Cloud Event Processing, Inc.
 * 
 */
package scratchpad;

import java.io.IOException;

import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.pubsub.pub.PublishTopic;

/**
 * @author colin
 *
 */
public class PublishTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public static void main(String[] args) throws IOException, JSONException {
		PublishTopic publish = new PublishTopic.Builder().hostName("192.168.1.5").topic("QUERY").build();
		EventObject event = new EventObject();
		event.put("Hello", "World");
		for (int i=0;i<100;i++) {
			publish.publish(event);
		}
		System.exit(0);
	}
}
