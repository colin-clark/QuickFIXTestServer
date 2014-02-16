package com.cep.darkstar.utility.cassandra;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class FileClient {
	final static Logger logger = Logger.getLogger("com.cep.darkstar.utility.cassandra.FileClient");
	private MessageReaderCallback callback;
	
	/*
	 * Parameters which are configurable through yaml
	 */
	private String log4j_file;
	CassandraClientConfigInfo info;
	
	public void processMessages() throws IOException {
		FileMessageReader reader = new FileMessageReader(info);
		reader.read(callback);
		System.exit(0);
	}
	
	private void createCallback(String configFile) throws Exception {
		// Create the configuration class from yaml
		info = CassandraClientConfiguration.getConfiguration(configFile);
		System.out.println("File Client is being configured using yaml config file " + configFile);

		// Configure logging
		log4j_file = info.getLog4j_file();
		PropertyConfigurator.configure(log4j_file);
		logger.info("File Client configuration complete");

		logger.info("Creating File callback");
		callback = new SendToFile(info);
	}

	public static void main(String[] args) {
        FileClient client = new FileClient();
		try {
			client.createCallback(args[0]);
			client.processMessages();
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
			e1.printStackTrace();
			System.exit(1);
		}
    }

}
