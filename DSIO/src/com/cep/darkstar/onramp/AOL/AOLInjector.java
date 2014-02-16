package com.cep.darkstar.onramp.AOL;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.pubsub.pub.PublishTopic;

public class AOLInjector {
	public static final String EXAMPLE_TEST = "dc2f6778c6c611df8fa90fa486bd7be0 b.aol.com 2456971 [23/Sep/2010:00:00:00 -0400] \"GET /ping?ts=1285214399671&h=www.aol.com&v=9&t=AOL.com%20-%20Welcome%20to%20AOL&r=&l=1625&ms=19843&nm=dl&dL_ch=us.aolportal&ver=" +
	"1&dL_dpt=main%20AOL.com%20Main%2010x7&template=main&cobrand=main&kb=134&plid=172244&dtid=offlede&mnid=dl4&mpid=1&mlid=cwell HTTP1/1.1\" 200 43 \"http://www.aol.com/\" "+
	"\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; GTB0.0; Media Center PC 3.0; .NET CLR 1.0.3705; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)\" "+
	"RSP_COOKIE=%2B1U7zz6gXJuEv5r14sYTfA%3D%3D "+
	"\"city=nanuet,region=ny,country=usa,postal_code=,conn_type=cable,latitude=041.098,longitude=-074.009,county_conf=5,region_conf=4,city_conf=4,status=ok\" "+
	"\"0:12:0:0:40000000001,x-lb-client-ip:PPPPPPPP_P\"";
	public static void main(String[] args) {
		Pattern pattern = Pattern.compile("^(\\S+) ([\\S.]+) (\\d+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(?:GET|POST|HEAD) (\\S+) (\\S+)\" (\\d+) (\\d+) \"(\\S+)\" \"(.+)\" (\\S+) \"(\\S+)\" \"(.+)\"");

		PublishTopic publisher;

		try {
			BufferedReader in = new BufferedReader(new FileReader("access.log"));
			String str;
			publisher = new PublishTopic.Builder().hostName("64.21.255.71").build();

			while ((str = in.readLine()) != null) {
				try {
					Matcher matcher = pattern.matcher(str);
					if (matcher.matches()) {	
						System.out.println("Unique ID   :"+matcher.group(1));
						System.out.println("site        :"+matcher.group(2));
						System.out.println("some number :"+matcher.group(3));
						System.out.println("timestamp   :"+matcher.group(4));
						System.out.println("HTTP request:"+matcher.group(5));
						//System.out.println("Decoded     :"+URLDecoder.decode(matcher.group(5), "UTF-8"));
						System.out.println("Filler      :"+matcher.group(6));
						System.out.println("Return code :"+matcher.group(7));
						System.out.println("Bytes sent  :"+matcher.group(8));
						System.out.println("Referrer    :"+matcher.group(9));
						System.out.println("Broswer info:"+matcher.group(10));
						System.out.println("RSP Cookie  :"+matcher.group(11));
						System.out.println("Location    :"+matcher.group(12));
						System.out.println("IP address  :"+matcher.group(13));
					} else {
						System.out.println("No match!");
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
						System.out.println("BASE &ARGS:"+URL);
						temp = URL.split("\\?");
						logEntry.put("action",temp[0]);
						String argslist[];
						argslist = temp[1].split("\\&");
						for(int i =0; i < argslist.length ; i++) {
							System.out.println(URLDecoder.decode(argslist[i], "UTF-8"));
							String field[];
							field = URLDecoder.decode(argslist[i], "UTF-8").split("=");
							if (field.length == 1) {
								logEntry.put(field[0], "null");
							} else {
								logEntry.put(field[0], field[1]);
							}
						}
						// return code, bytes sent, referrer
						logEntry.put("return_code", matcher.group(7));
						logEntry.put("bytes_sent", matcher.group(8));
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
							System.out.println(URLDecoder.decode(argslist[i], "UTF-8"));
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
						System.out.println(logEntry.toString());
						publisher.publish(logEntry);
						System.out.println("-------------------------------------");

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			in.close();
		} catch (IOException e) {
		}
	}
}