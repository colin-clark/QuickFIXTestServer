package com.cep.darkstar.onramp.configuration.weatherstats;

import java.io.File;
import java.io.FileInputStream;

import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.constructor.Constructor;



public class WeatherStatsConfiguration {

	public static WeatherStatsConfigInfo getConfiguration(String configFile) throws Exception {
		
		BaseConstructor constructor = new Constructor(WeatherStatsConfigInfo.class);
		TypeDescription configDescription = new TypeDescription(WeatherStatsConfigInfo.class);
		((Constructor) constructor).addTypeDescription(configDescription);
		Yaml yaml = new Yaml(new Loader(constructor));

		WeatherStatsConfigInfo info = (WeatherStatsConfigInfo) yaml.load(new FileInputStream(new File(configFile)));
		return info;
	}
	
}
