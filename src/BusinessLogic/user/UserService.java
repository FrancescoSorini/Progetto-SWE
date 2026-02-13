package BusinessLogic.user;

import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.dao.UserDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class UserService {

    private final UserDAO userDAO;

    public UserService(Connection connection) {
        this.userDAO = new UserDAO(connection);
    }

    // ============================================================
    // 1) REGISTRAZIONE UTENTE
    // ============================================================
    public User registerUser(String username, String email, String password) throws SQLException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username non valido");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email non valida");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Password troppo corta");
        }
        if (userDAO.getUserByUsername(username) != null) {
            throw new IllegalStateException("Username gia esistente");
        }

        User user = new User(username, email, password, true, Role.PLAYER);
        userDAO.createUser(user);
        return user;
    }

    // ============================================================
    // 2) LOGIN UTENTE
    // ============================================================
    public User login(String username, String password) throws SQLException {
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            return null;
        }
        if (!user.isEnabled()) {
            return null;
        }
        if (!user.getPassword().equals(password)) {
            return null;
        }
        return user;
    }

    // ============================================================
    // 3) CAMBIO USERNAME
    // ============================================================
    public void changeUsername(int userId, String newUsername) throws SQLException {
        if (newUsername == null || newUsername.isBlank()) {
            throw new IllegalArgumentException("Nuovo username non valido");
        }

        User existing = userDAO.getUserByUsername(newUsername);
        if (existing != null && existing.getUserId() != userId) {
            throw new IllegalStateException("Username gia in uso");
        }

        userDAO.updateUsername(userId, newUsername);
    }

    // ============================================================
    // 4) CAMBIO EMAIL
    // ============================================================
    public void changeEmail(int userId, String newEmail) throws SQLException {
        if (newEmail == null || !newEmail.contains("@")) {
            throw new IllegalArgumentException("Email non valida");
        }
        userDAO.updateEmail(userId, newEmail);
    }

    // ============================================================
    // 5) CAMBIO PASSWORD
    // ============================================================
    public void changePassword(int userId, String newPassword) throws SQLException {
        if (newPassword == null || newPassword.length() < 4) {
            throw new IllegalArgumentException("Password non valida");
        }
        userDAO.updatePassword(userId, newPassword);
    }

    // ============================================================
    // 6) CAMBIO RUOLO
    // ============================================================
    public void changeUserRole(User caller, int targetUserId, Role newRole) throws SQLException {
        userDAO.updateUserRole(targetUserId, newRole);
    }

    // ============================================================
    // 7) DISABILITAZIONE / RIABILITAZIONE ACCOUNT
    // ============================================================
    public void setUserEnabled(User caller, int userId, boolean enabled) throws SQLException {
        userDAO.updateUserEnabled(userId, enabled);
    }

    // ============================================================
    // 8) RECUPERO SINGOLO UTENTE
    // ============================================================
    public User getUser(int userId) throws SQLException {
        return userDAO.getUserById(userId);
    }

    // ============================================================
    // 8.1) RECUPERO UTENTI By Name CON FUZZY SEARCH
    // ============================================================
    public List<User> searchUsersByName(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Nome non valido");
        }

        List<User> allUsers = userDAO.getAllUsers();
        return allUsers.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    // ============================================================
    // 9) RECUPERO TUTTI GLI UTENTI
    // ============================================================
    public List<User> getAllUsers(User caller) throws SQLException {
        return userDAO.getAllUsers();
    }


    // ============================================================
    // 10) CANCELLAZIONE UTENTE
    // ============================================================
    public void deleteUser(User caller, int targetUserId) throws SQLException {
        userDAO.deleteUser(targetUserId);
    }
}
