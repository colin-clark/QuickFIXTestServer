package com.cep.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class MySQLConnectionHelper {
	private static Logger logger = Logger.getLogger("com.cep.utils.MySQLConnectionHelper");
	private static String url;
	private static String user;
	private static String pwd;
	private static MySQLConnectionHelper instance;
	
	private MySQLConnectionHelper(String url)
	{
		try {
			Class.forName("com.mysql.jdbc.Driver");
			MySQLConnectionHelper.url = url;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static Connection getConnection() throws SQLException {
		return getConnection(url);
	}
	
	public static void setURL(String url) {
		MySQLConnectionHelper.url = url;
	}
	
	public static void setUser(String user) {
		MySQLConnectionHelper.user = user;
	}
	
	public static void setPwd(String pwd) {
		MySQLConnectionHelper.pwd = pwd;
	}
	
	public static Connection getConnection(String url) throws SQLException {
		if (instance == null) {
			instance = new MySQLConnectionHelper(url);
		}
		try {
			return DriverManager.getConnection(url, user, pwd);
		} catch (SQLException e) {
			throw e;
		}
	}

	// shut it down
	public static void close(Connection connection)
	{
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}
}

