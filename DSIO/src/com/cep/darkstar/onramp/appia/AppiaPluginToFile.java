package com.cep.darkstar.onramp.appia;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONException;

import com.javtech.appia.ef.session.DefaultSessionPlugin;
import com.javtech.appia.ef.session.InboundPostValidationPlugin;
import com.javtech.appia.ef.session.OutboundPostValidationPlugin;
import com.javtech.appia.ef.session.SessionPluginFilter;
import com.javtech.javatoolkit.message.MessageObject;


public class AppiaPluginToFile extends DefaultSessionPlugin 
		implements InboundPostValidationPlugin,OutboundPostValidationPlugin {
	private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	
	private SimpleDateFormat fixFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss:SSS");
	
	private String fileName;

	@SuppressWarnings("rawtypes")
	public AppiaPluginToFile(String plugin_name, SessionPluginFilter interest_filter, Map plugin_args) throws IOException {
		super(plugin_name, interest_filter);
		setProperties(plugin_args);
		startDequeueThread();
	}
	
	private void startDequeueThread() {
		new Thread() {
			String event;
			private FileWriter fstream;
			private BufferedWriter writer = null;
			
			public void run() {
				try {
					fstream = new FileWriter(fileName);
					writer = new BufferedWriter(fstream);
					while(true) {
						try {
							event = queue.take();
							writer.write(event);
							writer.newLine();
							writer.flush();
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} 
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				} finally {
					if (writer != null) {
						try {
							writer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}.start();
	}

	@Override
	public void processOutboundMessagePostValidation(MessageObject message, CallContext context) {
		processMessage(message);		
	}

	@Override
	public void processInboundMessagePostValidation(MessageObject message, CallContext context) {
		processMessage(message);
	}

	public void processMessage(MessageObject message) {
		String formatted = fixFormat.format(new Date());
		StringBuffer toSend = new StringBuffer().append(formatted).append(": ").append(message.toString());
		try {
			send(toSend.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
    @SuppressWarnings("rawtypes")
	public void setProperties(Map info) throws IOException {
		fileName = (String)info.get("fileName");
    }
    
    private void send(String event) throws JSONException {
    	try {
			queue.put(event);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
