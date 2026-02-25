package Controllers.test;

import Controllers.AdminController;
import Controllers.session.UserSession;
import DomainModel.GameType;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import ORM.dao.UserDAO;
import Services.card.CardService;
import Services.tournament.TournamentService;
import Services.user.UserService;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestAdminController {

    private static Connection connection;
    private static UserService userService;
    private static UserDAO userDAO;
    private static CardService cardService;
    private static TournamentService tournamentService;

    @BeforeAll
    static void init() {
        connection = DatabaseConnection.getConnection();
        userService = new UserService(connection);
        userDAO = new UserDAO(connection);
        cardService = new CardService(connection);
        tournamentService = new TournamentService(connection);
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

    @Test
    @Order(1)
    void test01_adminMenuLogout() throws Exception {
        User admin = new User("admin", "a@mail.com", "pass123", true, Role.ADMIN);
        userDAO.createUser(admin);
        UserSession.getInstance().login(admin);
        UserSession.getInstance().setGameType(GameType.MAGIC);

        AdminController controller = new AdminController(new Scanner("6\n"), userService, cardService, tournamentService);
        controller.adminMenu();

        assertFalse(UserSession.isLoggedIn());
    }

    @Test
    @Order(2)
    void test02_adminMenuChangeGameType() throws Exception {
        User admin = new User("admin", "a@mail.com", "pass123", true, Role.ADMIN);
        userDAO.createUser(admin);
        UserSession.getInstance().login(admin);
        UserSession.getInstance().setGameType(GameType.MAGIC);

        AdminController controller = new AdminController(new Scanner("5\n3\n6\n"), userService, cardService, tournamentService);
        controller.adminMenu();

        assertEquals(GameType.POKEMON, UserSession.getInstance().getGameType());
    }

    @Test
    @Order(3)
    void test03_adminAreaPersonaleChangeUsername() throws Exception {
        User admin = new User("admin", "a@mail.com", "pass123", true, Role.ADMIN);
        userDAO.createUser(admin);
        UserSession.getInstance().login(admin);
        UserSession.getInstance().setGameType(GameType.YUGIOH);

        AdminController controller = new AdminController(
                new Scanner("4\n1\nadmin_new\n4\n6\n"), userService, cardService, tournamentService
        );
        controller.adminMenu();

        User loaded = userDAO.getUserById(admin.getUserId());
        assertEquals("admin_new", loaded.getUsername());
    }
}
