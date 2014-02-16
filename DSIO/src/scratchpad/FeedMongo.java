/**
 * Cloud Event Processing, Inc.
 * 
 */
package scratchpad;

import com.cep.darkstar.pubsub.sub.Rabbit2Mongo;

public class FeedMongo {

	public static void main(String[] args) {
		Rabbit2Mongo r2m = new Rabbit2Mongo();
		r2m.main();
	}
}
