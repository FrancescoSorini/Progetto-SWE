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
        this.connection = connection;
    }

    // ====================================================================================
    // 1) CREATE
    // ====================================================================================
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

    // ====================================================================================
    // 2) READ by Id
    // ====================================================================================
    public User getUserById(int userId) throws SQLException {
        String sql = """
                    SELECT u.user_id, u.username, u.email, u.pwd, u.is_enabled, r.name AS role_name
                    FROM users u
                    JOIN roles r ON u.role_id = r.role_id
                    WHERE u.user_id = ?
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return mapResultSetToUser(rs); //definita più in basso
                }
            }
        }
        return null;
    }

    // ====================================================================================
    // 3) READ by username
    // ====================================================================================
    public User getUserByUsername(String username) throws SQLException {
        String sql = """
                    SELECT u.user_id, u.username, u.email, u.pwd, u.is_enabled, r.name AS role_name
                    FROM users u
                    JOIN roles r ON u.role_id = r.role_id
                    WHERE u.username = ?
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);

            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()){
                    return mapResultSetToUser(rs); //definita più in basso
                }
            }
        }
        return null;
    }

    // ====================================================================================
    // 4) READ all users
    // ====================================================================================
    public List<User> getAllUsers() throws SQLException {
        String sql = """
                    SELECT u.user_id, u.username, u.email, u.pwd, u.is_enabled, r.name AS role_name
                    FROM users u
                    JOIN roles r ON u.role_id = r.role_id
                """;
        List<User> users = new ArrayList<>();

        try( Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    // ====================================================================================
    // 5) UPDATE username
    // ====================================================================================
    public void updateUsername(int userId, String newUsername) throws SQLException {
        String sql = """
                    UPDATE users
                    SET username = ?
                    WHERE user_id = ?
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newUsername);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ====================================================================================
    // 6) UPDATE email
    // ====================================================================================
    public void updateEmail(int userId, String newEmail) throws SQLException {
        String sql = """
                    UPDATE users
                    SET email = ?
                    WHERE user_id = ?
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newEmail);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ====================================================================================
    // 7) UPDATE password
    // ====================================================================================
    public void updatePassword(int userId, String newPassword) throws SQLException {
        String sql = """
                    UPDATE users
                    SET pwd = ?
                    WHERE user_id = ?
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ====================================================================================
    // 8) UPDATE STATUS
    // ====================================================================================
    public void updateUserEnabled(int userId, boolean enabled) throws SQLException {
        String sql = """
                    UPDATE users
                    SET is_enabled = ?
                    WHERE user_id = ?
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, enabled);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ====================================================================================
    // 9) UPDATE ROLE
    // ====================================================================================
    public void updateUserRole(int userId, Role newRole) throws SQLException {
        String sql = """
                    UPDATE users
                    SET role_id = ?
                    WHERE user_id = ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, mapRoleToId(newRole)); //definita più in basso
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ====================================================================================
    // 10) DELETE
    // ====================================================================================
    public void deleteUser(int userId) throws SQLException {
        String sql = """
                    DELETE
                    FROM users
                    WHERE user_id = ?
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    // ====================================================================================
    // EXTRA: LOGIN VALIDATION
    // ====================================================================================
    public User validateLogin(String username, String passwordHash) throws SQLException {
        String sql = """
            SELECT u.user_id, u.username, u.email, u.password_hash, u.is_enabled, r.name AS role_name
            FROM users u
            JOIN roles r ON u.role_id = r.role_id
            WHERE u.username = ? AND u.password_hash = ? AND u.is_enabled = TRUE
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    // =====================================================================================
    // PRIVATE UTILITIES
    // ====================================================================================

    //Mappa un ResultSet a un oggetto User.
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User(rs.getString("username"));
        user.setUserId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("pwd"));
        user.setEnabled(rs.getBoolean("is_enabled"));
        user.setRole(Role.valueOf(rs.getString("role_name").toUpperCase()));
        return user;
    }

    //Mappa l'enum Role all'ID corrispondente nella tabella roles.
    private int mapRoleToId(Role role) {
        return switch (role) {
            case ADMIN -> 1;
            case ORGANIZER -> 2;
            case PLAYER -> 3;
        };
    }
}

