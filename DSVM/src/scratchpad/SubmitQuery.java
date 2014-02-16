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
public class SubmitQuery {
	
	public static void main(String[] args) throws JSONException, IOException {
		PublishTopic publish = new PublishTopic.Builder().hostName("192.168.1.5").build();
		// and publish a test object
		EventObject anEvent = new EventObject();

		anEvent.put("NqID", "ABC123");
		anEvent.put("Nq", "select * from AOLLog");
		anEvent.setEventName("query");
		
		publish.publish(anEvent);
		System.exit(0);
	}
}
