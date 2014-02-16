package com.cep.utils;

import java.sql.SQLException;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;

public class OracleConnectionHelper {
	private static String url;
	private static OracleConnectionHelper instance = null;

	private OracleConnectionHelper(String url)
	{
		try {
			OracleConnectionHelper.url = url;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static OracleConnection getConnection(String url, String user, String pwd)  {
		OracleConnection conn = null;
		if (instance == null) {
			instance = new OracleConnectionHelper(url);
		}
		try {
			OracleDataSource ods = new OracleDataSource();
			ods.setUser(user);
			ods.setPassword(pwd);
			ods.setURL(OracleConnectionHelper.url);
			conn = (OracleConnection) ods.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	public static void close(OracleConnection connection)
	{
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

