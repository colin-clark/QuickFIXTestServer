package com.cep.darkstar.onramp.configuration.adv;

import java.io.File;
import java.io.FileInputStream;

import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.constructor.Constructor;


public class ADVConfiguration {

	public static ADVConfigInfo getConfiguration(String configFile) throws Exception {
		BaseConstructor constructor = new Constructor(ADVConfigInfo.class);
		TypeDescription configDescription = new TypeDescription(ADVConfigInfo.class);
		((Constructor) constructor).addTypeDescription(configDescription);
		Yaml yaml = new Yaml(new Loader(constructor));

		ADVConfigInfo info = (ADVConfigInfo) yaml.load(new FileInputStream(new File(configFile)));
		return info;
	}

}
