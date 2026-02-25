package Controllers.test;

import Controllers.security.ControllerGuards;
import Controllers.session.UserSession;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestControllerGuards {

    private static Connection connection;

    @BeforeAll
    static void init() {
        connection = DatabaseConnection.getConnection();
    }

    @BeforeEach
    void resetBeforeEach() throws Exception {
        truncateAll();
        UserSession.getInstance().logout();
    }

    @AfterEach
    void resetAfterEach() throws Exception {
        truncateAll();
        UserSession.getInstance().logout();
    }

    private void truncateAll() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                TRUNCATE TABLE registrations, tournaments, decks_cards, decks, cards, users
                RESTART IDENTITY CASCADE
            """);
        }
    }

    @Test
    @Order(1)
    void test01_requireLoggedInThrowsWhenNoUser() {
        assertThrows(SecurityException.class, ControllerGuards::requireLoggedIn);
    }

    @Test
    @Order(2)
    void test02_requireLoggedInReturnsUser() {
        User user = new User("p", "p@mail.com", "pass", true, Role.PLAYER);
        UserSession.getInstance().login(user);
        assertEquals(user, ControllerGuards.requireLoggedIn());
    }

    @Test
    @Order(3)
    void test03_requireRoleSuccessAndFailure() {
        User admin = new User("a", "a@mail.com", "pass", true, Role.ADMIN);
        UserSession.getInstance().login(admin);
        assertEquals(admin, ControllerGuards.requireRole(Role.ADMIN));
        assertThrows(SecurityException.class, () -> ControllerGuards.requireRole(Role.PLAYER));
    }
}
