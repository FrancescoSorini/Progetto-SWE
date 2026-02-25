package Controllers.test;

import Controllers.*;
import Controllers.session.UserSession;
import DomainModel.GameType;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import ORM.dao.UserDAO;
import Services.card.CardService;
import Services.card.DeckService;
import Services.tournament.RegistrationService;
import Services.tournament.TournamentService;
import Services.user.UserService;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestGuestController {

    private static Connection connection;
    private static UserService userService;
    private static UserDAO userDAO;

    @BeforeAll
    static void init() {
        connection = DatabaseConnection.getConnection();
        userService = new UserService(connection);
        userDAO = new UserDAO(connection);
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
    void test01_exitOptionReturnsFalse() {
        GuestController guest = new GuestController(new Scanner("3\n"), userService, new TrackingRoleMenuController());
        assertFalse(guest.showWelcomeMenuAndHandleSelection());
    }

    @Test
    @Order(2)
    void test02_invalidOptionReturnsTrue() {
        GuestController guest = new GuestController(new Scanner("9\n"), userService, new TrackingRoleMenuController());
        assertTrue(guest.showWelcomeMenuAndHandleSelection());
    }

    @Test
    @Order(3)
    void test03_loginFlowCallsRoleMenu() throws Exception {
        User admin = new User("admin", "admin@mail.com", "pass123", true, Role.ADMIN);
        userDAO.createUser(admin);

        TrackingRoleMenuController roleMenu = new TrackingRoleMenuController();
        Scanner scanner = new Scanner("1\nadmin\npass123\n2\n");
        GuestController guest = new GuestController(scanner, userService, roleMenu);

        assertTrue(guest.showWelcomeMenuAndHandleSelection());
        assertTrue(roleMenu.called);
        assertTrue(UserSession.isLoggedIn());
        assertEquals(GameType.YUGIOH, UserSession.getInstance().getGameType());
    }

    @Test
    @Order(4)
    void test04_registrationFlowCreatesUserAndCallsRoleMenu() {
        TrackingRoleMenuController roleMenu = new TrackingRoleMenuController();
        Scanner scanner = new Scanner("2\nnewuser\nnew@mail.com\npass123\n1\n");
        GuestController guest = new GuestController(scanner, userService, roleMenu);

        assertTrue(guest.showWelcomeMenuAndHandleSelection());
        assertTrue(roleMenu.called);
        assertTrue(UserSession.isLoggedIn());
        assertEquals(GameType.MAGIC, UserSession.getInstance().getGameType());
    }

    private static class TrackingRoleMenuController extends RoleMenuController {
        boolean called;

        TrackingRoleMenuController() {
            super(
                    new PlayerController(new Scanner(""), new CardService(connection), new DeckService(connection),
                            new TournamentService(connection), new RegistrationService(connection),
                            new TournamentStatusController(new TournamentService(connection)), new UserService(connection)),
                    new AdminController(new Scanner(""), new UserService(connection), new CardService(connection), new TournamentService(connection)),
                    new OrganizerController(new Scanner(""), new TournamentService(connection), new RegistrationService(connection),
                            new TournamentStatusController(new TournamentService(connection)), new UserService(connection))
            );
        }

        @Override
        public void showRoleMenu() {
            called = true;
        }
    }
}
