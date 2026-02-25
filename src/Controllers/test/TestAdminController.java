package Controllers.test;

import Controllers.AdminController;
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
import ORM.dao.UserDAO;
import Services.card.CardService;
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
public class TestAdminController {

    private static Connection connection;
    private static UserService userService;
    private static UserDAO userDAO;
    private static CardService cardService;
    private static CardDAO cardDAO;
    private static DeckDAO deckDAO;
    private static TournamentService tournamentService;
    private static RegistrationService registrationService;

    private static User admin;
    private static User organizer;
    private static User player;
    private static int yugiPendingTournamentId;

    @BeforeAll
    static void init() throws Exception {
        connection = DatabaseConnection.getConnection();
        userService = new UserService(connection);
        userDAO = new UserDAO(connection);
        cardService = new CardService(connection);
        cardDAO = new CardDAO(connection);
        deckDAO = new DeckDAO(connection);
        tournamentService = new TournamentService(connection);
        registrationService = new RegistrationService(connection);

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
        admin = new User("admin_ctrl", "admin_ctrl@mail.com", "pass123", true, Role.ADMIN);
        organizer = new User("org_ctrl", "org_ctrl@mail.com", "pass123", true, Role.ORGANIZER);
        player = new User("player_ctrl", "player_ctrl@mail.com", "pass123", true, Role.PLAYER);
        userDAO.createUser(admin);
        userDAO.createUser(organizer);
        userDAO.createUser(player);

        Card c1 = new Card("AdminSeedYugi", GameType.YUGIOH);
        Card c2 = new Card("AdminSeedMagic", GameType.MAGIC);
        cardDAO.addCard(c1);
        cardDAO.addCard(c2);

        Deck d = new Deck("AdminSeedDeck", player, GameType.YUGIOH);
        deckDAO.createDeck(d);
        deckDAO.addCardToDeck(d.getDeckId(), c1.getCardId());

        Tournament pendingYugi = buildTournament("PendingYugi", GameType.YUGIOH, TournamentStatus.PENDING);
        tournamentService.createTournament(organizer, pendingYugi);
        yugiPendingTournamentId = pendingYugi.getTournamentId();

        Tournament pendingMagic = buildTournament("PendingMagic", GameType.MAGIC, TournamentStatus.PENDING);
        tournamentService.createTournament(organizer, pendingMagic);

        Tournament approvedY = buildTournament("ApprovedYugiSeed", GameType.YUGIOH, TournamentStatus.PENDING);
        tournamentService.createTournament(organizer, approvedY);
        tournamentService.approveTournament(organizer, approvedY.getTournamentId());
        registrationService.registerUserToTournament(
                player,
                approvedY.getTournamentId(),
                new Registration(tournamentService.getTournamentById(approvedY.getTournamentId()), player, d)
        );
    }

    private static Tournament buildTournament(String name, GameType gameType, TournamentStatus status) {
        Tournament t = new Tournament();
        t.setName(name);
        t.setDescription("desc");
        t.setOrganizer(organizer);
        t.setCapacity(8);
        t.setDeadline(LocalDate.now().plusDays(2));
        t.setStartDate(LocalDate.now().plusDays(3));
        t.setStatus(status);
        t.setGameType(gameType);
        t.setRegistrations(new ArrayList<>());
        return t;
    }

    private AdminController buildController(String cli) {
        return new AdminController(new Scanner(cli), userService, cardService, tournamentService);
    }

    private void loginAsAdmin(GameType gt) {
        UserSession.getInstance().login(admin);
        UserSession.getInstance().setGameType(gt);
    }

    @Test
    @Order(1)
    void test01_adminMenuRequiresAdminRole() {
        UserSession.getInstance().login(player);
        UserSession.getInstance().setGameType(GameType.YUGIOH);
        assertThrows(SecurityException.class, () -> buildController("6\n").adminMenu());
    }

    @Test
    @Order(2)
    void test02_adminMenuLogout() throws Exception {
        loginAsAdmin(GameType.YUGIOH);
        buildController("6\n").adminMenu();
        assertFalse(UserSession.isLoggedIn());
    }

