package ORM.connection;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDatabaseConnection {
    public static void main(String[] args) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();

        if (conn != null) {
            System.out.println("✅ Test riuscito: connessione attiva!");
            DatabaseConnection.closeConnection();
        } else {
            System.out.println("❌ Test fallito: connessione nulla!");
        }
    }
}
