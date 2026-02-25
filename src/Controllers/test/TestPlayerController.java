package Controllers.test;

import Controllers.PlayerController;
import Controllers.TournamentStatusController;
import Controllers.session.UserSession;
import DomainModel.GameType;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import ORM.dao.UserDAO;
import Services.card.CardService;
import Services.card.DeckService;
import Services.tournament.RegistrationService;
import Services.tournament.TournamentService;
import Services.user.UserService;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPlayerController {

    private static Connection connection;
    private static UserDAO userDAO;
    private static UserService userService;
    private static CardService cardService;
    private static DeckService deckService;
    private static TournamentService tournamentService;
    private static RegistrationService registrationService;

    @BeforeAll
    static void init() {
        connection = DatabaseConnection.getConnection();
        userDAO = new UserDAO(connection);
        userService = new UserService(connection);
        cardService = new CardService(connection);
        deckService = new DeckService(connection);
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

    private PlayerController buildController(String cliInput) {
        return new PlayerController(
                new Scanner(cliInput),
                cardService,
                deckService,
                tournamentService,
                registrationService,
                new TournamentStatusController(tournamentService),
                userService
        );
    }

    @Test
    @Order(1)
    void test01_playerMenuLogout() throws Exception {
        User player = new User("p", "p@mail.com", "pass123", true, Role.PLAYER);
        userDAO.createUser(player);
        UserSession.getInstance().login(player);
        UserSession.getInstance().setGameType(GameType.MAGIC);

        buildController("6\n").playerMenu();
        assertFalse(UserSession.isLoggedIn());
    }

    @Test
    @Order(2)
    void test02_playerMenuChangeGame() throws Exception {
        User player = new User("p", "p@mail.com", "pass123", true, Role.PLAYER);
        userDAO.createUser(player);
        UserSession.getInstance().login(player);
        UserSession.getInstance().setGameType(GameType.MAGIC);

        buildController("5\n3\n6\n").playerMenu();
        assertEquals(GameType.POKEMON, UserSession.getInstance().getGameType());
    }

    @Test
    @Order(3)
    void test03_playerAreaPersonaleChangeEmail() throws Exception {
        User player = new User("p", "p@mail.com", "pass123", true, Role.PLAYER);
        userDAO.createUser(player);
        UserSession.getInstance().login(player);
        UserSession.getInstance().setGameType(GameType.YUGIOH);

        buildController("4\n2\nnew_p@mail.com\n4\n6\n").playerMenu();
        User loaded = userDAO.getUserById(player.getUserId());
        assertEquals("new_p@mail.com", loaded.getEmail());
    }
}
