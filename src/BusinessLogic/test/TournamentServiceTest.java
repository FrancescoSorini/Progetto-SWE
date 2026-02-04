package BusinessLogic.test;

import BusinessLogic.session.UserSession;
import BusinessLogic.tournament.TournamentService;
import DomainModel.GameType;
import DomainModel.card.Deck;
import DomainModel.tournament.*;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import ORM.dao.DeckDAO;
import ORM.dao.RegistrationDAO;
import ORM.dao.TournamentDAO;
import ORM.dao.UserDAO;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TournamentServiceTest {

    private static Connection connection;
    private static TournamentService tournamentService;
    private static TournamentDAO tournamentDAO;
    private static UserDAO userDAO;
    private static RegistrationDAO registrationDAO;
    private static DeckDAO deckDAO;

    // ====================================================
    // SETUP
    // ====================================================
    @BeforeAll
    static void setup() {
        connection = DatabaseConnection.getConnection();
        tournamentService = new TournamentService(connection);
        tournamentDAO = new TournamentDAO(connection);
        userDAO = new UserDAO(connection);
        registrationDAO = new RegistrationDAO(connection);
        deckDAO = new DeckDAO(connection);
    }

    @BeforeEach
    void resetState() throws SQLException {
        clearTable("registrations");
        clearTable("decks_cards");
        clearTable("decks");
        clearTable("tournaments");
        clearTable("users");
        UserSession.getInstance().logout();
    }

    @AfterAll
    static void cleanDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM registrations");
            stmt.executeUpdate("DELETE FROM decks_cards");
            stmt.executeUpdate("DELETE FROM decks");
            stmt.executeUpdate("DELETE FROM tournaments");
            stmt.executeUpdate("DELETE FROM users");
        }
    }

    private void clearTable(String table) throws SQLException {
        try (PreparedStatement ps =
                     connection.prepareStatement("DELETE FROM " + table)) {
            ps.executeUpdate();
        }
    }

    // ====================================================
    // HELPERS
    // ====================================================
    private User createUser(String username, Role role) throws SQLException {
        User u = new User(username, username + "@mail.com", "1234", true, role);
        userDAO.createUser(u);
        return u;
    }

    private Tournament createTournament(User organizer,
                                        TournamentStatus status,
                                        LocalDate deadline,
                                        LocalDate startDate) throws SQLException {
        Tournament t = new Tournament("Tournament", GameType.MAGIC);
        t.setOrganizer(organizer);
        t.setStatus(status);
        t.setCapacity(160);
        t.setDeadline(deadline);
        t.setStartDate(startDate);
        tournamentDAO.createTournament(t);
        return t;
    }

    private Deck createDeck(User owner, GameType type) throws SQLException {
        Deck d = new Deck("DeckTest", owner, type);
        deckDAO.createDeck(d);
        return d;
    }

    private void createRegistration(Tournament t, User u, Deck d) throws SQLException {
        Registration r = new Registration(t, u, d);
        registrationDAO.createRegistration(r);
    }

    private void login(User u) {
        UserSession.getInstance().login(u);
    }

    // ====================================================
    // CREATE
    // ====================================================
    @Test
    @Order(1)
    void organizer_creates_tournament() throws Exception {
        User org = createUser("org1", Role.ORGANIZER);
        login(org);

        Tournament t = new Tournament("T1", GameType.MAGIC);
        t.setCapacity(10);
        t.setDeadline(LocalDate.now().plusDays(5));
        t.setStartDate(LocalDate.now().plusDays(10));

        tournamentService.createTournament(t);

        assertEquals(1, tournamentDAO.getAllTournaments().size());
    }

    @Test
    @Order(2)
    void not_logged_cannot_create() {
        Tournament t = new Tournament("Fail", GameType.MAGIC);
        assertThrows(SecurityException.class,
                () -> tournamentService.createTournament(t));
    }

    @Test
    @Order(3)
    void player_cannot_create() throws Exception {
        User p = createUser("p1", Role.PLAYER);
        login(p);

        Tournament t = new Tournament("Fail", GameType.MAGIC);
        t.setCapacity(5);
        t.setDeadline(LocalDate.now().plusDays(5));
        t.setStartDate(LocalDate.now().plusDays(10));

        assertThrows(SecurityException.class,
                () -> tournamentService.createTournament(t));
    }

    // ====================================================
    // UPDATE
    // ====================================================
    @Test
    @Order(4)
    void organizer_updates_pending() throws Exception {
        User org = createUser("org2", Role.ORGANIZER);
        login(org);

        Tournament t = createTournament(
                org,
                TournamentStatus.PENDING,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)
        );
        t.setCapacity(32);

        tournamentService.updateTournament(t);
        assertEquals(32, t.getCapacity());
    }

    @Test
    @Order(5)
    void organizer_not_owner_forbidden() throws Exception {
        User org1 = createUser("orgA", Role.ORGANIZER);
        User org2 = createUser("orgB", Role.ORGANIZER);

        Tournament t = createTournament(
                org1,
                TournamentStatus.PENDING,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)
        );

        login(org2);
        assertThrows(SecurityException.class,
                () -> tournamentService.updateTournament(t));
    }

    @Test
    @Order(6)
    void organizer_cannot_update_non_pending() throws Exception {
        User org = createUser("org3", Role.ORGANIZER);
        login(org);

        Tournament t = createTournament(
                org,
                TournamentStatus.APPROVED,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)
        );

        assertThrows(IllegalStateException.class,
                () -> tournamentService.updateTournament(t));
    }

    // ====================================================
    // DELETE
    // ====================================================
    @Test
    @Order(7)
    void organizer_deletes_own() throws Exception {
        User org = createUser("org4", Role.ORGANIZER);
        login(org);

        Tournament t = createTournament(
                org,
                TournamentStatus.PENDING,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)
        );

        tournamentService.deleteTournament(t.getTournamentId());
        assertTrue(tournamentDAO.getAllTournaments().isEmpty());
    }

    @Test
    @Order(8)
    void admin_deletes_any() throws Exception {
        User org = createUser("org5", Role.ORGANIZER);
        User admin = createUser("admin", Role.ADMIN);

        Tournament t = createTournament(
                org,
                TournamentStatus.PENDING,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)
        );

        login(admin);
        tournamentService.deleteTournament(t.getTournamentId());

        assertTrue(tournamentDAO.getAllTournaments().isEmpty());
    }

    @Test
    @Order(9)
    void player_cannot_delete() throws Exception {
        User org = createUser("org6", Role.ORGANIZER);
        User p = createUser("p2", Role.PLAYER);

        Tournament t = createTournament(
                org,
                TournamentStatus.PENDING,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)
        );

        login(p);
        assertThrows(SecurityException.class,
                () -> tournamentService.deleteTournament(t.getTournamentId()));
    }

    // ====================================================
    // APPROVE / REJECT
    // ====================================================
    @Test
    @Order(10)
    void admin_approves() throws Exception {
        User org = createUser("org7", Role.ORGANIZER);
        User admin = createUser("admin2", Role.ADMIN);

        Tournament t = createTournament(
                org,
                TournamentStatus.PENDING,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)
        );

        login(admin);
        tournamentService.approveTournament(t.getTournamentId());

        assertEquals(TournamentStatus.APPROVED,
                tournamentDAO.getTournamentById(t.getTournamentId()).getStatus());
    }

    @Test
    @Order(11)
    void admin_rejects() throws Exception {
        User org = createUser("org8", Role.ORGANIZER);
        User admin = createUser("admin3", Role.ADMIN);

        Tournament t = createTournament(
                org,
                TournamentStatus.PENDING,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)
        );

        login(admin);
        tournamentService.rejectTournament(t.getTournamentId());

        assertEquals(TournamentStatus.REJECTED,
                tournamentDAO.getTournamentById(t.getTournamentId()).getStatus());
    }

    @Test
    @Order(12)
    void non_admin_cannot_approve() throws Exception {
        User org = createUser("org9", Role.ORGANIZER);
        login(org);

        Tournament t = createTournament(
                org,
                TournamentStatus.PENDING,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)
        );

        assertThrows(SecurityException.class,
                () -> tournamentService.approveTournament(t.getTournamentId()));
    }

    // ====================================================
    // AUTO STATUS UPDATE
    // ====================================================
    @Test
    @Order(13)
    void approved_to_ready() throws Exception {
        User org = createUser("org10", Role.ORGANIZER);

        createTournament(
                org,
                TournamentStatus.APPROVED,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5)
        );

        tournamentService.updateTournamentStatusesAutomatically();

        assertEquals(TournamentStatus.READY,
                tournamentDAO.getAllTournaments().get(0).getStatus());
    }

    @Test
    @Order(14)
    void ready_to_closed() throws Exception {
        User org = createUser("org11", Role.ORGANIZER);

        createTournament(
                org,
                TournamentStatus.READY,
                LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(1)
        );

        tournamentService.updateTournamentStatusesAutomatically();

        assertEquals(TournamentStatus.CLOSED,
                tournamentDAO.getAllTournaments().get(0).getStatus());
    }

    // ====================================================
    // GETTERS
    // ====================================================
    @Test
    @Order(15)
    void admin_sees_all() throws Exception {
        User admin = createUser("admin4", Role.ADMIN);
        createTournament(admin, TournamentStatus.PENDING,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10));

        login(admin);
        assertEquals(1, tournamentService.getAllTournaments().size());
    }

    @Test
    @Order(16)
    void player_sees_only_approved_ready() throws Exception {
        User org = createUser("org12", Role.ORGANIZER);
        User p = createUser("p3", Role.PLAYER);

        createTournament(org, TournamentStatus.PENDING,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10));

        createTournament(org, TournamentStatus.APPROVED,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10));

        login(p);
        assertEquals(1, tournamentService.getAllTournaments().size());
    }

    @Test
    @Order(17)
    void get_by_organizer_correct() throws Exception {
        User org = createUser("org13", Role.ORGANIZER);
        login(org);

        createTournament(org, TournamentStatus.PENDING,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10));

        assertEquals(1,
                tournamentService.getTournamentsByOrganizer(org.getUserId()).size());
    }

    @Test
    @Order(18)
    void get_by_organizer_forbidden() throws Exception {
        User org = createUser("org14", Role.ORGANIZER);
        User other = createUser("org15", Role.ORGANIZER);

        createTournament(org, TournamentStatus.PENDING,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10));

        login(other);
        assertThrows(SecurityException.class,
                () -> tournamentService.getTournamentsByOrganizer(org.getUserId()));
    }

    // ====================================================
    // OBSERVER
    // ====================================================
    @Test
    @Order(19)
    void approve_notifies_registered_users() throws Exception {
        User org = createUser("org_obs", Role.ORGANIZER);
        User admin = createUser("admin_obs", Role.ADMIN);
        User player = createUser("player_obs", Role.PLAYER);

        Tournament t = createTournament(
                org,
                TournamentStatus.PENDING,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)
        );

        Deck deck = createDeck(player, GameType.MAGIC);
        createRegistration(t, player, deck);

        login(admin);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));

        try {
            tournamentService.approveTournament(t.getTournamentId());
        } finally {
            System.setOut(originalOut);
        }

        String output = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Notifica a " + player.getUsername()));
        assertTrue(output.contains("ha cambiato stato in APPROVED"));
    }
}
