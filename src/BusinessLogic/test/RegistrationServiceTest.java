/*
package BusinessLogic.test;

import Controllers.session.UserSession;
import BusinessLogic.tournament.RegistrationService;
import DomainModel.GameType;
import DomainModel.card.Deck;
import DomainModel.tournament.Registration;
import DomainModel.tournament.Tournament;
import DomainModel.tournament.TournamentStatus;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import ORM.dao.DeckDAO;
import ORM.dao.RegistrationDAO;
import ORM.dao.TournamentDAO;
import ORM.dao.UserDAO;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RegistrationServiceTest {

    private static Connection connection;
    private static RegistrationService registrationService;
    private static RegistrationDAO registrationDAO;
    private static TournamentDAO tournamentDAO;
    private static UserDAO userDAO;
    private static DeckDAO deckDAO;

    // ====================================================
    // SETUP
    // ====================================================
    @BeforeAll
    static void setup() {
        connection = DatabaseConnection.getConnection();
        registrationService = new RegistrationService(connection);
        registrationDAO = new RegistrationDAO(connection);
        tournamentDAO = new TournamentDAO(connection);
        userDAO = new UserDAO(connection);
        deckDAO = new DeckDAO(connection);
    }

    @BeforeEach
    void resetState() throws SQLException {
        clearTable("registrations");
        clearTable("decks");
        clearTable("tournaments");
        clearTable("users");
        UserSession.getInstance().logout();
    }

    @AfterAll
    static void cleanDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM registrations");
            stmt.executeUpdate("DELETE FROM decks");
            stmt.executeUpdate("DELETE FROM tournaments");
            stmt.executeUpdate("DELETE FROM users");
        }
    }

    private void clearTable(String table) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM " + table)) {
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

    private Tournament createTournament(User organizer, GameType type) throws SQLException {
        Tournament t = new Tournament("Tournament_"+type, type);
        t.setOrganizer(organizer);
        t.setStatus(TournamentStatus.APPROVED);
        t.setDeadline(java.time.LocalDate.now().plusDays(10));
        t.setStartDate(java.time.LocalDate.now().plusDays(20));
        t.setCapacity(100);
        tournamentDAO.createTournament(t);
        return t;
    }

    private Deck createDeck(User owner, String name, GameType type) throws SQLException {
        Deck d = new Deck(name, owner, type);
        deckDAO.createDeck(d);
        return d;
    }

    private Registration createRegistration(Tournament t, User u, Deck d) {
        return new Registration(t, u, d);
    }

    private void login(User u) {
        UserSession.getInstance().login(u);
    }

    // ====================================================
    // 1. PLAYER registra correttamente
    // ====================================================
    @Test
    @Order(1)
    void registerUserToTournament_success() throws SQLException {
        User org = createUser("org1", Role.ORGANIZER);
        User player = createUser("player1", Role.PLAYER);
        Tournament t = createTournament(org, GameType.MAGIC);
        Deck deck = createDeck(player, "MagicDeck", GameType.MAGIC);
        Registration r = createRegistration(t, player, deck);

        login(player);
        registrationService.registerUserToTournament(t.getTournamentId(), r);

        assertEquals(1, registrationDAO.getRegistrationsByUser(player.getUserId()).size());
    }

    // ====================================================
    // 2. NON loggato
    // ====================================================
    @Test
    @Order(2)
    void registerUserToTournament_notLogged() {
        assertThrows(SecurityException.class,
                () -> registrationService.registerUserToTournament(1, null));
    }

    // ====================================================
    // 3. ruolo ≠ PLAYER
    // ====================================================
    @Test
    @Order(3)
    void registerUserToTournament_notPlayer() throws SQLException {
        User org = createUser("org2", Role.ORGANIZER);
        Tournament t = createTournament(org, GameType.MAGIC);
        Deck deck = createDeck(org, "Deck", GameType.MAGIC);
        Registration r = createRegistration(t, org, deck);

        login(org);
        assertThrows(SecurityException.class,
                () -> registrationService.registerUserToTournament(t.getTournamentId(), r));
    }

    // ====================================================
    // 4. doppia registrazione
    // ====================================================
    @Test
    @Order(4)
    void registerUserToTournament_doubleRegistration() throws SQLException {
        User org = createUser("org3", Role.ORGANIZER);
        User player = createUser("player2", Role.PLAYER);
        Tournament t = createTournament(org, GameType.YUGIOH);
        Deck deck = createDeck(player, "YugiDeck", GameType.YUGIOH);

        login(player);
        registrationService.registerUserToTournament(
                t.getTournamentId(), createRegistration(t, player, deck));

        assertThrows(IllegalStateException.class,
                () -> registrationService.registerUserToTournament(
                        t.getTournamentId(), createRegistration(t, player, deck)));
    }

    // ====================================================
    // 5. PLAYER cancella la propria
    // ====================================================
    @Test
    @Order(5)
    void unregisterFromTournament_success() throws SQLException {
        User org = createUser("org4", Role.ORGANIZER);
        User player = createUser("player3", Role.PLAYER);
        Tournament t = createTournament(org, GameType.MAGIC);
        Deck deck = createDeck(player, "Deck", GameType.MAGIC);

        login(player);
        registrationService.registerUserToTournament(
                t.getTournamentId(), createRegistration(t, player, deck));

        registrationService.unregisterFromTournament(t.getTournamentId());
        assertTrue(registrationDAO.getRegistrationsByUser(player.getUserId()).isEmpty());
    }

    // ====================================================
    // 6. PLAYER non registrato
    // ====================================================
    @Test
    @Order(6)
    void unregisterFromTournament_notRegistered() throws SQLException {
        User org = createUser("org5", Role.ORGANIZER);
        User player = createUser("player4", Role.PLAYER);
        Tournament t = createTournament(org, GameType.MAGIC);

        login(player);
        assertThrows(IllegalStateException.class,
                () -> registrationService.unregisterFromTournament(t.getTournamentId()));
    }

    // ====================================================
    // 7. ORGANIZER rimuove
    // ====================================================
    @Test
    @Order(7)
    void unregisterUserFromTournament_organizer() throws SQLException {
        User org = createUser("org6", Role.ORGANIZER);
        User player = createUser("player5", Role.PLAYER);
        Tournament t = createTournament(org, GameType.YUGIOH);
        Deck deck = createDeck(player, "Deck", GameType.YUGIOH);

        login(player);
        registrationService.registerUserToTournament(
                t.getTournamentId(), createRegistration(t, player, deck));

        login(org);
        registrationService.unregisterUserFromTournament(t.getTournamentId(), player.getUserId());
        assertTrue(registrationDAO.getRegistrationsByUser(player.getUserId()).isEmpty());
    }

    // ====================================================
    // 8. ADMIN rimuove
    // ====================================================
    @Test
    @Order(8)
    void unregisterUserFromTournament_admin() throws SQLException {
        User admin = createUser("admin1", Role.ADMIN);
        User org = createUser("org7", Role.ORGANIZER);
        User player = createUser("player6", Role.PLAYER);
        Tournament t = createTournament(org, GameType.MAGIC);
        Deck deck = createDeck(player, "Deck", GameType.MAGIC);

        login(player);
        registrationService.registerUserToTournament(
                t.getTournamentId(), createRegistration(t, player, deck));

        login(admin);
        registrationService.unregisterUserFromTournament(t.getTournamentId(), player.getUserId());
        assertTrue(registrationDAO.getRegistrationsByUser(player.getUserId()).isEmpty());
    }

    // ====================================================
    // 9. PLAYER tenta rimozione
    // ====================================================
    @Test
    @Order(9)
    void unregisterUserFromTournament_playerForbidden() throws SQLException {
        User org = createUser("org8", Role.ORGANIZER);
        User p1 = createUser("player7", Role.PLAYER);
        User p2 = createUser("player8", Role.PLAYER);
        Tournament t = createTournament(org, GameType.MAGIC);
        Deck deck = createDeck(p1, "Deck", GameType.MAGIC);

        login(p1);
        registrationService.registerUserToTournament(
                t.getTournamentId(), createRegistration(t, p1, deck));

        login(p2);
        assertThrows(SecurityException.class,
                () -> registrationService.unregisterUserFromTournament(
                        t.getTournamentId(), p1.getUserId()));
    }

    // ====================================================
    // 10. PLAYER vede le proprie
    // ====================================================
    @Test
    @Order(10)
    void getRegistrationsByUser_self() throws SQLException {
        User org = createUser("org9", Role.ORGANIZER);
        User player = createUser("player9", Role.PLAYER);
        Tournament t = createTournament(org, GameType.MAGIC);
        Deck deck = createDeck(player, "Deck", GameType.MAGIC);

        login(player);
        registrationService.registerUserToTournament(
                t.getTournamentId(), createRegistration(t, player, deck));

        List<Registration> regs =
                registrationService.getRegistrationsByUser(player.getUserId());

        assertEquals(1, regs.size());
    }

    // ====================================================
    // 11. PLAYER vede altri (vietato)
    // ====================================================
    @Test
    @Order(11)
    void getRegistrationsByUser_otherForbidden() throws SQLException {
        User p1 = createUser("player10", Role.PLAYER);
        User p2 = createUser("player11", Role.PLAYER);

        login(p1);
        assertThrows(SecurityException.class,
                () -> registrationService.getRegistrationsByUser(p2.getUserId()));
    }

    // ====================================================
    // 12. ORGANIZER corretto
    // ====================================================
    @Test
    @Order(12)
    void getRegistrationsByTournament_organizerAllowed() throws SQLException {
        User org = createUser("org10", Role.ORGANIZER);
        User player = createUser("player12", Role.PLAYER);
        Tournament t = createTournament(org, GameType.MAGIC);
        Deck deck = createDeck(player, "Deck", GameType.MAGIC);

        login(player);
        registrationService.registerUserToTournament(
                t.getTournamentId(), createRegistration(t, player, deck));

        login(org);
        assertEquals(1,
                registrationService.getRegistrationsByTournament(t.getTournamentId()).size());
    }

    // ====================================================
    // 13. ORGANIZER sbagliato
    // ====================================================
    @Test
    @Order(13)
    void getRegistrationsByTournament_wrongOrganizer() throws SQLException {
        User org1 = createUser("org11", Role.ORGANIZER);
        User org2 = createUser("org12", Role.ORGANIZER);
        Tournament t = createTournament(org1, GameType.MAGIC);

        login(org2);
        assertThrows(SecurityException.class,
                () -> registrationService.getRegistrationsByTournament(t.getTournamentId()));
    }

    // ====================================================
    // 14. ADMIN
    // ====================================================
    @Test
    @Order(14)
    void getAllRegistrations_admin() throws SQLException {
        User admin = createUser("admin2", Role.ADMIN);
        login(admin);
        assertDoesNotThrow(() -> registrationService.getAllRegistrations());
    }

    // ====================================================
    // 15. NON admin
    // ====================================================
    @Test
    @Order(15)
    void getAllRegistrations_notAdmin() throws SQLException {
        User player = createUser("player13", Role.PLAYER);
        login(player);
        assertThrows(SecurityException.class,
                () -> registrationService.getAllRegistrations());
    }

    // ====================================================
    // 16. deck GameType errato
    // ====================================================
    @Test
    @Order(16)
    void registerUserToTournament_wrongDeckGameType() throws SQLException {
        User org = createUser("org13", Role.ORGANIZER);
        User player = createUser("player14", Role.PLAYER);
        Tournament t = createTournament(org, GameType.MAGIC);
        Deck wrongDeck = createDeck(player, "WrongDeck", GameType.YUGIOH);

        login(player);
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.registerUserToTournament(
                        t.getTournamentId(),
                        createRegistration(t, player, wrongDeck)));
    }
}
*/
