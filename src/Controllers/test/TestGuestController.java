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
public class TestGuestController {

    private static Connection connection;
    private static UserService userService;
    private static UserDAO userDAO;
    private static User admin;
    private static User disabledPlayer;

    @BeforeAll
    static void init() throws Exception {
        connection = DatabaseConnection.getConnection();
        userService = new UserService(connection);
        userDAO = new UserDAO(connection);
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
        CardDAO cardDAO = new CardDAO(connection);
        DeckDAO deckDAO = new DeckDAO(connection);

        admin = new User("guest_admin", "guest_admin@mail.com", "pass123", true, Role.ADMIN);
        userDAO.createUser(admin);

        User organizer = new User("guest_org", "guest_org@mail.com", "pass123", true, Role.ORGANIZER);
        userDAO.createUser(organizer);

        User player = new User("guest_player", "guest_player@mail.com", "pass123", true, Role.PLAYER);
        userDAO.createUser(player);

        disabledPlayer = new User("guest_disabled", "guest_disabled@mail.com", "pass123", false, Role.PLAYER);
        userDAO.createUser(disabledPlayer);

        Card card = new Card("GuestSeedCard", GameType.MAGIC);
        cardDAO.addCard(card);
        Deck deck = new Deck("GuestSeedDeck", player, GameType.MAGIC);
        deckDAO.createDeck(deck);
        deckDAO.addCardToDeck(deck.getDeckId(), card.getCardId());

        TournamentService tournamentService = new TournamentService(connection);
        RegistrationService registrationService = new RegistrationService(connection);
        Tournament t = new Tournament();
        t.setName("GuestSeedTournament");
        t.setDescription("desc");
        t.setOrganizer(organizer);
        t.setCapacity(8);
        t.setDeadline(LocalDate.now().plusDays(2));
        t.setStartDate(LocalDate.now().plusDays(3));
        t.setStatus(TournamentStatus.PENDING);
        t.setGameType(GameType.MAGIC);
        t.setRegistrations(new ArrayList<>());
        tournamentService.createTournament(organizer, t);
        tournamentService.approveTournament(organizer, t.getTournamentId());
        registrationService.registerUserToTournament(
                player,
                t.getTournamentId(),
                new Registration(tournamentService.getTournamentById(t.getTournamentId()), player, deck)
        );
    }

    @Test
    @Order(1)
    void test01_exitOptionReturnsFalse() {
        UserSession.getInstance().logout();
        GuestController guest = new GuestController(new Scanner("3\n"), userService, new TrackingRoleMenuController());
        assertFalse(guest.showWelcomeMenuAndHandleSelection());
    }

    @Test
    @Order(2)
    void test02_invalidOptionReturnsTrue() {
        UserSession.getInstance().logout();
        GuestController guest = new GuestController(new Scanner("9\n"), userService, new TrackingRoleMenuController());
        assertTrue(guest.showWelcomeMenuAndHandleSelection());
    }

    @Test
    @Order(3)
    void test03_loginFlowCallsRoleMenu() {
        UserSession.getInstance().logout();
        TrackingRoleMenuController roleMenu = new TrackingRoleMenuController();
        Scanner scanner = new Scanner("1\nguest_admin\npass123\n2\n");
        GuestController guest = new GuestController(scanner, userService, roleMenu);

        assertTrue(guest.showWelcomeMenuAndHandleSelection());
        assertTrue(roleMenu.called);
        assertTrue(UserSession.isLoggedIn());
        assertEquals(GameType.YUGIOH, UserSession.getInstance().getGameType());
    }

    @Test
    @Order(4)
    void test04_loginWrongCredentialsStaysGuest() {
        UserSession.getInstance().logout();
        TrackingRoleMenuController roleMenu = new TrackingRoleMenuController();
        Scanner scanner = new Scanner("1\nguest_admin\nwrong\n");
        GuestController guest = new GuestController(scanner, userService, roleMenu);

        assertTrue(guest.showWelcomeMenuAndHandleSelection());
        assertFalse(roleMenu.called);
        assertFalse(UserSession.isLoggedIn());
    }

    @Test
    @Order(5)
    void test05_loginDisabledUserStaysGuest() {
        UserSession.getInstance().logout();
        TrackingRoleMenuController roleMenu = new TrackingRoleMenuController();
        Scanner scanner = new Scanner("1\nguest_disabled\npass123\n");
        GuestController guest = new GuestController(scanner, userService, roleMenu);

        assertTrue(guest.showWelcomeMenuAndHandleSelection());
        assertFalse(roleMenu.called);
        assertFalse(UserSession.isLoggedIn());
    }

    @Test
    @Order(6)
    void test06_registrationFlowCreatesUserAndCallsRoleMenu() {
        UserSession.getInstance().logout();
        TrackingRoleMenuController roleMenu = new TrackingRoleMenuController();
        Scanner scanner = new Scanner("2\nnew_guest_user\nnew_guest_user@mail.com\npass123\n1\n");
        GuestController guest = new GuestController(scanner, userService, roleMenu);

        assertTrue(guest.showWelcomeMenuAndHandleSelection());
        assertTrue(roleMenu.called);
        assertTrue(UserSession.isLoggedIn());
        assertEquals(GameType.MAGIC, UserSession.getInstance().getGameType());
    }

    @Test
    @Order(7)
    void test07_registrationInvalidDataHandled() {
        UserSession.getInstance().logout();
        TrackingRoleMenuController roleMenu = new TrackingRoleMenuController();
        Scanner scanner = new Scanner("2\nbad_user\nbad_mail\n123\n");
        GuestController guest = new GuestController(scanner, userService, roleMenu);

        assertTrue(guest.showWelcomeMenuAndHandleSelection());
        assertFalse(roleMenu.called);
        assertFalse(UserSession.isLoggedIn());
    }

    @Test
    @Order(8)
    void test08_selectGameRecursiveOnInvalidChoice() {
        UserSession.getInstance().logout();
        TrackingRoleMenuController roleMenu = new TrackingRoleMenuController();
        Scanner scanner = new Scanner("1\nguest_admin\npass123\n9\n1\n");
        GuestController guest = new GuestController(scanner, userService, roleMenu);

        assertTrue(guest.showWelcomeMenuAndHandleSelection());
        assertTrue(roleMenu.called);
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
