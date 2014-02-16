package com.cep.darkstar.onramp.adv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


import com.cep.commons.EventObject;
import com.cep.darkstar.onramp.AbstractOnRamp;
import com.cep.darkstar.onramp.configuration.adv.ADVConfigInfo;
import com.cep.darkstar.onramp.configuration.adv.ADVConfiguration;

public class ADVOnramp extends AbstractOnRamp {
	final static Logger logger = Logger.getLogger("com.cep.darkstar.onramp.adv.ADVOnramp");
	
    // Properties
	private String fileName;
	private String log4j_file;
    
	public ADVOnramp() {
	}
    
    public void setProperties(String properties) {
		ADVConfigInfo info = null;
		try {
			info = ADVConfiguration.getConfiguration(properties);
			super.setProperties(info);
			fileName = info.getFile();
			log4j_file = info.getLog4j_file();
			PropertyConfigurator.configure(log4j_file);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
    }
    
	private void processMessages() {
		// Read the file in, parse it out and send the ADV info 
		// 	into super.send(EventObject);
		try {
			File file = new File(fileName);
			if (file.exists()) {
				EventObject message = new EventObject();
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						String[] tokens = line.split(",");
						message.put("Symbol", tokens[0]);
						message.put("ADVToday", Double.valueOf(tokens[1]));
						message.put("ADV30Day", Double.valueOf(tokens[2]));
						message.setEventName("ADV");
						message.put("partition_on", tokens[0]);
						if (logger.isDebugEnabled()) {
							logger.debug("Sending Event: " + message.toString());
						}
						super.send(message);
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				logger.error("File " + fileName + " does not exist");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		logger.info("File read complete, ADVOnramp shutting down");
		System.exit(0);
	}

	/**
     * Main method
     * @param args command line arguments
     */
    public static void main(String[] args) {
        ADVOnramp onramp = new ADVOnramp();
		try {
			onramp.setProperties(args[0]);
			onramp.init();
			onramp.processMessages();
		} catch (Exception e1) {
			System.out.println("Unable to use "+args[0]+" as a valid configuration file.");
			e1.printStackTrace();
		}
    }

}
