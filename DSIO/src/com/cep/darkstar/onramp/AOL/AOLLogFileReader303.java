/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.onramp.AOL;

/**
 * @author colin
 *
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.cep.commons.EventObject;
import com.cep.darkstar.pubsub.pub.PublishTopic;

public class AOLLogFileReader303 {

	public static void main(String[] args) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String str;
		PublishTopic publisher = new PublishTopic.Builder().topic("EVENT").hostName("darkstar-d01.ihost.aol.com").build();
		int x = 0;
		
		try {
			while ((str = in.readLine()) != null) {
				// split on ctrl-E
				String[] logFields = (str.split("\\cE"));
				
				EventObject log303 = new EventObject();
				log303.put("plcnetwork_id", Long.parseLong(logFields[0]));
				log303.put("plcsubnetwork_id", Long.parseLong(logFields[1]));
				log303.put("enter_date", logFields[2]);
				log303.put("imp_hour", Long.parseLong(logFields[3]));
				log303.put("record_type_id", Long.parseLong(logFields[4]));
				log303.put("seq_id", Long.parseLong(logFields[5]));
				log303.put("version_id", Long.parseLong(logFields[6]));
				log303.put("imp_minute", Long.parseLong(logFields[7]));
				log303.put("imp_second", Long.parseLong(logFields[8]));
				log303.put("master_campaign_id", Long.parseLong(logFields[9]));
				log303.put("campaign_id", Long.parseLong(logFields[10]));
				log303.put("website_id", Long.parseLong(logFields[11]));
				log303.put("placement_id", Long.parseLong(logFields[12]));
				log303.put("page_id", Long.parseLong(logFields[13]));
				log303.put("banner_id", Long.parseLong(logFields[14]));
				log303.put("banner_num", Long.parseLong(logFields[15]));
				log303.put("campaign_nework_id", Long.parseLong(logFields[16]));
				log303.put("campaign_network_id", Long.parseLong(logFields[17]));
				log303.put("payment_id", Long.parseLong(logFields[18]));
				log303.put("state_id", Long.parseLong(logFields[19]));
				log303.put("count_type_id", Long.parseLong(logFields[20]));
				log303.put("ip_address", logFields[21]);
				log303.put("user_id", logFields[22]);
				log303.put("os_id", Long.parseLong(logFields[23]));
				log303.put("browser_id", Long.parseLong(logFields[24]));
				log303.put("browser_lang", Long.parseLong(logFields[25]));
				log303.put("tag_type", Long.parseLong(logFields[26]));
				log303.put("media_type_id", Long.parseLong(logFields[27]));
				log303.put("plc_content_type_id", Long.parseLong(logFields[28]));
				log303.put("ad_server_ip", logFields[29]);
				log303.put("ad_server_farm_id", Long.parseLong(logFields[30]));
				log303.put("dma_id", Long.parseLong(logFields[31]));
				log303.put("country_id", Long.parseLong(logFields[32]));
				log303.put("zipcode_id", Long.parseLong(logFields[33]));
				log303.put("city_id", Long.parseLong(logFields[34]));
				log303.put("isp_id", Long.parseLong(logFields[35]));
				log303.put("connection_type_id", Long.parseLong(logFields[36]));
				log303.put("area_code", Long.parseLong(logFields[37]));
				log303.put("til_id", Long.parseLong(logFields[38]));
				log303.put("event_name", "AOL303");
				publisher.publish(log303);
				log303 = null;
				x++;
				if (x%5000==0) {
					System.out.println("Sent 5,000 events.");
					Thread.sleep(1000);
					x = 0;
				}
			}
			publisher = null;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}