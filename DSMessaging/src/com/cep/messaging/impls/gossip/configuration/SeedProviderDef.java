package com.cep.messaging.impls.gossip.configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SeedProviderDef {
	public String class_name;
	public Map<String, String> parameters;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SeedProviderDef(LinkedHashMap p) {
		class_name = (String) p.get("class_name");
		parameters = (Map<String, String>) ((List) p.get("parameters")).get(0);
	}
}
