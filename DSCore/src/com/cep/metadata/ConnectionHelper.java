package com.cep.metadata;

import java.util.List;
import java.util.Map;

import com.cep.commons.EntityField;
import com.cep.utils.ConfigurationException;


public interface ConnectionHelper {

	/**
	 * @return a list of Map<String,Object>, with each map representing a single
	 * event type and each name-value pair in the map containing the name of 
	 * one of the fields for the event type as the "name" and the data type of
	 * that field as the "value".
	 * @throws MetadataException 
	 */
	public List<MapEvent> getMapEvents() throws MetadataException;
	
	/**
	 * @return a list of the names of the event types defined in the metadata as
	 * being Variant Events.
	 * @throws MetadataException 
	 */
	public List<String> getVariantEvents() throws MetadataException;
	
	/**
	 * @param params a Map of 'parameter name'-'parameter value' pairs for the
	 * Connection Helper to use to set it's parameters.
	 * @throws ConfigurationException 
	 */
	public void setParams(Map<String,String> params) throws ConfigurationException;
	
	/**
	 * @return an array of QueryEntity Objects comprising the entire set of event 
	 * types defined in the metadata.
	 * @throws MetadataException 
	 */
	public QueryEntity[] getQueryEntities() throws MetadataException;
	
	/**
	 * @param entityFieldDefinition the Event type for which the fields should
	 * be returned.
	 * @return an array of EntityField Objects detailing all of the fields defined
	 * for the given Event type.
	 * @throws MetadataException 
	 */
	public EntityField[] getQueryEntityFields(String entityFieldDefinition) throws MetadataException;
	
}
