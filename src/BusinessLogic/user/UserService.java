package BusinessLogic.user;

import ORM.dao.UserDAO;
import DomainModel.user.Role;
import DomainModel.user.User;
import BusinessLogic.session.UserSession;

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

        // Validazioni di business
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username non valido");

        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Email non valida");

        if (password == null || password.length() < 4)
            throw new IllegalArgumentException("Password troppo corta");

        // Controlla se username esiste già
        if (userDAO.getUserByUsername(username) != null)
            throw new IllegalStateException("Username già esistente");

        // Crea l’utente
        User user = new User(username, email, password, true, Role.PLAYER);

        userDAO.createUser(user);
        return user;
    }

    // ============================================================
    // 2) LOGIN UTENTE
    // ============================================================
    public User login(String username, String password) throws SQLException {

        // 1) Recupero dello user tramite username
        User user = userDAO.getUserByUsername(username);

        // 2) Se non esiste, login fallito
        if (user == null) {
            return null;
        }

        // 3) Confronto credenziali
        if (!user.getPassword().equals(password) || user.isEnabled()) {
            return null;
        }

        // 4) Login riuscito
        return user;
    }

    // ============================================================
    // 3) CAMBIO USERNAME
    // ============================================================
    public void changeUsername(int userId, String newUsername) throws SQLException {

        if (newUsername == null || newUsername.isBlank())
            throw new IllegalArgumentException("Nuovo username non valido");

        // Controlla se esiste già
        if (userDAO.getUserByUsername(newUsername) != null)
            throw new IllegalStateException("Username già in uso");

        userDAO.updateUsername(userId, newUsername);
    }

    // ============================================================
    // 4) CAMBIO EMAIL
    // ============================================================
    public void changeEmail(int userId, String newEmail) throws SQLException {

        if (newEmail == null || !newEmail.contains("@"))
            throw new IllegalArgumentException("Email non valida");

        userDAO.updateEmail(userId, newEmail);
    }

    // ============================================================
    // 5) CAMBIO PASSWORD
    // ============================================================
    public void changePassword(int userId, String newPassword) throws SQLException {

        if (newPassword == null || newPassword.length() < 4)
            throw new IllegalArgumentException("Password non valida");

        userDAO.updatePassword(userId, newPassword);
    }

    // ============================================================
    // 6) CAMBIO RUOLO (solo admin)
    // ============================================================
    public void changeUserRole(int targetUserId, Role newRole) throws SQLException {

        // ottieni l’utente attualmente loggato
        User caller = UserSession.getInstance().getCurrentUser();

        if (caller == null) {
            throw new SecurityException("Nessun utente loggato");
        }

        if (caller.getRole() != Role.ADMIN) {
            throw new SecurityException("Solo gli admin possono modificare i ruoli");
        }

        // Esegui update
        userDAO.updateUserRole(targetUserId, newRole);
    }

    // ============================================================
    // 7) DISABILITAZIONE / RIABILITAZIONE ACCOUNT
    // ============================================================
    public void setUserEnabled(int userId, boolean enabled) throws SQLException {
        // ottieni l’utente attualmente loggato
        User caller = UserSession.getInstance().getCurrentUser();

        if (caller == null) {
            throw new SecurityException("Nessun utente loggato");
        }

        if (caller.getRole() != Role.ADMIN) {
            throw new SecurityException("Solo gli admin possono modificare i ruoli");
        }

        //Esegui update
        userDAO.updateUserEnabled(userId, enabled);
    }

    // ============================================================
    // 8) RECUPERO SINGOLO UTENTE (serve?)
    // ============================================================
    public User getUser(int userId) throws SQLException {
        return userDAO.getUserById(userId);
    }

    // ============================================================
    // 9) RECUPERO TUTTI GLI UTENTI (solo admin)
    // ============================================================
    public List<User> getAllUsers() throws SQLException {
        // ottieni l’utente attualmente loggato
        User caller = UserSession.getInstance().getCurrentUser();

        if (caller == null) {
            throw new SecurityException("Nessun utente loggato");
        }

        if (caller.getRole() != Role.ADMIN) {
            throw new SecurityException("Solo gli admin possono modificare i ruoli");
        }

        // Esegui recupero
        return userDAO.getAllUsers();
    }

    // ============================================================
    // 10) CANCELLAZIONE UTENTE
    // ============================================================
    public void deleteUser(int targetUserId) throws SQLException {

        UserSession session = UserSession.getInstance();
        User caller = session.getCurrentUser();

        if (caller == null) {
            throw new SecurityException("Nessun utente loggato.");
        }

        int callerId = caller.getUserId();
        boolean callerIsAdmin = caller.getRole() == Role.ADMIN;

        // CASO 1: L'utente vuole cancellare se stesso → SEMPRE PERMESSO
        if (callerId == targetUserId) {
            userDAO.deleteUser(targetUserId);
            session.logout(); // opzionale: logout automatico dopo self-delete
            return;
        }

        // CASO 2: Un admin vuole cancellare un altro utente → PERMESSO
        if (callerIsAdmin) {
            userDAO.deleteUser(targetUserId);
            return;
        }

        // CASO 3: Utente normale vuole cancellare un altro utente → BLOCCATO
        throw new SecurityException("Non hai i permessi per cancellare altri utenti.");
    }

}

