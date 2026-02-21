package Services.test;

import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import ORM.dao.UserDAO;
import Services.user.UserService;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestUserService {

    private static Connection connection;
    private static UserService userService;
    private static UserDAO userDAO;

    @BeforeAll
    static void init() throws Exception {
        connection = DatabaseConnection.getConnection();
        userService = new UserService(connection);
        userDAO = new UserDAO(connection);
    }

    @BeforeEach
    void resetBeforeEach() throws Exception {
        truncateAll();
    }

    @AfterEach
    void resetAfterEach() throws Exception {
        truncateAll();
    }

    @AfterAll
    static void closeAll() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void truncateAll() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                TRUNCATE TABLE registrations,
                               tournaments,
                               decks_cards,
                               decks,
                               cards,
                               users
                RESTART IDENTITY CASCADE
            """);
        }
    }

    @Test
    @Order(1)
    void test01_registerUser_success() throws Exception {
        User user = userService.registerUser("p1", "p1@mail.com", "pass1234");
        assertNotNull(user);
        assertTrue(user.getUserId() > 0);
        assertEquals(Role.PLAYER, user.getRole());
    }

    @Test
    @Order(2)
    void test02_registerUser_invalidUsername() {
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(" ", "a@a.com", "pass"));
    }

    @Test
    @Order(3)
    void test03_registerUser_invalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser("u1", "mail", "pass"));
    }

    @Test
    @Order(4)
    void test04_registerUser_shortPassword() {
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser("u1", "u1@mail.com", "123"));
    }

    @Test
    @Order(5)
    void test05_registerUser_duplicateUsername() throws Exception {
        userService.registerUser("dup", "d1@mail.com", "pass123");
        assertThrows(IllegalStateException.class, () -> userService.registerUser("dup", "d2@mail.com", "pass999"));
    }

    @Test
    @Order(6)
    void test06_login_success() throws Exception {
        userService.registerUser("loginUser", "l@mail.com", "pass123");
        User user = userService.login("loginUser", "pass123");
        assertNotNull(user);
        assertEquals("loginUser", user.getUsername());
    }

    @Test
    @Order(7)
    void test07_login_wrongPasswordOrDisabled() throws Exception {
        User u = userService.registerUser("x", "x@mail.com", "pass123");
        assertNull(userService.login("x", "wrong"));

        userService.setUserEnabled(u, u.getUserId(), false);
        assertNull(userService.login("x", "pass123"));
    }

    @Test
    @Order(8)
    void test08_changeUsername_successAndDuplicate() throws Exception {
        User a = userService.registerUser("a", "a@mail.com", "pass123");
        User b = userService.registerUser("b", "b@mail.com", "pass123");

        userService.changeUsername(a.getUserId(), "a2");
        assertEquals("a2", userService.getUser(a.getUserId()).getUsername());

        assertThrows(IllegalStateException.class, () -> userService.changeUsername(b.getUserId(), "a2"));
    }

    @Test
    @Order(9)
    void test09_changeEmailAndPassword_validation() throws Exception {
        User a = userService.registerUser("a", "a@mail.com", "pass123");

        userService.changeEmail(a.getUserId(), "a2@mail.com");
        assertEquals("a2@mail.com", userService.getUser(a.getUserId()).getEmail());
        assertThrows(IllegalArgumentException.class, () -> userService.changeEmail(a.getUserId(), "bad"));

        userService.changePassword(a.getUserId(), "newpass");
        assertNotNull(userService.login("a", "newpass"));
        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(a.getUserId(), "12"));
    }

    @Test
    @Order(10)
    void test10_changeUserRole_adminCannotBeDemoted() throws Exception {
        User admin = new User("admin", "admin@mail.com", "pass123", true, Role.ADMIN);
        userDAO.createUser(admin);
        User caller = userService.registerUser("caller", "c@mail.com", "pass123");

        assertThrows(IllegalArgumentException.class,
                () -> userService.changeUserRole(caller, admin.getUserId(), Role.PLAYER));
    }

    @Test
    @Order(11)
    void test11_changeUserRoleAndSetEnabledAndDelete() throws Exception {
        User caller = userService.registerUser("caller", "c@mail.com", "pass123");
        User target = userService.registerUser("target", "t@mail.com", "pass123");

        userService.changeUserRole(caller, target.getUserId(), Role.ORGANIZER);
        assertEquals(Role.ORGANIZER, userService.getUser(target.getUserId()).getRole());

        userService.setUserEnabled(caller, target.getUserId(), false);
        assertFalse(userService.getUser(target.getUserId()).isEnabled());

        userService.deleteUser(caller, target.getUserId());
        assertNull(userService.getUser(target.getUserId()));
    }

    @Test
    @Order(12)
    void test12_searchAndGetAllUsers() throws Exception {
        User caller = userService.registerUser("caller", "c@mail.com", "pass123");
        userService.registerUser("mario1", "m1@mail.com", "pass123");
        userService.registerUser("luigi", "l@mail.com", "pass123");
        userService.registerUser("mario2", "m2@mail.com", "pass123");

        List<User> found = userService.searchUsersByName("mario");
        assertEquals(2, found.size());
        assertThrows(IllegalArgumentException.class, () -> userService.searchUsersByName(" "));

        List<User> all = userService.getAllUsers(caller);
        assertEquals(4, all.size());
    }
}
