package Services.test;

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
import ORM.dao.RegistrationDAO;
import ORM.dao.TournamentDAO;
import ORM.dao.UserDAO;
import Services.tournament.RegistrationService;
import Services.tournament.TournamentService;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestRegistrationService {

    private static Connection connection;
    private static RegistrationService registrationService;
    private static TournamentService tournamentService;
    private static UserDAO userDAO;
    private static DeckDAO deckDAO;
    private static CardDAO cardDAO;
    private static RegistrationDAO registrationDAO;
    private static TournamentDAO tournamentDAO;

    private static User organizer;
    private static User player1;
    private static User player2;
    private static Deck player1YDeck;
    private static Deck player1MDeck;
    private static Deck player2YDeck;

    @BeforeAll
    static void init() throws Exception {
        connection = DatabaseConnection.getConnection();
        registrationService = new RegistrationService(connection);
        tournamentService = new TournamentService(connection);
        userDAO = new UserDAO(connection);
        deckDAO = new DeckDAO(connection);
        cardDAO = new CardDAO(connection);
        registrationDAO = new RegistrationDAO(connection);
        tournamentDAO = new TournamentDAO(connection);
    }

    @BeforeEach
    void resetBeforeEach() throws Exception {
        truncateAll();

        organizer = new User("org", "org@mail.com", "pass123", true, Role.ORGANIZER);
        player1 = new User("p1", "p1@mail.com", "pass123", true, Role.PLAYER);
        player2 = new User("p2", "p2@mail.com", "pass123", true, Role.PLAYER);
        userDAO.createUser(organizer);
        userDAO.createUser(player1);
        userDAO.createUser(player2);

        Card yCard = new Card("Blue-Eyes", GameType.YUGIOH);
        Card mCard = new Card("Lightning Bolt", GameType.MAGIC);
        cardDAO.addCard(yCard);
        cardDAO.addCard(mCard);

        player1YDeck = new Deck("p1YDeck", player1, GameType.YUGIOH);
        player1MDeck = new Deck("p1MDeck", player1, GameType.MAGIC);
        player2YDeck = new Deck("p2YDeck", player2, GameType.YUGIOH);
        deckDAO.createDeck(player1YDeck);
        deckDAO.createDeck(player1MDeck);
        deckDAO.createDeck(player2YDeck);
    }

    @AfterEach
    void resetAfterEach() throws Exception {
        truncateAll();
        UserSession.getInstance().logout();
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

    private Tournament createTournament(String name, TournamentStatus status, int capacity, LocalDate deadline, LocalDate startDate)
            throws Exception {
        Tournament t = new Tournament();
        t.setName(name);
        t.setDescription("desc");
        t.setOrganizer(organizer);
        t.setCapacity(capacity);
        t.setDeadline(deadline);
        t.setStartDate(startDate);
        t.setStatus(TournamentStatus.PENDING);
        t.setGameType(GameType.YUGIOH);
        t.setRegistrations(new ArrayList<>());

        tournamentService.createTournament(organizer, t);
        if (status == TournamentStatus.APPROVED) {
            tournamentService.approveTournament(organizer, t.getTournamentId());
        } else if (status != TournamentStatus.PENDING) {
            Tournament loaded = tournamentService.getTournamentById(t.getTournamentId());
            loaded.setStatus(status);
            tournamentDAO.updateTournament(loaded);
        }
        return tournamentService.getTournamentById(t.getTournamentId());
    }

    @Test
    @Order(1)
    void test01_registerUserToTournament_success() throws Exception {
        Tournament t = createTournament("Cup", TournamentStatus.APPROVED, 8, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        Registration reg = new Registration(t, player1, player1YDeck);

        registrationService.registerUserToTournament(player1, t.getTournamentId(), reg);

        assertTrue(registrationDAO.isUserRegistered(t.getTournamentId(), player1.getUserId()));
    }

    @Test
    @Order(2)
    void test02_registerUserToTournament_requiresLoginAndTournamentExists() throws Exception {
        Tournament t = createTournament("Cup", TournamentStatus.APPROVED, 8, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        Registration reg = new Registration(t, player1, player1YDeck);

        assertThrows(SecurityException.class, () -> registrationService.registerUserToTournament(null, t.getTournamentId(), reg));
        assertThrows(IllegalArgumentException.class, () -> registrationService.registerUserToTournament(player1, 999, reg));
    }

    @Test
    @Order(3)
    void test03_registerUserToTournament_rulesValidation() throws Exception {
        Tournament pending = createTournament("PendingCup", TournamentStatus.PENDING, 8, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        assertThrows(IllegalStateException.class, () ->
                registrationService.registerUserToTournament(player1, pending.getTournamentId(), new Registration(pending, player1, player1YDeck)));

        Tournament deadlinePassed = createTournament("DeadlineCup", TournamentStatus.APPROVED, 8, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        assertThrows(IllegalStateException.class, () ->
                registrationService.registerUserToTournament(player1, deadlinePassed.getTournamentId(), new Registration(deadlinePassed, player1, player1YDeck)));

        Tournament typeMismatch = createTournament("MismatchCup", TournamentStatus.APPROVED, 8, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        assertThrows(IllegalArgumentException.class, () ->
                registrationService.registerUserToTournament(player1, typeMismatch.getTournamentId(), new Registration(typeMismatch, player1, player1MDeck)));
    }

    @Test
    @Order(4)
    void test04_registerUserToTournament_duplicateAndFull() throws Exception {
        Tournament t = createTournament("FullCup", TournamentStatus.APPROVED, 1, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        Registration reg1 = new Registration(t, player1, player1YDeck);
        registrationService.registerUserToTournament(player1, t.getTournamentId(), reg1);

        assertThrows(IllegalStateException.class,
                () -> registrationService.registerUserToTournament(player1, t.getTournamentId(), new Registration(t, player1, player1YDeck)));

        assertThrows(IllegalStateException.class,
                () -> registrationService.registerUserToTournament(player2, t.getTournamentId(), new Registration(t, player2, player2YDeck)));
    }

    @Test
    @Order(5)
    void test05_unregisterFromTournament_successAndNotRegistered() throws Exception {
        Tournament t = createTournament("Cup", TournamentStatus.APPROVED, 8, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        registrationService.registerUserToTournament(player1, t.getTournamentId(), new Registration(t, player1, player1YDeck));
        assertTrue(registrationDAO.isUserRegistered(t.getTournamentId(), player1.getUserId()));

        registrationService.unregisterFromTournament(player1, t.getTournamentId());
        assertFalse(registrationDAO.isUserRegistered(t.getTournamentId(), player1.getUserId()));

        assertThrows(IllegalStateException.class, () -> registrationService.unregisterFromTournament(player1, t.getTournamentId()));
    }

    @Test
    @Order(6)
    void test06_unregisterFromTournament_startedTournament() throws Exception {
        Tournament t = createTournament("StartedCup", TournamentStatus.APPROVED, 8, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        registrationService.registerUserToTournament(player1, t.getTournamentId(), new Registration(t, player1, player1YDeck));

        Tournament loaded = tournamentService.getTournamentById(t.getTournamentId());
        loaded.setStartDate(LocalDate.now().minusDays(1));
        tournamentDAO.updateTournament(loaded);

        assertThrows(IllegalStateException.class, () -> registrationService.unregisterFromTournament(player1, t.getTournamentId()));
    }

    @Test
    @Order(7)
    void test07_unregisterUserFromTournament_successAndNotification() throws Exception {
        Tournament t = createTournament("Cup", TournamentStatus.APPROVED, 8, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        registrationService.registerUserToTournament(player1, t.getTournamentId(), new Registration(t, player1, player1YDeck));

        UserSession.getInstance().login(player1);
        UserSession.getAndClearNotificationsForCurrentUser();
        UserSession.getInstance().logout();

        registrationService.unregisterUserFromTournament(organizer, t.getTournamentId(), player1.getUserId());
        assertFalse(registrationDAO.isUserRegistered(t.getTournamentId(), player1.getUserId()));

        UserSession.getInstance().login(player1);
        List<String> notifications = UserSession.getAndClearNotificationsForCurrentUser();
        assertFalse(notifications.isEmpty());
        assertTrue(notifications.getFirst().contains("rimosso dal torneo ID " + t.getTournamentId()));
    }

    @Test
    @Order(8)
    void test08_unregisterUserFromTournament_notRegistered() throws Exception {
        Tournament t = createTournament("Cup", TournamentStatus.APPROVED, 8, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        assertThrows(IllegalStateException.class,
                () -> registrationService.unregisterUserFromTournament(organizer, t.getTournamentId(), player1.getUserId()));
    }

    @Test
    @Order(9)
    void test09_getRegistrationsApis() throws Exception {
        Tournament t = createTournament("Cup", TournamentStatus.APPROVED, 8, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        registrationService.registerUserToTournament(player1, t.getTournamentId(), new Registration(t, player1, player1YDeck));

        assertEquals(1, registrationService.getRegistrationsByUser(player1, player1.getUserId()).size());
        assertEquals(1, registrationService.getRegistrationsByTournament(player1, t.getTournamentId()).size());
        assertEquals(1, registrationService.getAllRegistrations(player1).size());

        assertThrows(SecurityException.class, () -> registrationService.getRegistrationsByUser(null, player1.getUserId()));
        assertThrows(IllegalArgumentException.class, () -> registrationService.getRegistrationsByTournament(player1, 999));
    }
}
