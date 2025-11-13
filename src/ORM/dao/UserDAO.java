package ORM.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import ORM.connection.DatabaseConnection;

import DomainModel.user.Role;
import DomainModel.user.User;

public class UserDAO {
    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = DatabaseConnection.getConnection();
    }

    // --- CREATE USER ---
    public void createUser(User user) throws SQLException {
        String sql = """
            INSERT INTO users (username, email, pwd, is_enabled, role_id)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setBoolean(4, user.isEnabled());
            ps.setInt(5, user.getRole());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setUserId(rs.getInt(1));
                }
            }
        }
    }

    // --- READ (by Id) ---

}
