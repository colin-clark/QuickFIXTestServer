package com.cep.darkstar.onramp.configuration.arca.trf;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.constructor.Constructor;


public class ClientConfiguration {
	static Logger logger = Logger.getLogger("com.cep.darkstar.client.configuration.ClientConfiguration");

	public static ClientConfigInfo getConfiguration(String configFile) throws Exception {
		BaseConstructor constructor = new Constructor(ClientConfigInfo.class);
		TypeDescription configDescription = new TypeDescription(ClientConfigInfo.class);
		((Constructor) constructor).addTypeDescription(configDescription);
		Yaml yaml = new Yaml(new Loader(constructor));

		ClientConfigInfo info = (ClientConfigInfo) yaml.load(new FileInputStream(new File(configFile)));
		logger.info("Configuration file " + configFile + " has been loaded by DarkStarClient");
		return info;
	}

}
