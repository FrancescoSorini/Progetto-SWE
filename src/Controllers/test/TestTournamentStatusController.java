package Controllers.test;

import Controllers.TournamentStatusController;
import DomainModel.GameType;
import DomainModel.tournament.Tournament;
import DomainModel.tournament.TournamentStatus;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import ORM.dao.UserDAO;
import Services.tournament.TournamentService;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestTournamentStatusController {

    private static Connection connection;
    private static TournamentService tournamentService;
    private static TournamentStatusController controller;
    private static UserDAO userDAO;

    @BeforeAll
    static void init() {
        connection = DatabaseConnection.getConnection();
        tournamentService = new TournamentService(connection);
        controller = new TournamentStatusController(tournamentService);
        userDAO = new UserDAO(connection);
    }

    @BeforeEach
    void resetBeforeEach() throws Exception {
        truncateAll();
    }

    @AfterEach
    void resetAfterEach() throws Exception {
        truncateAll();
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
    void test01_syncTournamentStatusesMovesApprovedToReady() throws Exception {
        User org = new User("org", "o@mail.com", "pass123", true, Role.ORGANIZER);
        userDAO.createUser(org);

        Tournament t = new Tournament();
        t.setName("Cup");
        t.setDescription("desc");
        t.setOrganizer(org);
        t.setCapacity(8);
        t.setDeadline(LocalDate.now());
        t.setStartDate(LocalDate.now().plusDays(2));
        t.setStatus(TournamentStatus.PENDING);
        t.setGameType(GameType.MAGIC);
        t.setRegistrations(new ArrayList<>());

        tournamentService.createTournament(org, t);
        tournamentService.approveTournament(org, t.getTournamentId());

        controller.syncTournamentStatuses();

        Tournament loaded = tournamentService.getTournamentById(t.getTournamentId());
        assertEquals(TournamentStatus.READY, loaded.getStatus());
    }
}
