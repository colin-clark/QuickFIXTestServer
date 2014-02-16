package com.cep.darkstar.dto;

import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.lex.GateAnalyzer;
import com.cep.darkstar.node.CEPEventsSendRunnable;
import com.cep.darkstar.node.DarkStarNode;
import com.eaio.uuid.UUID;

public class CNNNews extends AbstractDTO implements IDTO {
	static Logger logger = Logger.getLogger("com.cep.darkstar.DTO.CNNNews");
	private GateAnalyzer analyzer;
	
	public void doFunction(EventObject e) {
		if (logger.isDebugEnabled()) {
			logger.debug("***DJNews DTO called with Event");
		}
		timestamp(e);
		e.remove("function");
		String guid = (new UUID()).toString();
		//ByteBuffer rowKey = StringSerializer.get().toByteBuffer(guid);
		try {
			e.put("guid", guid);
		} catch (JSONException e1) {
			logger.error(e1.getMessage(), e1);
		}
		save(e);
		String story = null;
		if (e.has("story")) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("***DJNews DTO retrieving story from Event ");
				}
				story = e.getString("story");
			} catch (JSONException e1) {
				logger.error(e1.getMessage(), e1);
			}
			e.remove("story");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("***CNNNews DTO Sending Event");
		}
		CEPEventsSendRunnable.handleEventObject(e);
		if (story != null) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("***CNNNews DTO analyzing Story");
				}
				Set<String> symbols = analyzer.processDoc(story);
				if (logger.isDebugEnabled()) {
					logger.debug("***CNNNews DTO analysis returned symbols " + symbols.toString());
				}
				for(String symbol : symbols) {
					sendNamedEntity(symbol, guid);
				}
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
	}
	
	private void sendNamedEntity(String symbol, String guid) throws JSONException {
		EventObject event = new EventObject();
		event.put("type", "symbol");
		event.put("value", symbol);
		event.put("guid", guid);
		event.put("partition_on", symbol);
		event.setEventName("named_entities");
		if (logger.isDebugEnabled()) {
			logger.debug("CNNNews DTO Sending Event " + event);
		}
		CEPEventsSendRunnable.handleEventObject(event);
	}

	@Override
	public void run() {
		try {
			logger.info("Creating Gate Analyzer");
			analyzer = new GateAnalyzer(DarkStarNode.getGatePath());
			logger.info("Gate Analyzer created");
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}
		while(true) {
			try {
				doFunction(getNext());
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

}
