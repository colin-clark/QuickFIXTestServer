package com.cep.darkstar.utility.cassandra;

import java.util.List;

public class FileClientConfigInfo {
	protected String log4j_file;
	protected String callback;
	protected String file;
	protected String inputFile;
	protected List<String> headers;
	protected boolean useTransactTime = true;
	
	public String getLog4j_file() {
		return log4j_file;
	}
	public void setLog4j_file(String log4j_file) {
		this.log4j_file = log4j_file;
	}
	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public List<String> getHeaders() {
		return headers;
	}
	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}
	public String getInputFile() {
		return inputFile;
	}
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}
	public boolean isUseTransactTime() {
		return useTransactTime;
	}
	public void setUseTransactTime(boolean useTransactTime) {
		this.useTransactTime = useTransactTime;
	}

}
