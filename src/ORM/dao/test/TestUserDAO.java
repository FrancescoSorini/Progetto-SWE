package ORM.dao.test;

import ORM.dao.*;
import ORM.connection.DatabaseConnection;
import DomainModel.user.Role;
import DomainModel.user.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestUserDAO {

    private static Connection connection;
    private static UserDAO userDAO;

    @BeforeAll
    static void setupDatabase() throws SQLException {
        connection = DatabaseConnection.getConnection();
        userDAO = new UserDAO(connection);

        resetUsersTable();
        resetUserIdSerial();
    }

    @AfterEach
    void cleanTableAfterTest() throws SQLException {
        resetUsersTable();
        resetUserIdSerial();
    }

    private static void resetUsersTable() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM users")) {
            ps.executeUpdate();
        }
    }

    private static void resetUserIdSerial() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "ALTER SEQUENCE users_user_id_seq RESTART WITH 1"
        )) {
            ps.executeUpdate();
        }
    }

    @Test
    @Order(1)
    void testCreateAndGetUserById() throws SQLException {
        User u = new User("mario", "mario@example.com", "pwd123", true, Role.PLAYER);

        userDAO.createUser(u);
        assertEquals(1, u.getUserId(), "L'ID generato deve essere 1");

        User dbUser = userDAO.getUserById(1);
        assertNotNull(dbUser);
        assertEquals("mario", dbUser.getUsername());
        assertEquals("mario@example.com", dbUser.getEmail());
        assertEquals(Role.PLAYER, dbUser.getRole());
    }


    @Test
    @Order(2)
    void testGetUserByUsername() throws SQLException {
        User u = new User("luca", "luca@example.com", "pwd456", true, Role.ORGANIZER);

        userDAO.createUser(u);

        User dbUser = userDAO.getUserByUsername("luca");
        assertNotNull(dbUser);
        assertEquals("luca@example.com", dbUser.getEmail());
        assertEquals(Role.ORGANIZER, dbUser.getRole());
    }


    @Test
    @Order(3)
    void testGetAllUsers() throws SQLException {
        User u1 = new User("anna", "a@example.com", "123", true, Role.PLAYER);;
        User u2 = new User("mark", "m@example.com", "456", false, Role.ADMIN);

        userDAO.createUser(u1);
        userDAO.createUser(u2);

        List<User> users = userDAO.getAllUsers();
        assertEquals(2, users.size());
    }

    @Test
    @Order(4)
    void testUpdateUsername() throws SQLException {
        User u = new User("oldname", "test@example.com", "pwd", true, Role.PLAYER);

        userDAO.createUser(u);

        userDAO.updateUsername(1, "newname");
        User dbUser = userDAO.getUserById(1);

        assertEquals("newname", dbUser.getUsername());
    }


    @Test
    @Order(5)
    void testUpdateEmail() throws SQLException {
        User u = new User("user", "old@x.com", "pwd", true, Role.PLAYER);

        userDAO.createUser(u);

        userDAO.updateEmail(1, "new@x.com");
        User dbUser = userDAO.getUserById(1);

        assertEquals("new@x.com", dbUser.getEmail());
    }


    @Test
    @Order(6)
    void testUpdatePassword() throws SQLException {
        User u = new User("user2", "a@b.com", "old", true, Role.PLAYER);

        userDAO.createUser(u);

        userDAO.updatePassword(1, "newpwd");
        User dbUser = userDAO.getUserById(1);

        assertEquals("newpwd", dbUser.getPassword());
    }


    @Test
    @Order(7)
    void testUpdateUserEnabled() throws SQLException {
        User u = new User("pino", "p@p.com", "pwd", false, Role.PLAYER);
        u.setEmail("p@p.com");
        u.setPassword("pwd");
        u.setEnabled(false);

        userDAO.createUser(u);

        userDAO.updateUserEnabled(1, true);
        User dbUser = userDAO.getUserById(1);

        assertTrue(dbUser.isEnabled());
    }


    @Test
    @Order(8)
    void testUpdateUserRole() throws SQLException {
        User u = new User("roleTest", "r@example.com", "pwd", true, Role.PLAYER);

        userDAO.createUser(u);

        userDAO.updateUserRole(1, Role.ADMIN);
        User dbUser = userDAO.getUserById(1);

        assertEquals(Role.ADMIN, dbUser.getRole());
    }


    @Test
    @Order(9)
    void testDeleteUser() throws SQLException {
        User u = new User("toDelete", "d@example.com", "pwd", true, Role.PLAYER);

        userDAO.createUser(u);

        userDAO.deleteUser(1);
        User dbUser = userDAO.getUserById(1);

        assertNull(dbUser);
    }


    @Test
    @Order(10)
    void testValidateLogin() throws SQLException {
        User u = new User("loginUser", "login@x.com", "mypassword", true, Role.PLAYER);

        userDAO.createUser(u);

        User logged = userDAO.validateLogin("loginUser", "mypassword");
        assertNotNull(logged);

        // login should fail if disabled
        userDAO.updateUserEnabled(1, false);
        User shouldFail = userDAO.validateLogin("loginUser", "mypassword");
        assertNull(shouldFail);
    }
}
