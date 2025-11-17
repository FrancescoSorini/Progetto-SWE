package ORM.dao.test;

import ORM.connection.DatabaseConnection;
import ORM.dao.*;

import DomainModel.tournament.*;
import DomainModel.user.*;

import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestRegistrationDAO {

    private static Connection connection;

    private RegistrationDAO registrationDAO;
    private UserDAO userDAO;
    private TournamentDAO tournamentDAO;

    private int userId;
    private int tournamentId;

    @BeforeAll
    static void init() throws Exception {
        connection = DatabaseConnection.getConnection();
    }

    @BeforeEach
    void setup() throws Exception {

        // Reset totale DB
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                TRUNCATE TABLE registrations,
                                tournaments,
                                users
                RESTART IDENTITY CASCADE
            """);
        }

        registrationDAO = new RegistrationDAO(connection);
        userDAO = new UserDAO(connection);
        tournamentDAO = new TournamentDAO(connection);


        // Creazione utente Player
        User user = new User("testUser", "test@mail.com", "pwd123", true, Role.PLAYER);
        userDAO.createUser(user);
        userId = user.getUserId();


        // Creazione torneo
        Tournament tournament = new Tournament("Test Tournament");
        tournament.setDescription("Test description");
        tournament.setOrganizer(user);
        tournament.setCapacity(16);
        tournament.setDeadline(LocalDate.now().plusDays(5));
        tournament.setStartDate(LocalDate.now().plusDays(10));
        tournament.setStatus(TournamentStatus.APPROVED);  // enum valido

        tournamentDAO.createTournament(tournament);
        tournamentId = tournament.getTournamentId();
    }

    @AfterAll
    static void cleanup() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                TRUNCATE TABLE registrations,
                                tournaments,
                                users
                RESTART IDENTITY CASCADE
            """);
        }
    }

    @Test
    @Order(1)
    void testCreateRegistration() throws Exception {

        User user = userDAO.getUserById(userId);
        Tournament tournament = tournamentDAO.getTournamentById(tournamentId);

        Registration reg = new Registration(tournament, user);
        reg.setRegistrationDate(LocalDateTime.now());

        registrationDAO.createRegistration(reg);

        Registration dbReg = registrationDAO.getRegistration(tournamentId, userId);
        assertNotNull(dbReg);
    }

    @Test
    @Order(2)
    void testGetRegistration() throws Exception {

        User user = userDAO.getUserById(userId);
        Tournament tournament = tournamentDAO.getTournamentById(tournamentId);

        Registration reg = new Registration(tournament, user);
        reg.setRegistrationDate(LocalDateTime.now());
        registrationDAO.createRegistration(reg);

        Registration loaded = registrationDAO.getRegistration(tournamentId, userId);

        assertNotNull(loaded);
        assertEquals(userId, loaded.getUser().getUserId());
        assertEquals(tournamentId, loaded.getTournament().getTournamentId());
    }

    @Test
    @Order(3)
    void testGetRegistrationsByUser() throws Exception {

        User user = userDAO.getUserById(userId);
        Tournament tournament = tournamentDAO.getTournamentById(tournamentId);

        Registration reg = new Registration(tournament, user);
        reg.setRegistrationDate(LocalDateTime.now());

        registrationDAO.createRegistration(reg);

        List<Registration> list = registrationDAO.getRegistrationsByUser(userId);
        assertEquals(1, list.size());
    }

    @Test
    @Order(4)
    void testGetRegistrationsByTournament() throws Exception {

        User user = userDAO.getUserById(userId);
        Tournament tournament = tournamentDAO.getTournamentById(tournamentId);

        Registration reg = new Registration(tournament, user);
        reg.setRegistrationDate(LocalDateTime.now());

        registrationDAO.createRegistration(reg);

        List<Registration> list = registrationDAO.getRegistrationsByTournament(tournamentId);

        assertEquals(1, list.size());
        assertEquals(userId, list.getFirst().getUser().getUserId());
    }

    @Test
    @Order(5)
    void testGetAllRegistrations() throws Exception {

        User user = userDAO.getUserById(userId);
        Tournament tournament = tournamentDAO.getTournamentById(tournamentId);

        Registration reg = new Registration(tournament, user);
        reg.setRegistrationDate(LocalDateTime.now());

        registrationDAO.createRegistration(reg);

        List<Registration> list = registrationDAO.getAllRegistrations();

        assertEquals(1, list.size());
    }

    @Test
    @Order(6)
    void testDeleteRegistration() throws Exception {

        User user = userDAO.getUserById(userId);
        Tournament tournament = tournamentDAO.getTournamentById(tournamentId);

        Registration reg = new Registration(tournament, user);
        reg.setRegistrationDate(LocalDateTime.now());

        registrationDAO.createRegistration(reg);

        registrationDAO.deleteRegistration(tournamentId, userId);

        assertNull(registrationDAO.getRegistration(tournamentId, userId));
    }

    @Test
    @Order(7)
    void testIsUserRegistered() throws Exception {

        User user = userDAO.getUserById(userId);
        Tournament tournament = tournamentDAO.getTournamentById(tournamentId);

        Registration reg = new Registration(tournament, user);
        reg.setRegistrationDate(LocalDateTime.now());

        registrationDAO.createRegistration(reg);

        assertTrue(registrationDAO.isUserRegistered(tournamentId, userId));

        registrationDAO.deleteRegistration(tournamentId, userId);

        assertFalse(registrationDAO.isUserRegistered(tournamentId, userId));
    }
}