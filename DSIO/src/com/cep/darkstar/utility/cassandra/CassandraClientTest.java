package com.cep.darkstar.utility.cassandra;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class CassandraClientTest {
	final static Logger logger = Logger.getLogger("com.cep.darkstar.utility.cassandra.CassandraClient");
	private MessageReaderCallback callback;
	
	/*
	 * Parameters which are configurable through yaml
	 */
	private String log4j_file;
	CassandraClientConfigInfo info;
	
	public void processMessages() throws IOException {
		CassandraMessageReaderTest reader = new CassandraMessageReaderTest(info);
		reader.read(callback);
		System.exit(0);
	}
	
	private void createCallback(String configFile) throws Exception {
		// Create the configuration class from yaml
		info = CassandraClientConfiguration.getConfiguration(configFile);
		System.out.println("Cassandra Client is being configured using yaml config file " + configFile);

		// Configure logging
		log4j_file = info.getLog4j_file();
		PropertyConfigurator.configure(log4j_file);
		logger.info("Replay Client configuration complete");
		
		// Configure callback
		String type = info.getCallback();
		if (type.equalsIgnoreCase("darkstar")) {
			logger.info("Creating DarkStar callback");
			callback = new SendToDarkStar(info);
		} else if (type.equalsIgnoreCase("file")) {
			logger.info("Creating File callback");
			callback = new SendToFile(info);
		} else {
			throw new Exception("Unsupported callback type " + callback);
		}
	}

	public static void main(String[] args) {
        CassandraClientTest client = new CassandraClientTest();
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
