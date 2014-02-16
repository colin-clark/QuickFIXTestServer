package com.cep.darkstar.node.configuration;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.constructor.Constructor;

public class YamlConfiguration {
	static Logger logger = Logger.getLogger("com.cep.darkstar.node.configuration.YamlConfiguration");

	public static YamlConfigInfo getConfiguration(InputStream configFile) throws Exception {
		BaseConstructor constructor = new Constructor(YamlConfigInfo.class);
		TypeDescription configDescription = new TypeDescription(YamlConfigInfo.class);
		((Constructor) constructor).addTypeDescription(configDescription);
		Yaml yaml = new Yaml(new Loader(constructor));

		YamlConfigInfo info = (YamlConfigInfo) yaml.load(configFile);
		logger.info("Configuration file " + configFile + " parsed by YAML");
		return info;
	}

}
