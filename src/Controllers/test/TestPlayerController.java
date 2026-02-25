package Controllers.test;

import Controllers.PlayerController;
import Controllers.TournamentStatusController;
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
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPlayerController {

    private static Connection connection;
    private static UserDAO userDAO;
    private static UserService userService;
    private static CardService cardService;
    private static CardDAO cardDAO;
    private static DeckService deckService;
    private static DeckDAO deckDAO;
    private static TournamentService tournamentService;
    private static RegistrationService registrationService;
    private static RegistrationDAO registrationDAO;

    private static User player;
    private static User organizer;
    private static int yugiCardId;
    private static int yugiCard2Id;
    private static int magicCardId;
    private static int baseYugiDeckId;

    @BeforeAll
    static void init() throws Exception {
        connection = DatabaseConnection.getConnection();
        userDAO = new UserDAO(connection);
        userService = new UserService(connection);
        cardService = new CardService(connection);
        cardDAO = new CardDAO(connection);
        deckService = new DeckService(connection);
        deckDAO = new DeckDAO(connection);
        tournamentService = new TournamentService(connection);
        registrationService = new RegistrationService(connection);
        registrationDAO = new RegistrationDAO(connection);

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
        player = new User("player_ctrl", "player_ctrl@mail.com", "pass123", true, Role.PLAYER);
        organizer = new User("org_player_ctrl", "org_player_ctrl@mail.com", "pass123", true, Role.ORGANIZER);
        User admin = new User("admin_player_ctrl", "admin_player_ctrl@mail.com", "pass123", true, Role.ADMIN);
        userDAO.createUser(player);
        userDAO.createUser(organizer);
        userDAO.createUser(admin);

        Card y1 = new Card("PCardY1", GameType.YUGIOH);
        Card y2 = new Card("PCardY2", GameType.YUGIOH);
        Card m1 = new Card("PCardM1", GameType.MAGIC);
        cardDAO.addCard(y1);
        cardDAO.addCard(y2);
        cardDAO.addCard(m1);
        yugiCardId = y1.getCardId();
        yugiCard2Id = y2.getCardId();
        magicCardId = m1.getCardId();

        Deck yDeck = new Deck("PBaseYDeck", player, GameType.YUGIOH);
        deckDAO.createDeck(yDeck);
        deckDAO.addCardToDeck(yDeck.getDeckId(), yugiCardId);
        baseYugiDeckId = yDeck.getDeckId();

        Deck mDeck = new Deck("PBaseMDeck", player, GameType.MAGIC);
        deckDAO.createDeck(mDeck);
        deckDAO.addCardToDeck(mDeck.getDeckId(), magicCardId);

        Tournament approvedY = buildTournament("PApprovedY", GameType.YUGIOH, TournamentStatus.PENDING);
        tournamentService.createTournament(organizer, approvedY);
        tournamentService.approveTournament(organizer, approvedY.getTournamentId());
        registrationService.registerUserToTournament(
                player,
                approvedY.getTournamentId(),
                new Registration(tournamentService.getTournamentById(approvedY.getTournamentId()), player, yDeck)
        );

        Tournament approvedM = buildTournament("PApprovedM", GameType.MAGIC, TournamentStatus.PENDING);
        tournamentService.createTournament(organizer, approvedM);
        tournamentService.approveTournament(organizer, approvedM.getTournamentId());
    }

    private static Tournament buildTournament(String name, GameType gameType, TournamentStatus status) {
        Tournament t = new Tournament();
        t.setName(name);
        t.setDescription("desc");
        t.setOrganizer(organizer);
        t.setCapacity(8);
        t.setDeadline(LocalDate.now().plusDays(2));
        t.setStartDate(LocalDate.now().plusDays(4));
        t.setStatus(status);
        t.setGameType(gameType);
        t.setRegistrations(new ArrayList<>());
        return t;
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

    private void loginAsPlayer(GameType gameType) {
        UserSession.getInstance().login(player);
        UserSession.getInstance().setGameType(gameType);
    }

    @Test
    @Order(1)
    void test01_playerMenuRequiresPlayerRole() {
        UserSession.getInstance().login(organizer);
        UserSession.getInstance().setGameType(GameType.YUGIOH);
        assertThrows(SecurityException.class, () -> buildController("6\n").playerMenu());
    }

    @Test
    @Order(2)
    void test02_playerMenuLogout() throws Exception {
        loginAsPlayer(GameType.YUGIOH);
        buildController("6\n").playerMenu();
        assertFalse(UserSession.isLoggedIn());
    }

    @Test
    @Order(3)
    void test03_playerMenuChangeGame() throws Exception {
        loginAsPlayer(GameType.MAGIC);
        buildController("5\n2\n6\n").playerMenu();
        assertEquals(GameType.YUGIOH, UserSession.getInstance().getGameType());
    }

    @Test
    @Order(4)
    void test04_areaPersonaleChangeEmailAndPassword() throws Exception {
        loginAsPlayer(GameType.YUGIOH);
        buildController("4\n2\nplayer_ctrl_new@mail.com\n3\nnewpass321\n4\n6\n").playerMenu();

        User loaded = userDAO.getUserById(player.getUserId());
        assertEquals("player_ctrl_new@mail.com", loaded.getEmail());
        assertNotNull(userService.login("player_ctrl", "newpass321"));
        player = loaded;
    }

    @Test
    @Order(5)
    void test05_gestioneMazziCreateDeck() throws Exception {
        loginAsPlayer(GameType.YUGIOH);
        buildController("1\n2\nPNewDeck\n3\n6\n").playerMenu();

        List<Deck> myDecks = deckService.getMyDecks(player);
        assertTrue(myDecks.stream().anyMatch(d -> d.getDeckName().equals("PNewDeck") && d.getGameType() == GameType.YUGIOH));
    }

    @Test
    @Order(6)
    void test06_gestioneMazziRenameDeck() throws Exception {
        Deck temp = deckService.createDeck(player, "PTempRename", GameType.YUGIOH);
        loginAsPlayer(GameType.YUGIOH);
        String cli = "1\n1\n" + temp.getDeckId() + "\n1\n1\nPTempRenamed\n4\n3\n3\n6\n";
        buildController(cli).playerMenu();

        assertEquals("PTempRenamed", deckService.getDeckById(temp.getDeckId()).getDeckName());
    }

    @Test
    @Order(7)
    void test07_gestioneMazziAddAndRemoveCard() throws Exception {
        Deck temp = deckService.createDeck(player, "PTempCards", GameType.YUGIOH);
        loginAsPlayer(GameType.YUGIOH);
        String addCli = "1\n1\n" + temp.getDeckId() + "\n1\n2\n" + yugiCard2Id + "\n4\n3\n3\n6\n";
        buildController(addCli).playerMenu();

        List<Card> afterAdd = deckService.getCardsInDeck(temp.getDeckId());
        assertTrue(afterAdd.stream().anyMatch(c -> c.getCardId() == yugiCard2Id));

        loginAsPlayer(GameType.YUGIOH);
        String removeCli = "1\n1\n" + temp.getDeckId() + "\n1\n3\n" + yugiCard2Id + "\n4\n3\n3\n6\n";
        buildController(removeCli).playerMenu();
        List<Card> afterRemove = deckService.getCardsInDeck(temp.getDeckId());
        assertFalse(afterRemove.stream().anyMatch(c -> c.getCardId() == yugiCard2Id));
    }

    @Test
    @Order(8)
    void test08_gestioneMazziDeleteDeck() throws Exception {
        Deck temp = deckService.createDeck(player, "PTempDelete", GameType.YUGIOH);
        loginAsPlayer(GameType.YUGIOH);
        String cli = "1\n1\n" + temp.getDeckId() + "\n2\nsi\n3\n6\n";
        buildController(cli).playerMenu();

        assertThrows(IllegalArgumentException.class, () -> deckService.getDeckById(temp.getDeckId()));
    }

    @Test
    @Order(9)
    void test09_checkTorneiIscrivitiFlow() throws Exception {
        Tournament t = buildTournament("PJoinTournament", GameType.YUGIOH, TournamentStatus.PENDING);
        tournamentService.createTournament(organizer, t);
        tournamentService.approveTournament(organizer, t.getTournamentId());

        loginAsPlayer(GameType.YUGIOH);
        String cli = "3\n1\n" + t.getTournamentId() + "\n1\n" + baseYugiDeckId + "\nsi\n3\n6\n";
        buildController(cli).playerMenu();

        assertTrue(registrationDAO.isUserRegistered(t.getTournamentId(), player.getUserId()));
    }

    @Test
    @Order(10)
    void test10_checkTorneiMieiTorneiDisiscrizione() throws Exception {
        Tournament t = buildTournament("PLeaveTournament", GameType.YUGIOH, TournamentStatus.PENDING);
        tournamentService.createTournament(organizer, t);
        tournamentService.approveTournament(organizer, t.getTournamentId());

        Registration reg = new Registration(tournamentService.getTournamentById(t.getTournamentId()), player, deckService.getDeckById(baseYugiDeckId));
        registrationService.registerUserToTournament(player, t.getTournamentId(), reg);
        assertTrue(registrationDAO.isUserRegistered(t.getTournamentId(), player.getUserId()));

        loginAsPlayer(GameType.YUGIOH);
        String cli = "3\n2\n1\n" + t.getTournamentId() + "\n2\n3\n6\n";
        buildController(cli).playerMenu();

        assertFalse(registrationDAO.isUserRegistered(t.getTournamentId(), player.getUserId()));
    }

    @Test
    @Order(11)
    void test11_catalogoCarteFlow() throws Exception {
        loginAsPlayer(GameType.YUGIOH);
        buildController("2\n1\n2\nPCardY\n3\n6\n").playerMenu();
        assertTrue(cardService.getCardsByGameType(GameType.YUGIOH).size() >= 2);
    }
}
