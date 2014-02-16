package scratchpad;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.cep.utils.MySQLConnectionHelper;

public class TestMySQLConnection {

	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		java.sql.Connection con = MySQLConnectionHelper.getConnection();
		Statement getEventNames = con.createStatement();
		ResultSet eventNames = getEventNames.executeQuery("SELECT ENTITY_NAME FROM QUERY_ENTITY WHERE ENTITY_TYPE = 'VARIANT'");
		while (eventNames.next()) {
			System.out.println(eventNames.toString());
		}
		con.close();
	}
}
