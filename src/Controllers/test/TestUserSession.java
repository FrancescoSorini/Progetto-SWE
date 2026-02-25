package Controllers.test;

import Controllers.session.UserSession;
import DomainModel.GameType;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestUserSession {

    private static Connection connection;

    @BeforeAll
    static void init() {
        connection = DatabaseConnection.getConnection();
    }

    @BeforeEach
    void resetBeforeEach() throws Exception {
        truncateAll();
        UserSession.getInstance().logout();
        UserSession.getInstance().setGameType(null);
    }

    @AfterEach
    void resetAfterEach() throws Exception {
        truncateAll();
        UserSession.getInstance().logout();
        UserSession.getInstance().setGameType(null);
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
    void test01_singleton() {
        assertSame(UserSession.getInstance(), UserSession.getInstance());
    }

    @Test
    @Order(2)
    void test02_loginLogoutAndUserData() {
        User user = new User("mario", "m@mail.com", "pass", true, Role.PLAYER);
        user.setUserId(7);
        UserSession.getInstance().login(user);

        assertTrue(UserSession.isLoggedIn());
        assertEquals(7, UserSession.getUserId());
        assertEquals("mario", UserSession.getInstance().getUsername());
        assertEquals("m@mail.com", UserSession.getInstance().getEmail());

        UserSession.getInstance().logout();
        assertFalse(UserSession.isLoggedIn());
        assertEquals(-1, UserSession.getUserId());
    }

    @Test
    @Order(3)
    void test03_gameTypeAndRoleChecks() {
        User admin = new User("admin", "a@mail.com", "pass", true, Role.ADMIN);
        UserSession.getInstance().login(admin);
        UserSession.getInstance().setGameType(GameType.MAGIC);

        assertEquals(GameType.MAGIC, UserSession.getInstance().getGameType());
        assertTrue(UserSession.getInstance().hasSelectedGame());
        assertTrue(UserSession.isAdmin());
        assertFalse(UserSession.getInstance().isOrganizer());
        assertFalse(UserSession.getInstance().isPlayer());
    }

    @Test
    @Order(4)
    void test04_notifications() {
        User player = new User("p", "p@mail.com", "pass", true, Role.PLAYER);
        player.setUserId(11);
        UserSession.getInstance().login(player);

        UserSession.addNotificationForUser(11, "N1");
        UserSession.addNotificationForUser(11, "N2");
        List<String> first = UserSession.getAndClearNotificationsForCurrentUser();
        List<String> second = UserSession.getAndClearNotificationsForCurrentUser();

        assertEquals(2, first.size());
        assertEquals(0, second.size());
    }
}
