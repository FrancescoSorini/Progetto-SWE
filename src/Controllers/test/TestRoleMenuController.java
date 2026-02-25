package Controllers.test;

import Controllers.*;
import Controllers.session.UserSession;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
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
public class TestRoleMenuController {

    private static Connection connection;
    private static CardService cardService;
    private static DeckService deckService;
    private static TournamentService tournamentService;
    private static RegistrationService registrationService;
    private static UserService userService;

    @BeforeAll
    static void init() {
        connection = DatabaseConnection.getConnection();
        cardService = new CardService(connection);
        deckService = new DeckService(connection);
        tournamentService = new TournamentService(connection);
        registrationService = new RegistrationService(connection);
        userService = new UserService(connection);
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
    void test01_routePlayerRole() throws Exception {
        TrackingPlayerController p = new TrackingPlayerController();
        TrackingAdminController a = new TrackingAdminController();
        TrackingOrganizerController o = new TrackingOrganizerController();
        RoleMenuController roleMenu = new RoleMenuController(p, a, o);

        UserSession.getInstance().login(new User("p", "p@mail.com", "pass", true, Role.PLAYER));
        roleMenu.showRoleMenu();

        assertTrue(p.called);
        assertFalse(a.called);
        assertFalse(o.called);
    }

    @Test
    @Order(2)
    void test02_routeAdminRole() throws Exception {
        TrackingPlayerController p = new TrackingPlayerController();
        TrackingAdminController a = new TrackingAdminController();
        TrackingOrganizerController o = new TrackingOrganizerController();
        RoleMenuController roleMenu = new RoleMenuController(p, a, o);

        UserSession.getInstance().login(new User("a", "a@mail.com", "pass", true, Role.ADMIN));
        roleMenu.showRoleMenu();

        assertTrue(a.called);
    }

    @Test
    @Order(3)
    void test03_routeOrganizerRoleAndRequireLogin() throws Exception {
        TrackingPlayerController p = new TrackingPlayerController();
        TrackingAdminController a = new TrackingAdminController();
        TrackingOrganizerController o = new TrackingOrganizerController();
        RoleMenuController roleMenu = new RoleMenuController(p, a, o);

        UserSession.getInstance().login(new User("o", "o@mail.com", "pass", true, Role.ORGANIZER));
        roleMenu.showRoleMenu();
        assertTrue(o.called);

        UserSession.getInstance().logout();
        assertThrows(SecurityException.class, roleMenu::showRoleMenu);
    }

    private static class TrackingPlayerController extends PlayerController {
        boolean called;
        TrackingPlayerController() {
            super(new Scanner(""), cardService, deckService, tournamentService, registrationService,
                    new TournamentStatusController(tournamentService), userService);
        }
        @Override
        public void playerMenu() {
            called = true;
        }
    }

    private static class TrackingAdminController extends AdminController {
        boolean called;
        TrackingAdminController() {
            super(new Scanner(""), userService, cardService, tournamentService);
        }
        @Override
        public void adminMenu() {
            called = true;
        }
    }

    private static class TrackingOrganizerController extends OrganizerController {
        boolean called;
        TrackingOrganizerController() {
            super(new Scanner(""), tournamentService, registrationService,
                    new TournamentStatusController(tournamentService), userService);
        }
        @Override
        public void organizerMenu() {
            called = true;
        }
    }
}
