package com.cep.metadata;

import org.json.JSONException;

import com.cep.commons.EventObject;

public class QueryEntity extends EventObject {
	
	private final String entityName = "entityName";
	private final String entityDescription = "entityDescription";
	private final String entityQueryKey = "entityQueryKey";
	private final String entityFieldDefinition = "entityFieldDefinition";

	public QueryEntity(String entityName, String entityDescription,
			String entityQueryKey, String entityFieldDefinition) throws JSONException {
		super();
		this.put(this.entityName, entityName);
		this.put(this.entityDescription, entityDescription);
		this.put(this.entityQueryKey, entityQueryKey);
		this.put(this.entityFieldDefinition, entityFieldDefinition);
	}

	public String getEntityFieldDefinition() throws JSONException {
		return this.get(entityFieldDefinition).toString();
	}

	public String getEntityName() throws JSONException {
		return this.get(entityName).toString();
	}

	public String getEntityDescription() throws JSONException {
		return this.get(entityDescription).toString();
	}

	public String getEntityQueryKey() throws JSONException {
		return this.get(entityQueryKey).toString();
	}

	public void setEntityName(String entityName) throws JSONException {
		this.put(this.entityName, entityName);
	}

	public void setEntityDescription(String entityDescription) throws JSONException {
		this.put(this.entityDescription, entityDescription);
	}

	public void setEntityQueryKey(String entityQueryKey) throws JSONException {
		this.put(this.entityQueryKey, entityQueryKey);
	}
	
	public void setEntityFieldDefinition(String entityFieldDefinition) throws JSONException {
		this.put(this.entityFieldDefinition, entityFieldDefinition);
	}
}
