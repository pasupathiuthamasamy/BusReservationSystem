package databaseconnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_URL="jdbc:mysql://localhost:3306/bus_rental_system";
    private static final String DB_USER="root";
    private static final String DB_PASS="asdf";
    public static Connection getConnection() throws SQLException{
    	return DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
    }

}
