package scratchpad;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.cep.darkstar.onramp.twitter.Tweet;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class DumpTweetsFromMongo {

	public Tweet[] getTweets() throws UnknownHostException, MongoException {
		// list of tweets, we're going to convert this to an array at the end
		List<Tweet> list = new ArrayList<Tweet>();
		// declare and connect to mongo
		Mongo m = new Mongo("localhost");
		DB db = m.getDB("twitter");
		DBCollection coll = db.getCollection("tweets");
		BasicDBObject filter = new BasicDBObject();
		BasicDBObject query = new BasicDBObject();
		filter.put("text", 1);
		filter.put("user.screen_name", 1);
		DBCursor cur = coll.find(query,filter);
		while (cur.hasNext()) {
			DBObject row = cur.next();
			//System.out.println(row.toString());
			String text = row.get("text").toString();
			String user = ((DBObject) row.get("user")).get("screen_name").toString();

			// let's stuff the list
			Tweet aTweet = new Tweet();
			aTweet.setSentiment(0.0);
			aTweet.setText(text);
			aTweet.setUser(user);
			list.add(aTweet);
		}

		Tweet[] tweets = new Tweet[list.size()];
		Iterator<Tweet> i = list.iterator(); 
		int index = 0;
		while(i.hasNext()) {
			tweets[index] = (Tweet)i.next();
			index++;
		}
		return tweets;        
	}

	public static void main(String[] args) {
		
	}

}


