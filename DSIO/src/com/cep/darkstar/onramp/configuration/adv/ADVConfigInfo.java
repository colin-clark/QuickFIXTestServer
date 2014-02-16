 package com.cep.darkstar.onramp.configuration.adv;

import com.cep.darkstar.onramp.configuration.BaseConfig;


public class ADVConfigInfo extends BaseConfig {
	private String file;
	private String log4j_file;
	
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public String getLog4j_file() {
		return log4j_file;
	}
	public void setLog4j_file(String log4j_file) {
		this.log4j_file = log4j_file;
	}
}
