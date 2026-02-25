package Controllers.test;

import Controllers.*;
import Controllers.session.UserSession;
import DomainModel.GameType;
import DomainModel.card.Card;
import DomainModel.card.Deck;
import DomainModel.tournament.Registration;
import DomainModel.tournament.Tournament;
import DomainModel.tournament.TournamentStatus;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import ORM.dao.CardDAO;
import ORM.dao.DeckDAO;
import ORM.dao.UserDAO;
import Services.card.CardService;
import Services.card.DeckService;
import Services.tournament.RegistrationService;
import Services.tournament.TournamentService;
import Services.user.UserService;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
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

    private static User dbPlayer;
    private static User dbAdmin;
    private static User dbOrganizer;

    @BeforeAll
    static void init() throws Exception {
        connection = DatabaseConnection.getConnection();
        cardService = new CardService(connection);
        deckService = new DeckService(connection);
        tournamentService = new TournamentService(connection);
        registrationService = new RegistrationService(connection);
        userService = new UserService(connection);

        truncateAll();
        seedBaseData();
    }

    @AfterAll
    static void cleanupAll() throws Exception {
        truncateAll();
        UserSession.getInstance().logout();
        UserSession.getInstance().setGameType(null);
    }

    private static void truncateAll() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                TRUNCATE TABLE registrations, tournaments, decks_cards, decks, cards, users
                RESTART IDENTITY CASCADE
            """);
        }
    }

    private static void seedBaseData() throws Exception {
        UserDAO userDAO = new UserDAO(connection);
        CardDAO cardDAO = new CardDAO(connection);
        DeckDAO deckDAO = new DeckDAO(connection);

        dbPlayer = new User("role_player", "role_player@mail.com", "pass123", true, Role.PLAYER);
        dbAdmin = new User("role_admin", "role_admin@mail.com", "pass123", true, Role.ADMIN);
        dbOrganizer = new User("role_organizer", "role_org@mail.com", "pass123", true, Role.ORGANIZER);
        userDAO.createUser(dbPlayer);
        userDAO.createUser(dbAdmin);
        userDAO.createUser(dbOrganizer);

        Card card = new Card("RoleMenuCard", GameType.YUGIOH);
        cardDAO.addCard(card);
        Deck deck = new Deck("RoleMenuDeck", dbPlayer, GameType.YUGIOH);
        deckDAO.createDeck(deck);
        deckDAO.addCardToDeck(deck.getDeckId(), card.getCardId());

        Tournament t = new Tournament();
        t.setName("RoleMenuTournament");
        t.setDescription("desc");
        t.setOrganizer(dbOrganizer);
        t.setCapacity(8);
        t.setDeadline(LocalDate.now().plusDays(2));
        t.setStartDate(LocalDate.now().plusDays(3));
        t.setStatus(TournamentStatus.PENDING);
        t.setGameType(GameType.YUGIOH);
        t.setRegistrations(new ArrayList<>());
        tournamentService.createTournament(dbOrganizer, t);
        tournamentService.approveTournament(dbOrganizer, t.getTournamentId());
        registrationService.registerUserToTournament(
                dbPlayer,
                t.getTournamentId(),
                new Registration(tournamentService.getTournamentById(t.getTournamentId()), dbPlayer, deck)
        );
    }

    @Test
    @Order(1)
    void test01_routePlayerRole() throws Exception {
        TrackingPlayerController p = new TrackingPlayerController();
        TrackingAdminController a = new TrackingAdminController();
        TrackingOrganizerController o = new TrackingOrganizerController();
        RoleMenuController roleMenu = new RoleMenuController(p, a, o);

        UserSession.getInstance().login(dbPlayer);
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

        UserSession.getInstance().login(dbAdmin);
        roleMenu.showRoleMenu();

        assertFalse(p.called);
        assertTrue(a.called);
        assertFalse(o.called);
    }

    @Test
    @Order(3)
    void test03_routeOrganizerRole() throws Exception {
        TrackingPlayerController p = new TrackingPlayerController();
        TrackingAdminController a = new TrackingAdminController();
        TrackingOrganizerController o = new TrackingOrganizerController();
        RoleMenuController roleMenu = new RoleMenuController(p, a, o);

        UserSession.getInstance().login(dbOrganizer);
        roleMenu.showRoleMenu();

        assertFalse(p.called);
        assertFalse(a.called);
        assertTrue(o.called);
    }

    @Test
    @Order(4)
    void test04_requireLogin() {
        TrackingPlayerController p = new TrackingPlayerController();
        TrackingAdminController a = new TrackingAdminController();
        TrackingOrganizerController o = new TrackingOrganizerController();
        RoleMenuController roleMenu = new RoleMenuController(p, a, o);

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
