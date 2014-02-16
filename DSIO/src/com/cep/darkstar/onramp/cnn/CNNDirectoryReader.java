package com.cep.darkstar.onramp.cnn;

import static me.prettyprint.hector.api.factory.HFactory.createColumn;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cep.commons.EventObject;
import com.cep.darkstar.onramp.configuration.DefaultConsistencyLevel;
import com.cep.darkstar.onramp.configuration.djnews.ClientConfigInfo;
import com.cep.darkstar.onramp.configuration.djnews.ClientConfiguration;

public class CNNDirectoryReader {

	final static Logger logger = Logger.getLogger("com.cep.darkstar.onramp.cnn.CNNDirectoryReader");
	
	/*
	 * Offset into alphabet array for partition_on field
	 */
	private int offset = 0;
	
	/*
	 * Parameters which are configurable through yaml
	 */
	private String source;
	private String function;
	private String source_dir;
	private String log4j_file;
	private String darkStarNode;
	private int darkStarPort;
	private String clusterName;
	private String keyspace;
	private int delay;
	private String dest_dir;
	
	/*
	 * DarkStar Client api classes
	 */
	private Mutator<ByteBuffer> mutator;
	private StringSerializer se = StringSerializer.get();
	private ByteBufferSerializer bfs = ByteBufferSerializer.get();
	private Cluster cluster;
	private Keyspace keySpace;
	private final ConsistencyLevelPolicy policy = new DefaultConsistencyLevel();

	/*
	 * Classes for document handling
	 */
	private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	private Document currentDoc;
	private File currentFile;
	private File dest_dir_file;
	
	public void processMessages() throws IOException {
		logger.info("Entering message processing loop in directory " + source_dir);
		File dir = new File(source_dir);

		while (true) {
			// Get a list of XML files in the current directory
			FilenameFilter filter = new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return name.toUpperCase().endsWith(".XML");
			    }
			};
			String[] children = dir.list(filter);
			
			// If there are no XML files in the specified directory take a nap until some show up
			while (waitOnFile(children)) {
				waitOnFile();
				children = dir.list(filter);
			}
			
			// Parse each XML file and send it to the DS Cluster
			for (String file : children) {
				if (logger.isDebugEnabled()) {
					logger.debug("Parsing file: " + file);
				}
				try {
					DocumentBuilder db = factory.newDocumentBuilder();
					String filePath = dir+System.getProperty("file.separator") + file;
					currentDoc = db.parse(filePath);
					String headline = getHeadline(currentDoc);
					if (headline != null) {
						if (logger.isDebugEnabled()) {
							logger.debug("headline: " + headline);
						}
						String story = getStory(currentDoc);
						if (story == null) {
							story = "NO STORY";
						}
						if (logger.isDebugEnabled()) {
							logger.debug("story: " + story);
						}
						sendMessage(headline, story);
					}
					currentFile = new File(filePath);
					if (!( currentFile.renameTo(new File(dest_dir_file, currentFile.getName())) )) {
						throw new Exception("Unable to move processed file " + currentFile.getAbsolutePath()
								+ " to processed directory " + dest_dir);
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

	}
	
	private boolean waitOnFile(String[] files) {
		return (files == null || files.length == 0);
	}
	
	private void waitOnFile() {
		logger.info("No new .XML files found in directory, waiting...");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}		
	}
	
	private void sendMessage(String headline, String story) {
		EventObject message = new EventObject();
		try {
			message.put("headline", headline);
			message.put("source", source);
			message.put("story", story);
			message.put("function", function);
			message.setEventName("CNNHeadlines");
			message.put("partition_on", randomLetter());
			if (logger.isDebugEnabled()) {
				logger.debug("Sending Event: " + message.toString());
			}
			send(message);
			if (delay > 0) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
    private String randomLetter() {
    	String[] alpha = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		return alpha[offset++%26];
	}

	private void send(EventObject event) throws JSONException {
		ByteBuffer rowKey = se.toByteBuffer(event.getString("partition_on"));		
		mutator.addInsertion(rowKey, keyspace, createColumn(event.getEventName(), event.toString(), se, se));
		mutator.execute();
	}

	private String getStory(Document doc) {
		StringBuilder story = new StringBuilder();
		Element docElement = doc.getDocumentElement();
		Node body = docElement.getElementsByTagName("body").item(0);
		if(body != null) {
			NodeList paragraphs = body.getChildNodes();
			for (int i = 0; i < paragraphs.getLength(); i++) {
				Node paragraph = paragraphs.item(i);
				if (paragraph.getNodeName().equals("paragraph")) {
					story.append(paragraph.getTextContent());
				}
			}
		}
		return story.toString();
	}

	private String getHeadline(Document doc) {
		String headline = null;
		Element docElement = doc.getDocumentElement();
		Node headlineNode = docElement.getElementsByTagName("headline").item(0);
		if(headlineNode != null) {
			headline = headlineNode.getTextContent();
		}
		return headline;
	}

	private void init() {
		logger.info("initializing cluster connection");
		
		CassandraHostConfigurator hostConfig = 
			new CassandraHostConfigurator(darkStarNode+":"+String.valueOf(darkStarPort));
		cluster = HFactory.createCluster(clusterName, hostConfig);
		keySpace = HFactory.createKeyspace(keyspace, cluster, policy);
		mutator = HFactory.createMutator(keySpace, bfs);
		
		logger.info("cluster connection initialized");
	}

	private void setProperties(String configFile) throws Exception {
		// Create the configuration class from yaml
		ClientConfigInfo info = null;
		info = ClientConfiguration.getConfiguration(configFile);
		System.out.println("CNN Directory Reader is being configured using yaml config file " + configFile);
		
		// Set the configurable properties
		log4j_file = info.getLog4j_file();
		darkStarNode = info.getDarkStarNode();
		darkStarPort = info.getDarkStarPort();
		clusterName = info.getClusterName();
		keyspace = info.getKeyspace();
		source = info.getSource();
		function = info.getInput_function();
		source_dir = info.getNews_directory();
		delay = info.getDelay();
		dest_dir = info.getProcessed_directory();
		
		// Verify property values
		if (!(new File(log4j_file).exists())) {
			throw new Exception("Cannot find specified log4j file " + log4j_file);
		}
		if (!(new File(source_dir).isDirectory())) {
			throw new Exception("Invalid news_directory of " + source_dir);
		}
		if (!(new File(dest_dir).isDirectory())) {
			throw new Exception("Invalid processed_directory of " + dest_dir);
		}
		dest_dir_file = new File(dest_dir);
		
		// Configure logging
		PropertyConfigurator.configure(log4j_file);
		logger.info("CNN Directory Reader configuration complete");
	}
	
	public static void main(String[] args) {
        CNNDirectoryReader onramp = new CNNDirectoryReader();
		try {
			onramp.setProperties(args[0]);
			onramp.init();
			onramp.processMessages();
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
			e1.printStackTrace();
			System.exit(1);
		}
    }
	
}
