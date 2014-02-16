package com.cep.quickfix.client.impls;

import java.io.InputStream;

import com.cep.quickfix.client.interfaces.MessageReader;
import com.cep.quickfix.client.interfaces.MessageReaderCallback;

public class MessageReaderFactory {
	
	public static MessageReader getReader(String type, InputStream source, MessageReaderCallback callback) {
		MessageReader reader = null;
		if (type.equalsIgnoreCase("file")) {
			reader = new FileMessageReader(source, callback);
		}
		return reader;
	}

}
