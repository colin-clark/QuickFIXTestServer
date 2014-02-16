package com.cep.darkstar.utility.cassandra;

import java.io.File;
import java.io.FileInputStream;

import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.constructor.Constructor;

public class CassandraClientConfiguration {

	public static CassandraClientConfigInfo getConfiguration(String configFile) throws Exception {
		BaseConstructor constructor = new Constructor(CassandraClientConfigInfo.class);
		TypeDescription configDescription = new TypeDescription(CassandraClientConfigInfo.class);
		configDescription.putListPropertyType("headers", String.class);
		((Constructor) constructor).addTypeDescription(configDescription);
		Yaml yaml = new Yaml(new Loader(constructor));

		CassandraClientConfigInfo info = (CassandraClientConfigInfo) yaml.load(new FileInputStream(new File(configFile)));
		return info;
	}

}
