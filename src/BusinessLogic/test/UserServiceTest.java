package BusinessLogic.test;

import BusinessLogic.user.UserService;
import BusinessLogic.session.UserSession;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import ORM.dao.UserDAO;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private static Connection connection;
    private static UserService userService;
    private static UserDAO userDAO;

    // =========================
    // SETUP
    // =========================

    @BeforeAll
    static void setup() {
        connection = DatabaseConnection.getConnection();
        userDAO = new UserDAO(connection);
        userService = new UserService(connection);
    }

    @BeforeEach
    void resetState() throws SQLException {
        resetUsersTable();
        resetUserIdSerial();
        UserSession.getInstance().logout();
    }

    private void resetUsersTable() throws SQLException {
        try (PreparedStatement ps =
                     connection.prepareStatement("DELETE FROM users")) {
            ps.executeUpdate();
        }
    }

    private void resetUserIdSerial() throws SQLException {
        try (PreparedStatement ps =
                     connection.prepareStatement(
                             "ALTER SEQUENCE users_user_id_seq RESTART WITH 1")) {
            ps.executeUpdate();
        }
    }

    // ============================================================
    // REGISTER USER
    // ============================================================

    @Test
    void registerUser_success() throws SQLException {
        User user = userService.registerUser("mario", "mario@mail.com", "1234");

        assertNotNull(user);
        assertEquals("mario", user.getUsername());
        assertEquals(Role.PLAYER, user.getRole());
    }

    @Test
    void registerUser_invalidUsername() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser("", "mail@mail.com", "1234"));
    }

    @Test
    void registerUser_invalidEmail() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser("luigi", "mail", "1234"));
    }

    @Test
    void registerUser_shortPassword() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser("luigi", "luigi@mail.com", "12"));
    }

    @Test
    void registerUser_duplicateUsername() throws SQLException {
        userService.registerUser("anna", "anna@mail.com", "1234");

        assertThrows(IllegalStateException.class, () ->
                userService.registerUser("anna", "other@mail.com", "5678"));
    }

    // ============================================================
    // LOGIN
    // ============================================================

    @Test
    void login_success() throws SQLException {
        userService.registerUser("paolo", "paolo@mail.com", "1234");

        User logged = userService.login("paolo", "1234");

        assertNotNull(logged);
        assertEquals("paolo", logged.getUsername());
    }

    @Test
    void login_wrongPassword() throws SQLException {
        userService.registerUser("carla", "carla@mail.com", "1234");

        User logged = userService.login("carla", "wrong");

        assertNull(logged);
    }

    @Test
    void login_userNotFound() throws SQLException {
        User logged = userService.login("ghost", "1234");
        assertNull(logged);
    }

    // ============================================================
    // CHANGE USERNAME / EMAIL / PASSWORD
    // ============================================================

    @Test
    void changeUsername_success() throws SQLException {
        User u = userService.registerUser("old", "old@mail.com", "1234");

        userService.changeUsername(u.getUserId(), "new");

        User updated = userDAO.getUserById(u.getUserId());
        assertEquals("new", updated.getUsername());
    }

    @Test
    void changeUsername_duplicate() throws SQLException {
        User u1 = userService.registerUser("user1", "u1@mail.com", "1234");
        User u2 = userService.registerUser("user2", "u2@mail.com", "1234");

        assertThrows(IllegalStateException.class, () ->
                userService.changeUsername(u2.getUserId(), "user1"));
    }

    @Test
    void changeEmail_invalid() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.changeEmail(1, "invalid"));
    }

    @Test
    void changePassword_invalid() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.changePassword(1, "12"));
    }

    // ============================================================
    // ADMIN ONLY METHODS
    // ============================================================

    @Test
    void changeUserRole_notLogged() {
        assertThrows(SecurityException.class, () ->
                userService.changeUserRole(1, Role.ADMIN));
    }

    @Test
    void changeUserRole_notAdmin() throws SQLException {
        User player = userService.registerUser("player", "p@mail.com", "1234");
        UserSession.getInstance().login(player);

        assertThrows(SecurityException.class, () ->
                userService.changeUserRole(player.getUserId(), Role.ADMIN));
    }

    @Test
    void changeUserRole_adminSuccess() throws SQLException {
        User admin = new User("admin", "admin@mail.com", "1234", true, Role.ADMIN);
        userDAO.createUser(admin);

        User target = userService.registerUser("target", "t@mail.com", "1234");

        UserSession.getInstance().login(admin);
        userService.changeUserRole(target.getUserId(), Role.ORGANIZER);

        User updated = userDAO.getUserById(target.getUserId());
        assertEquals(Role.ORGANIZER, updated.getRole());
    }

    // ============================================================
    // GET ALL USERS (ADMIN)
    // ============================================================

    @Test
    void getAllUsers_adminOnly() throws SQLException {
        User admin = new User("admin2", "admin2@mail.com", "1234", true, Role.ADMIN);
        userDAO.createUser(admin);

        userService.registerUser("u1", "u1@mail.com", "1234");
        userService.registerUser("u2", "u2@mail.com", "1234");

        UserSession.getInstance().login(admin);

        List<User> users = userService.getAllUsers();
        assertTrue(users.size() >= 3);
    }

    // ============================================================
    // DELETE USER
    // ============================================================

    @Test
    void deleteSelf_success() throws SQLException {
        User u = userService.registerUser("self", "self@mail.com", "1234");
        UserSession.getInstance().login(u);

        userService.deleteUser(u.getUserId());

        assertNull(userDAO.getUserById(u.getUserId()));
        assertFalse(UserSession.isLoggedIn());
    }

    @Test
    void deleteOther_notAdmin() throws SQLException {
        User u1 = userService.registerUser("u1x", "u1x@mail.com", "1234");
        User u2 = userService.registerUser("u2x", "u2x@mail.com", "1234");

        UserSession.getInstance().login(u1);

        assertThrows(SecurityException.class, () ->
                userService.deleteUser(u2.getUserId()));
    }

    @Test
    void deleteOther_admin() throws SQLException {
        User admin = new User("admin3", "admin3@mail.com", "1234", true, Role.ADMIN);
        userDAO.createUser(admin);

        User victim = userService.registerUser("victim", "v@mail.com", "1234");

        UserSession.getInstance().login(admin);
        userService.deleteUser(victim.getUserId());

        assertNull(userDAO.getUserById(victim.getUserId()));
    }
}
