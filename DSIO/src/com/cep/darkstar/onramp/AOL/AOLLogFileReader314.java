/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.onramp.AOL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.cep.commons.EventObject;
import com.cep.darkstar.pubsub.pub.PublishTopic;

/**
 * @author colin
 *
 */
public class AOLLogFileReader314 {
	public static void main(String[] args) throws IOException, InterruptedException {

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String str;
		PublishTopic publisher = new PublishTopic.Builder().topic("EVENT").hostName("192.168.1.6").build();
		int x = 0;
		while ((str = in.readLine()) != null) {
			try {
				// split on ctrl-E
				String[] logFields = (str.split("\\cE"));

				// aol314
				EventObject log314 = new EventObject();
				log314.put("version_id", Long.parseLong(logFields[0]));
				log314.put("record_size", Long.parseLong(logFields[1]));
				log314.put("seq_id", Long.parseLong(logFields[2]));
				log314.put("plcnetwork_id", Long.parseLong(logFields[3]));
				log314.put("plcsubnetwork_id", Long.parseLong(logFields[4]));
				log314.put("plc_subnetwork_id", Long.parseLong(logFields[5]));
				log314.put("website_id", Long.parseLong(logFields[6]));
				log314.put("placement_id", Long.parseLong(logFields[7]));
				log314.put("campaign_network_id", Long.parseLong(logFields[8]));
				log314.put("campaign_subnetwork_id", Long.parseLong(logFields[9]));
				log314.put("campaign_id", Long.parseLong(logFields[10]));
				log314.put("extension_type", Long.parseLong(logFields[11]));
				// no need to pump filler data out
				//log314.put("filler1", Long.parseLong(logFields[12]));
				//log314.put("filler2", Long.parseLong(logFields[13]));
				//log314.put("filler3", Long.parseLong(logFields[14]));
				//log314.put("filler4", Long.parseLong(logFields[15]));
				//log314.put("kv_length", Long.parseLong(logFields[16]));
				//log314.put("kv_string", logFields[16]);
				log314.put("event_name", "AOL314");
				publisher.publish(log314);
				log314 = null;

				// aol314kv
				String[] logFieldsKV = logFields[16].split(";");
				EventObject log314kv = new EventObject();
				log314kv.put("seq_id", Long.parseLong(logFields[2]));
				for(String aKey:logFieldsKV) {
					String[] aRow = aKey.split("=");
					log314kv.put("key", aRow[0]);
					log314kv.put("value", aRow[1]);
					log314kv.put("event_name", "AOL314KV");
					publisher.publish(log314kv);
				}

			} catch (Exception e) {
				System.out.println("Error parsing:"+str);
				e.printStackTrace();
			}	
			x++;
			if((x%5000)==0) {
				Thread.sleep(1000);
				x = 0;
				System.out.println("Sent 5000 events.");
			}
		}
	}
}
