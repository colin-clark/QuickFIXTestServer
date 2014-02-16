package com.cep.darkstar.utility.cassandra;

import java.io.File;
import java.io.FileInputStream;

import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.constructor.Constructor;

public class FileClientConfiguration {

	public static FileClientConfigInfo getConfiguration(String configFile) throws Exception {
		BaseConstructor constructor = new Constructor(FileClientConfigInfo.class);
		TypeDescription configDescription = new TypeDescription(FileClientConfigInfo.class);
		configDescription.putListPropertyType("headers", String.class);
		((Constructor) constructor).addTypeDescription(configDescription);
		Yaml yaml = new Yaml(new Loader(constructor));

		FileClientConfigInfo info = (FileClientConfigInfo) yaml.load(new FileInputStream(new File(configFile)));
		return info;
	}

}
