package ORM.connection;

import java.sql.Connection;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        Connection conn = DatabaseConnection.getConnection();

        if (conn != null) {
            System.out.println("✅ Test riuscito: connessione attiva!");
            DatabaseConnection.closeConnection();
        } else {
            System.out.println("❌ Test fallito: connessione nulla!");
        }
    }
}
