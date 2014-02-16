package com.cep.darkstar.onramp.weatherstats;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.cep.commons.EventObject;
import com.cep.darkstar.onramp.IOnRamp;
import com.cep.darkstar.onramp.AbstractOnRamp;
import com.cep.darkstar.onramp.configuration.weatherstats.WeatherStatsConfigInfo;
import com.cep.darkstar.onramp.configuration.weatherstats.WeatherStatsConfiguration;

public class WeatherStatsOnRamp extends AbstractOnRamp implements IOnRamp {
	
	final static Logger logger = Logger.getLogger("com.cep.darkstar.onramp.weatherstats.WeatherStatsOnRamp");
    private String log4j_file;
    private int delay;
	
	public WeatherStatsOnRamp() {
	}

	//@Override
	public void setProperties(String properties) {
		
		WeatherStatsConfigInfo info = null;
		try {
			info = WeatherStatsConfiguration.getConfiguration(properties);
			super.setProperties(info);
			
			delay = info.getDelay();
			log4j_file = info.getLog4j_file();
			PropertyConfigurator.configure(log4j_file);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void send(EventObject event, String key) throws JSONException {
		// Not used.
	}

	@Override
	public void send(EventObject event) throws JSONException {
		if (event.has("observation_time_rfc822")) {
			event.put("observation_time_rfc", event.get("observation_time_rfc822"));
		}
		if (event.has("wind_dir")) {
			event.put("partition_on", event.get("wind_dir"));
		} else {
			event.put("partition_on", "Z");
		}
		event.setEventName("WeatherStats");
		super.send(event);
		if (delay > 0) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

/*
	Requires two arguments when running, first is the WeatherStats.yaml, second is the path to the
	the folder with xml files to use as input. Use slashes in the folder path, with no ending slash. 
*/
    public static void main(String[] args) {
    	
        WeatherStatsOnRamp onramp = new WeatherStatsOnRamp();
        
		try {
			onramp.setProperties(args[0]);
			onramp.init();
			//folder with xml files to read into onramp
			String dir = args[1] + "/";
		    File folder = new File(dir);
		    File[] listOfFiles = folder.listFiles();

		    for (int i = 0; i < listOfFiles.length; i++) {
		    	if (listOfFiles[i].isFile()) {
		    		// System.out.println(args[1] + listOfFiles[i].getName());
		    		String file = dir + listOfFiles[i].getName();
		    	  
		    		File f = new File(file);
		    		InputStream is = new FileInputStream(f);
		        
		    		String xml = IOUtils.toString(is);
		    		//System.out.println(xml);
	            
		    		JSONObject jsonObject = XML.toJSONObject(xml);
	                      
		    		String fileOut = jsonObject.toString();           
		    		fileOut = fileOut.substring(23, fileOut.length()-1);
	               
		    		//send fileout to  the EventObject
	            
		    		// System.out.println(fileOut);
		    		// System.out.println(jsonObject);

		    		EventObject event = new EventObject(fileOut);
		    		onramp.send(event);
		    	}
		    }
		} catch (Exception e1) {
			System.out.println(args[0]+" is not a valid configuration file or "+args[1]+" is not a valid path");
			e1.printStackTrace();
		}
    }
    
}
