package com.cep.quickfix.client.impls;

import java.io.InputStream;
import java.util.GregorianCalendar;

import quickfix.Session;

import com.cep.quickfix.client.interfaces.MessageReader;
import com.cep.quickfix.client.interfaces.MessageReaderCallback;

@SuppressWarnings("unused")
public class FileMessageReader implements MessageReader {
	// The source of the fix messages we will be sending
	private final InputStream source;
	
	// Callback that actually sends the message. Having a callback instead of
	// having the reader send the message directly allows different implementations
	// to perform transformations on the message before it's sent if so desired.
	private final MessageReaderCallback callback;
	
	// The FIX session this reader is associated with
	private Session session;
	
	// id is used to generate the ClOrdID
	private int id = 0;
	
	// Calendar for filling in FIX message date/time fields
	private static final GregorianCalendar baseCalendar = new GregorianCalendar();
	
	// Defaults for certain fields 

	public FileMessageReader(InputStream source, MessageReaderCallback callback) {
		this.source = source;
		this.callback = callback;
	}

	@Override
	public void run() {

	}

	public String newClOrdId() {
		return String.valueOf(id++);
	}

	@Override
	public void setSession(Session session) {
		this.session = session;
	}

}
