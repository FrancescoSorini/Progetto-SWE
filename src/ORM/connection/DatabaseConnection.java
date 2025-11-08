package ORM.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/ProgettoSWE";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin";

    private static Connection connection = null;

    private DatabaseConnection() { }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connessione al database stabilita.");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver PostgreSQL non trovato: " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("❌ Errore nella connessione: " + e.getMessage());
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("🔒 Connessione chiusa.");
            } catch (SQLException e) {
                System.err.println("❌ Errore nella chiusura della connessione: " + e.getMessage());
            }
        }
    }
}