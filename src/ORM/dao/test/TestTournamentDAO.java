package ORM.dao.test;

import ORM.connection.DatabaseConnection;
import ORM.dao.*;
import DomainModel.tournament.*;
import DomainModel.user.*;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestTournamentDAO {

    private static Connection connection;
    private TournamentDAO tournamentDAO;
    private UserDAO userDAO;
    private int organizerId;

    @BeforeAll
    static void init() throws Exception {
        connection = DatabaseConnection.getConnection();
    }

    @BeforeEach
    void setup() throws Exception {
        // Reset DB
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                TRUNCATE TABLE registrations,
                                tournaments,
                                users
                RESTART IDENTITY CASCADE
            """);
        }

        tournamentDAO = new TournamentDAO(connection);
        userDAO = new UserDAO(connection);

        // Create organizer
        User organizer = new User("organizer", "org@example.com", "pwd", true, Role.ORGANIZER);
        userDAO.createUser(organizer);
        organizerId = organizer.getUserId();
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
    void testCreateTournament() throws Exception {
        User organizer = userDAO.getUserById(organizerId);
        Tournament tournament = new Tournament("Test Tournament");
        tournament.setDescription("Description");
        tournament.setOrganizer(organizer);
        tournament.setCapacity(16);
        tournament.setDeadline(LocalDate.now().plusDays(5));
        tournament.setStartDate(LocalDate.now().plusDays(10));
        tournament.setStatus(TournamentStatus.PENDING);

        tournamentDAO.createTournament(tournament);

        assertTrue(tournament.getTournamentId() > 0);

        Tournament loaded = tournamentDAO.getTournamentById(tournament.getTournamentId());
        assertNotNull(loaded);
        assertEquals("Test Tournament", loaded.getName());
    }

    @Test
    @Order(2)
    void testGetTournamentById() throws Exception {
        User organizer = userDAO.getUserById(organizerId);
        Tournament tournament = new Tournament("Get Test");
        tournament.setDescription("Desc");
        tournament.setOrganizer(organizer);
        tournament.setCapacity(8);
        tournament.setDeadline(LocalDate.now().plusDays(3));
        tournament.setStartDate(LocalDate.now().plusDays(7));
        tournament.setStatus(TournamentStatus.APPROVED);

        tournamentDAO.createTournament(tournament);

        Tournament loaded = tournamentDAO.getTournamentById(tournament.getTournamentId());
        assertNotNull(loaded);
        assertEquals("Get Test", loaded.getName());
        assertEquals(TournamentStatus.APPROVED, loaded.getStatus());
        // Since no registrations, list should be empty
        assertEquals(0, loaded.getRegistrations().size());
    }

    @Test
    @Order(3)
    void testGetAllTournaments() throws Exception {
        User organizer = userDAO.getUserById(organizerId);

        Tournament t1 = new Tournament("T1");
        t1.setDescription("D1");
        t1.setOrganizer(organizer);
        t1.setCapacity(4);
        t1.setDeadline(LocalDate.now().plusDays(1));
        t1.setStartDate(LocalDate.now().plusDays(2));
        t1.setStatus(TournamentStatus.READY);

        tournamentDAO.createTournament(t1);

        Tournament t2 = new Tournament("T2");
        t2.setDescription("D2");
        t2.setOrganizer(organizer);
        t2.setCapacity(32);
        t2.setDeadline(LocalDate.now().plusDays(10));
        t2.setStartDate(LocalDate.now().plusDays(15));
        t2.setStatus(TournamentStatus.CLOSED);

        tournamentDAO.createTournament(t2);

        List<Tournament> all = tournamentDAO.getAllTournaments();
        assertEquals(2, all.size());
    }

    @Test
    @Order(4)
    void testUpdateTournament() throws Exception {
        User organizer = userDAO.getUserById(organizerId);
        Tournament tournament = new Tournament("Update Test");
        tournament.setDescription("Old Desc");
        tournament.setOrganizer(organizer);
        tournament.setCapacity(16);
        tournament.setDeadline(LocalDate.now().plusDays(5));
        tournament.setStartDate(LocalDate.now().plusDays(10));
        tournament.setStatus(TournamentStatus.PENDING);

        tournamentDAO.createTournament(tournament);

        tournament.setName("Updated Name");
        tournament.setDescription("New Desc");
        tournament.setCapacity(20);
        tournament.setStatus(TournamentStatus.APPROVED);

        tournamentDAO.updateTournament(tournament);

        Tournament updated = tournamentDAO.getTournamentById(tournament.getTournamentId());
        assertEquals("Updated Name", updated.getName());
        assertEquals("New Desc", updated.getDescription());
        assertEquals(20, updated.getCapacity());
        assertEquals(TournamentStatus.APPROVED, updated.getStatus());
    }

    @Test
    @Order(5)
    void testDeleteTournament() throws Exception {
        User organizer = userDAO.getUserById(organizerId);
        Tournament tournament = new Tournament("Delete Test");
        tournament.setDescription("Desc");
        tournament.setOrganizer(organizer);
        tournament.setCapacity(8);
        tournament.setDeadline(LocalDate.now().plusDays(3));
        tournament.setStartDate(LocalDate.now().plusDays(7));
        tournament.setStatus(TournamentStatus.REJECTED);

        tournamentDAO.createTournament(tournament);

        tournamentDAO.deleteTournament(tournament.getTournamentId());

        Tournament deleted = tournamentDAO.getTournamentById(tournament.getTournamentId());
        assertNull(deleted);
    }
}
