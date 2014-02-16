package com.cep.darkstar.onramp.configuration.appia;

import java.io.File;
import java.io.FileInputStream;

import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.constructor.Constructor;


public class AppiaConfiguration {

	public static AppiaConfig getConfiguration(String configFile) throws Exception {
		BaseConstructor constructor = new Constructor(AppiaConfig.class);
		TypeDescription configDescription = new TypeDescription(AppiaConfig.class);
		configDescription.putListPropertyType("eventMapping", FixEventMapping.class);
		((Constructor) constructor).addTypeDescription(configDescription);
		Yaml yaml = new Yaml(new Loader(constructor));

		AppiaConfig info = (AppiaConfig) yaml.load(new FileInputStream(new File(configFile)));
		return info;
	}
}