    @Test
    @Order(3)
    void test03_adminMenuChangeGameType() throws Exception {
        loginAsAdmin(GameType.MAGIC);
        buildController("5\n3\n6\n").adminMenu();
        assertEquals(GameType.POKEMON, UserSession.getInstance().getGameType());
    }

    @Test
    @Order(4)
    void test04_areaPersonaleChangeUsernameEmailPassword() throws Exception {
        loginAsAdmin(GameType.YUGIOH);
        buildController("4\n1\nadmin_ctrl_new\n2\nadmin_ctrl_new@mail.com\n3\nnewpass123\n4\n6\n").adminMenu();

        User loaded = userDAO.getUserById(admin.getUserId());
        assertEquals("admin_ctrl_new", loaded.getUsername());
        assertEquals("admin_ctrl_new@mail.com", loaded.getEmail());
        assertNotNull(userService.login("admin_ctrl_new", "newpass123"));
        admin = loaded;
    }

    @Test
    @Order(5)
    void test05_gestioneUtentiChangeRole() throws Exception {
        loginAsAdmin(GameType.YUGIOH);
        buildController("2\n1\n1\n" + player.getUserId() + "\nORGANIZER\n3\n3\n6\n").adminMenu();

        User updated = userDAO.getUserById(player.getUserId());
        assertEquals(Role.ORGANIZER, updated.getRole());
    }

    @Test
    @Order(6)
    void test06_gestioneUtentiDisableEnable() throws Exception {
        loginAsAdmin(GameType.YUGIOH);
        buildController("2\n1\n2\n" + player.getUserId() + "\nno\n3\n3\n6\n").adminMenu();
        assertFalse(userDAO.getUserById(player.getUserId()).isEnabled());

        loginAsAdmin(GameType.YUGIOH);
        buildController("2\n2\nplayer_ctrl\n2\n" + player.getUserId() + "\nsi\n3\n3\n6\n").adminMenu();
        assertTrue(userDAO.getUserById(player.getUserId()).isEnabled());
    }

    @Test
    @Order(7)
    void test07_gestioneCarteCreateAndModify() throws Exception {
        loginAsAdmin(GameType.YUGIOH);
        buildController("1\n3\nCtrlCardNew\n4\n6\n").adminMenu();
        Card created = cardDAO.getCardByName("CtrlCardNew");
        assertNotNull(created);

        loginAsAdmin(GameType.YUGIOH);
        buildController("1\n1\n1\n" + created.getCardId() + "\nCtrlCardRenamed\n\n3\n4\n6\n").adminMenu();
        assertNotNull(cardDAO.getCardByName("CtrlCardRenamed"));
    }

    @Test
    @Order(8)
    void test08_gestioneCarteDelete() throws Exception {
        Card toDelete = new Card("CtrlCardDelete", GameType.YUGIOH);
        cardDAO.addCard(toDelete);

        loginAsAdmin(GameType.YUGIOH);
        buildController("1\n1\n2\n" + toDelete.getCardId() + "\n3\n4\n6\n").adminMenu();
        assertNull(cardDAO.getCardById(toDelete.getCardId()));
    }

    @Test
    @Order(9)
    void test09_approvazioneTorneiApproveAndReject() throws Exception {
        loginAsAdmin(GameType.YUGIOH);
        buildController("3\n" + yugiPendingTournamentId + "\nsi\n6\n").adminMenu();
        assertEquals(TournamentStatus.APPROVED, tournamentService.getTournamentById(yugiPendingTournamentId).getStatus());

        Tournament rejectCandidate = buildTournament("PendingToReject", GameType.YUGIOH, TournamentStatus.PENDING);
        tournamentService.createTournament(organizer, rejectCandidate);

        loginAsAdmin(GameType.YUGIOH);
        buildController("3\n" + rejectCandidate.getTournamentId() + "\nno\n6\n").adminMenu();
        assertEquals(TournamentStatus.REJECTED, tournamentService.getTournamentById(rejectCandidate.getTournamentId()).getStatus());
    }

    @Test
    @Order(10)
    void test10_getAllUsersStillAccessibleAfterFlows() throws Exception {
        loginAsAdmin(GameType.YUGIOH);
        List<User> users = userService.getAllUsers(admin);
        assertTrue(users.size() >= 3);
    }
}
