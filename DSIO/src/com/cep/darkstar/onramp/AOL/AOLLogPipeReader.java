/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.onramp.AOL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.pubsub.pub.PublishTopic;

/**
 * @author colin
 *
 */
public class AOLLogPipeReader {
	
	public static String stripGarbage(String s) {  
        String good = " @#abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789éãÜT:";
        String result = "";
        for ( int i = 0; i < s.length(); i++ ) {
            if ( good.indexOf(s.charAt(i)) >= 0 )
               result += s.charAt(i);
            }
        return result;
    }
	
	public static void main(String[] args) {
		Pattern pattern = Pattern.compile("^(\\S+) ([\\S.]+) (\\d+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(?:GET|POST|HEAD) (\\S+) (\\S+)\" (\\d+) (\\d+) \"(\\S+)\" \"(.+)\" (\\S+) \"(\\S+)\" \"(.+)\"");

		PublishTopic publisher;

		try {
			//BufferedReader in = new BufferedReader(new FileReader("access.log"));
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String str;
			publisher = new PublishTopic.Builder().hostName("64.21.255.71").build();
			//publisher = new PublishTopic.Builder().build();

			while ((str = in.readLine()) != null) {
				try {
					Matcher matcher = pattern.matcher(str);
					if (matcher.matches()) {	
/*						System.out.println("Unique ID   :"+matcher.group(1));
						System.out.println("site        :"+matcher.group(2));
						System.out.println("some number :"+matcher.group(3));
						System.out.println("timestamp   :"+matcher.group(4));
						System.out.println("HTTP request:"+matcher.group(5));
						System.out.println("Filler      :"+matcher.group(6));
						System.out.println("Return code :"+matcher.group(7));
						System.out.println("Bytes sent  :"+matcher.group(8));
						System.out.println("Referrer    :"+matcher.group(9));
						System.out.println("Broswer info:"+matcher.group(10));
						System.out.println("RSP Cookie  :"+matcher.group(11));
						System.out.println("Location    :"+matcher.group(12));
						System.out.println("IP address  :"+matcher.group(13));*/
					} else {
						//System.out.println("No match!");
					}
					// start decoding data into event map
					EventObject logEntry = new EventObject();
					// unique id, site & timestamp
					try {
						logEntry.put("unique_id", matcher.group(1));
						logEntry.put("site", matcher.group(2));
						logEntry.put("time_stamp", matcher.group(3));
						// split up the url request
						String URL;
						String temp[];
						// arg list
						URL = matcher.group(5);
						//System.out.println("BASE &ARGS:"+URL);
						temp = URL.split("\\?");
						logEntry.put("action",stripGarbage(temp[0]));
						String argslist[];
						argslist = temp[1].split("\\&");
						for(int i =0; i < argslist.length ; i++) {
							//System.out.println(URLDecoder.decode(argslist[i], "UTF-8"));
							String field[];
							field = URLDecoder.decode(argslist[i], "UTF-8").split("=");
							if (field.length == 1) {
								logEntry.put(field[0], "null");
							} else {
									logEntry.put(field[0], field[1]);
							}
						}
						// return code, bytes sent, referrer
						logEntry.put("return_code", Long.parseLong(matcher.group(7)));
						logEntry.put("bytes_sent", Long.parseLong(matcher.group(8)));
						logEntry.put("referrer", matcher.group(9));
						logEntry.put("browser", matcher.group(10));
						if (matcher.group(11).length() < 10) {
							logEntry.put("rsp_cookie", matcher.group(11));
						} else {
							logEntry.put("rsp_cookie", matcher.group(11).substring(11));
						}

						// grab all the location data
						String location = matcher.group(12);
						argslist = location.split(",");
						for(int i =0; i < argslist.length ; i++) {
							//System.out.println(URLDecoder.decode(argslist[i], "UTF-8"));
							String field[];
							field = URLDecoder.decode(argslist[i], "UTF-8").split("=");
							if (field.length == 1) {
								logEntry.put(field[0], "null");
							} else {
								logEntry.put(field[0], field[1]);
							}
						}
						// add event type
						logEntry.setEventName("AOLLog");
						//System.out.println(logEntry.toString());
						publisher.publish(logEntry);
						//System.out.println("-------------------------------------");

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		} catch (IOException e) {
		}
	}
}