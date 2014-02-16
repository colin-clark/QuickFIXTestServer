package com.cep.metadata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cep.commons.EntityField;
import com.cep.utils.ConfigurationException;

public class MySQLConnectionHelper implements ConnectionHelper {
	private static Logger logger = Logger.getLogger("com.cep.utils.MySQLConnectionHelper");
	
	// configurable params
	private String url;
	private String user;
	private String password;
	
	public static final String URL = "url";
	public static final String USER = "user";
	public static final String PASSWORD = "password";
	
	private static final String MAPS_QUERY = "SELECT ENTITY_NAME FROM QUERY_ENTITY WHERE ENTITY_TYPE = 'MAP'";
	private static final String FIELDS_QUERY_1 = "SELECT FIELD_NAME, FIELD_CLASS FROM QUERY_ENTITY_FIELDS WHERE FIELD_DEFINITION = '";
	private static final String VARIANT_QUERY = "SELECT ENTITY_NAME FROM QUERY_ENTITY WHERE ENTITY_TYPE = 'VARIANT'";
	private static final String ORDER_BY = "' ORDER BY FIELD_NAME";
	private static final String QUERY_ENTITIES_QUERY = "SELECT ENTITY_NAME, ENTITY_DESCRIPTION, ENTITY_QUERY_KEY, ENTITY_FIELD_DEFINITION FROM QUERY_ENTITY ORDER BY ENTITY_NAME";
	private static final String FIELDS_QUERY_2 = "SELECT FIELD_NAME, FIELD_TYPE, FIELD_LENGTH FROM QUERY_ENTITY_FIELDS WHERE FIELD_DEFINITION = '";

	private static final String ENTITY_NAME = "ENTITY_NAME";
	private static final String FIELD_CLASS = "FIELD_CLASS";
	private static final String FIELD_NAME = "FIELD_NAME";
	
	private static final String ENTITY_DESCRIPTION = "entity_description";
	private static final String ENTITY_QUERY_KEY = "entity_query_key";
	private static final String ENTITY_FIELD_DEFINITION = "entity_field_definition";
	private static final String SINGLE_QUOTE = "'";
	
	private static final String STRING = "String";
	private static final String LONG = "Long";
	private static final String DOUBLE = "Double";
	private static final String INTEGER = "Integer";
	private static final String EVENT_NAME = "event_name";
	
	public MySQLConnectionHelper() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void setURL(String url) {
		this.url = url;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	private Connection getConnection() throws SQLException {
		try {
			return DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			throw e;
		}
	}

	private void close(Connection connection) {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public List<MapEvent> getMapEvents() throws MetadataException {
		java.sql.Connection con = null;
		try {
			con = getConnection();
			Statement getEventNames = con.createStatement();
			Statement getEventFields = con.createStatement();

			String eventName = null;
			ResultSet eventNames = null;
		
			List<MapEvent> mapEvents = new ArrayList<MapEvent>();

			eventNames = getEventNames.executeQuery(MAPS_QUERY);
			while (eventNames.next()) {
				eventName = eventNames.getString(ENTITY_NAME);
				ResultSet eventFields = getEventFields.executeQuery(FIELDS_QUERY_1 + eventName + ORDER_BY);
				MapEvent event = new MapEvent();
				event.setEventName(eventName);
			
				while (eventFields.next()) {
					if (eventFields.getString(FIELD_CLASS).equals(STRING)) {
						event.addField(eventFields.getString(FIELD_NAME), String.class);
					}
					if (eventFields.getString(FIELD_CLASS).equals(LONG)) {
						event.addField(eventFields.getString(FIELD_NAME), Long.class);
					}
					if (eventFields.getString(FIELD_CLASS).equals(DOUBLE)) {
						event.addField(eventFields.getString(FIELD_NAME), Double.class);
					}
					if (eventFields.getString(FIELD_CLASS).equals(INTEGER)) {
						event.addField(eventFields.getString(FIELD_NAME), Integer.class);
					}
				}
				event.addField(EVENT_NAME, String.class);
				mapEvents.add(event);
			}
			return mapEvents;
		} catch (Exception e) {
			throw new MetadataException(e);
		} finally {
			close(con);
		}
	}

	@Override
	public List<String> getVariantEvents() throws MetadataException {
		List<String> variantEvents = new ArrayList<String>();
		java.sql.Connection con = null;

		try {
			con = getConnection();
			Statement getEventNames = con.createStatement();
			ResultSet eventNames = getEventNames.executeQuery(VARIANT_QUERY);
			while (eventNames.next()) {
				variantEvents.add(eventNames.getString(ENTITY_NAME));
			}
			return variantEvents;
		} catch (Exception e) {
			throw new MetadataException(e);
		} finally {
			close(con);
		}
	}

	@Override
	public void setParams(Map<String, String> params) throws ConfigurationException {
		if(params.containsKey(URL)) {
			setURL(params.get(URL));
			if (logger.isDebugEnabled()) {
				logger.debug("url set to " + params.get(URL));
			}
		} else {
			throw new ConfigurationException("Parameter url must be specified");
		}
		if(params.containsKey(USER)) {
			setUser(params.get(USER));
			if (logger.isDebugEnabled()) {
				logger.debug("user set to " + params.get(USER));
			}
		} else {
			throw new ConfigurationException("Parameter user must be specified");
		}
		if(params.containsKey(PASSWORD)) {
			setPassword(params.get(PASSWORD));
			if (logger.isDebugEnabled()) {
				logger.debug("password set to " + params.get(PASSWORD));
			}
		} else {
			throw new ConfigurationException("Parameter password must be specified");
		}
	}

	@Override
	public QueryEntity[] getQueryEntities() throws MetadataException {
		List<QueryEntity> list = new ArrayList<QueryEntity>();
		java.sql.Connection con = null;
		try {
			con = getConnection();

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(QUERY_ENTITIES_QUERY);
			
			while (rs.next()) {
				list.add(new QueryEntity(rs.getString(ENTITY_NAME),
						rs.getString(ENTITY_DESCRIPTION),
						rs.getString(ENTITY_QUERY_KEY),
						rs.getString(ENTITY_FIELD_DEFINITION)));
			}
			
			QueryEntity[] queryEntities = new QueryEntity[list.size()];
			
			queryEntities = list.toArray(queryEntities);
			return queryEntities;

		} catch ( Exception e ) { 
			throw new MetadataException(e);
		} finally {
			close(con);
		}
	}

	@Override
	public EntityField[] getQueryEntityFields(String entityFieldDefinition) throws MetadataException {
		List<EntityField> list = new ArrayList<EntityField>();
		java.sql.Connection con = null;
		try {
			con = getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(FIELDS_QUERY_2 + entityFieldDefinition + SINGLE_QUOTE);
			while (rs.next()) {
				list.add(new EntityField(rs.getString("field_name"),
						"FIELD DESCRIPTION",
						rs.getString("field_type"),
						rs.getInt("field_length")));
			}
			
			EntityField[] queryEntityFields = new EntityField[list.size()];
			queryEntityFields = list.toArray(queryEntityFields);
			
			return queryEntityFields;
		} catch ( Exception e ) { 
			throw new MetadataException(e);
		} finally {
			close(con);
		}
	}
}

