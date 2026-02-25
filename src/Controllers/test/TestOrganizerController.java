package Controllers.test;

import Controllers.OrganizerController;
import Controllers.TournamentStatusController;
import Controllers.session.UserSession;
import DomainModel.GameType;
import DomainModel.tournament.Tournament;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import ORM.dao.UserDAO;
import Services.tournament.RegistrationService;
import Services.tournament.TournamentService;
import Services.user.UserService;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestOrganizerController {

    private static Connection connection;
    private static UserDAO userDAO;
    private static UserService userService;
    private static TournamentService tournamentService;
    private static RegistrationService registrationService;

    @BeforeAll
    static void init() {
        connection = DatabaseConnection.getConnection();
        userDAO = new UserDAO(connection);
        userService = new UserService(connection);
        tournamentService = new TournamentService(connection);
        registrationService = new RegistrationService(connection);
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

    private OrganizerController buildController(String cliInput) {
        return new OrganizerController(
                new Scanner(cliInput),
                tournamentService,
                registrationService,
                new TournamentStatusController(tournamentService),
                userService
        );
    }

    @Test
    @Order(1)
    void test01_organizerMenuLogout() throws Exception {
        User organizer = new User("org", "o@mail.com", "pass123", true, Role.ORGANIZER);
        userDAO.createUser(organizer);
        UserSession.getInstance().login(organizer);
        UserSession.getInstance().setGameType(GameType.MAGIC);

        buildController("5\n").organizerMenu();
        assertFalse(UserSession.isLoggedIn());
    }

    @Test
    @Order(2)
    void test02_organizerMenuChangeGame() throws Exception {
        User organizer = new User("org", "o@mail.com", "pass123", true, Role.ORGANIZER);
        userDAO.createUser(organizer);
        UserSession.getInstance().login(organizer);
        UserSession.getInstance().setGameType(GameType.MAGIC);

        buildController("4\n2\n5\n").organizerMenu();
        assertEquals(GameType.YUGIOH, UserSession.getInstance().getGameType());
    }

    @Test
    @Order(3)
    void test03_organizerCreateTournamentFlow() throws Exception {
        User organizer = new User("org", "o@mail.com", "pass123", true, Role.ORGANIZER);
        userDAO.createUser(organizer);
        UserSession.getInstance().login(organizer);
        UserSession.getInstance().setGameType(GameType.YUGIOH);

        buildController("1\nCupTest\ndesc\n8\n31/12/2099\n01/01/2100\n5\n").organizerMenu();
        List<Tournament> tournaments = tournamentService.getTournamentsByOrganizer(organizer, organizer.getUserId());

        assertEquals(1, tournaments.size());
        assertEquals("CupTest", tournaments.get(0).getName());
        assertEquals(GameType.YUGIOH, tournaments.get(0).getGameType());
    }
}
