package Controllers.test;

import Controllers.OrganizerController;
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
import ORM.dao.TournamentDAO;
import ORM.dao.UserDAO;
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
public class TestOrganizerController {

    private static Connection connection;
    private static UserDAO userDAO;
    private static UserService userService;
    private static TournamentService tournamentService;
    private static TournamentDAO tournamentDAO;
    private static RegistrationService registrationService;
    private static RegistrationDAO registrationDAO;
    private static CardDAO cardDAO;
    private static DeckDAO deckDAO;

    private static User organizer;
    private static User player1;
    private static User player2;
    private static int approvedTournamentId;
    private static int readyTournamentId;
    private static int rejectedTournamentId;
    private static int ongoingTournamentId;
    private static int finishedTournamentId;

    @BeforeAll
    static void init() throws Exception {
        connection = DatabaseConnection.getConnection();
        userDAO = new UserDAO(connection);
        userService = new UserService(connection);
        tournamentService = new TournamentService(connection);
        tournamentDAO = new TournamentDAO(connection);
        registrationService = new RegistrationService(connection);
        registrationDAO = new RegistrationDAO(connection);
        cardDAO = new CardDAO(connection);
        deckDAO = new DeckDAO(connection);

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
        organizer = new User("org_ctrl", "org_ctrl@mail.com", "pass123", true, Role.ORGANIZER);
        player1 = new User("org_p1", "org_p1@mail.com", "pass123", true, Role.PLAYER);
        player2 = new User("org_p2", "org_p2@mail.com", "pass123", true, Role.PLAYER);
        userDAO.createUser(organizer);
        userDAO.createUser(player1);
        userDAO.createUser(player2);

        Card yCard = new Card("OrgSeedYCard", GameType.YUGIOH);
        cardDAO.addCard(yCard);
        Deck p1Deck = new Deck("OrgP1Deck", player1, GameType.YUGIOH);
        Deck p2Deck = new Deck("OrgP2Deck", player2, GameType.YUGIOH);
        deckDAO.createDeck(p1Deck);
        deckDAO.createDeck(p2Deck);
        deckDAO.addCardToDeck(p1Deck.getDeckId(), yCard.getCardId());
        deckDAO.addCardToDeck(p2Deck.getDeckId(), yCard.getCardId());

        Tournament approved = buildTournament("OrgApproved", TournamentStatus.PENDING, LocalDate.now().plusDays(2), LocalDate.now().plusDays(4));
        tournamentService.createTournament(organizer, approved);
        tournamentService.approveTournament(organizer, approved.getTournamentId());
        approvedTournamentId = approved.getTournamentId();

        Tournament ready = buildTournament("OrgReady", TournamentStatus.PENDING, LocalDate.now().plusDays(2), LocalDate.now().plusDays(3));
        tournamentService.createTournament(organizer, ready);
        tournamentService.approveTournament(organizer, ready.getTournamentId());
        registrationService.registerUserToTournament(player2, ready.getTournamentId(),
                new Registration(tournamentService.getTournamentById(ready.getTournamentId()), player2, p2Deck));
        ready = tournamentService.getTournamentById(ready.getTournamentId());
        ready.setStatus(TournamentStatus.READY);
        tournamentDAO.updateTournament(ready);
        readyTournamentId = ready.getTournamentId();

        Tournament rejected = buildTournament("OrgRejected", TournamentStatus.PENDING, LocalDate.now().plusDays(2), LocalDate.now().plusDays(4));
        tournamentService.createTournament(organizer, rejected);
        tournamentService.rejectTournament(organizer, rejected.getTournamentId());
        rejectedTournamentId = rejected.getTournamentId();

        Tournament ongoing = buildTournament("OrgOngoing", TournamentStatus.PENDING, LocalDate.now().minusDays(3), LocalDate.now().plusDays(3));
        tournamentService.createTournament(organizer, ongoing);
        ongoing = tournamentService.getTournamentById(ongoing.getTournamentId());
        ongoing.setStatus(TournamentStatus.ONGOING);
        tournamentDAO.updateTournament(ongoing);
        ongoingTournamentId = ongoing.getTournamentId();

        Tournament finished = buildTournament("OrgFinished", TournamentStatus.PENDING, LocalDate.now().minusDays(5), LocalDate.now().plusDays(3));
        tournamentService.createTournament(organizer, finished);
        finished = tournamentService.getTournamentById(finished.getTournamentId());
        finished.setStatus(TournamentStatus.FINISHED);
        tournamentDAO.updateTournament(finished);
        finishedTournamentId = finished.getTournamentId();

        registrationService.registerUserToTournament(player1, approvedTournamentId,
                new Registration(tournamentService.getTournamentById(approvedTournamentId), player1, p1Deck));
    }

    private static Tournament buildTournament(String name, TournamentStatus status, LocalDate deadline, LocalDate startDate) {
        Tournament t = new Tournament();
        t.setName(name);
        t.setDescription("desc");
        t.setOrganizer(organizer);
        t.setCapacity(8);
        t.setDeadline(deadline);
        t.setStartDate(startDate);
        t.setStatus(status);
        t.setGameType(GameType.YUGIOH);
        t.setRegistrations(new ArrayList<>());
        return t;
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

    private void loginAsOrganizer(GameType gt) {
        UserSession.getInstance().login(organizer);
        UserSession.getInstance().setGameType(gt);
    }

    @Test
    @Order(1)
    void test01_organizerMenuRequiresOrganizerRole() {
        UserSession.getInstance().login(player1);
        UserSession.getInstance().setGameType(GameType.YUGIOH);
        assertThrows(SecurityException.class, () -> buildController("5\n").organizerMenu());
    }

    @Test
    @Order(2)
    void test02_organizerMenuLogout() throws Exception {
        loginAsOrganizer(GameType.YUGIOH);
        buildController("5\n").organizerMenu();
        assertFalse(UserSession.isLoggedIn());
    }

    @Test
    @Order(3)
    void test03_organizerMenuChangeGame() throws Exception {
        loginAsOrganizer(GameType.MAGIC);
        buildController("4\n2\n5\n").organizerMenu();
        assertEquals(GameType.YUGIOH, UserSession.getInstance().getGameType());
    }

    @Test
    @Order(4)
    void test04_organizerAreaPersonaleChangeEmail() throws Exception {
        loginAsOrganizer(GameType.YUGIOH);
        buildController("3\n2\norg_ctrl_new@mail.com\n4\n5\n").organizerMenu();
        assertEquals("org_ctrl_new@mail.com", userDAO.getUserById(organizer.getUserId()).getEmail());
    }

    @Test
    @Order(5)
    void test05_organizerCreateTournamentFlow() throws Exception {
        loginAsOrganizer(GameType.YUGIOH);
        buildController("1\nOrgCreated\ndesc\n8\n31/12/2099\n01/01/2100\n5\n").organizerMenu();

        List<Tournament> tournaments = tournamentService.getTournamentsByOrganizer(organizer, organizer.getUserId());
        assertTrue(tournaments.stream().anyMatch(t -> t.getName().equals("OrgCreated")));
    }

    @Test
    @Order(6)
    void test06_gestisciPendingModifyTournament() throws Exception {
        Tournament pending = buildTournament("OrgPendingEdit", TournamentStatus.PENDING, LocalDate.now().plusDays(2), LocalDate.now().plusDays(4));
        tournamentService.createTournament(organizer, pending);

        loginAsOrganizer(GameType.YUGIOH);
        String cli = "2\n1\n1\n" + pending.getTournamentId() + "\nOrgPendingEdited\n\n\n\n\n\n3\n7\n5\n";
        buildController(cli).organizerMenu();

        assertEquals("OrgPendingEdited", tournamentService.getTournamentById(pending.getTournamentId()).getName());
    }

    @Test
    @Order(7)
    void test07_gestisciPendingDeleteTournament() throws Exception {
        Tournament pending = buildTournament("OrgPendingDelete", TournamentStatus.PENDING, LocalDate.now().plusDays(2), LocalDate.now().plusDays(4));
        tournamentService.createTournament(organizer, pending);

        loginAsOrganizer(GameType.YUGIOH);
        String cli = "2\n1\n2\n" + pending.getTournamentId() + "\nsi\n3\n7\n5\n";
        buildController(cli).organizerMenu();

        assertNull(tournamentService.getTournamentById(pending.getTournamentId()));
    }

    @Test
    @Order(8)
    void test08_gestisciApprovedRemoveParticipant() throws Exception {
        assertTrue(registrationDAO.isUserRegistered(approvedTournamentId, player1.getUserId()));
        loginAsOrganizer(GameType.YUGIOH);
        String cli = "2\n2\n1\n" + approvedTournamentId + "\n1\n" + player1.getUserId() + "\n2\n3\n7\n5\n";
        buildController(cli).organizerMenu();

        assertFalse(registrationDAO.isUserRegistered(approvedTournamentId, player1.getUserId()));

        UserSession.getInstance().login(player1);
        List<String> notifications = UserSession.getAndClearNotificationsForCurrentUser();
        assertTrue(notifications.stream().anyMatch(m -> m.contains("rimosso dal torneo ID " + approvedTournamentId)));
    }

    @Test
    @Order(9)
    void test09_gestisciReadyRemoveParticipant() throws Exception {
        assertTrue(registrationDAO.isUserRegistered(readyTournamentId, player2.getUserId()));
        loginAsOrganizer(GameType.YUGIOH);
        String cli = "2\n4\n1\n" + readyTournamentId + "\n1\n" + player2.getUserId() + "\n2\n2\n7\n5\n";
        buildController(cli).organizerMenu();
        assertFalse(registrationDAO.isUserRegistered(readyTournamentId, player2.getUserId()));
    }

    @Test
    @Order(10)
    void test10_gestisciRejectedDeleteTournament() throws Exception {
        loginAsOrganizer(GameType.YUGIOH);
        String cli = "2\n3\n1\n" + rejectedTournamentId + "\nsi\n2\n7\n5\n";
        buildController(cli).organizerMenu();
        assertNull(tournamentService.getTournamentById(rejectedTournamentId));
    }

    @Test
    @Order(11)
    void test11_gestisciOngoingAndFinishedViews() throws Exception {
        assertNotNull(tournamentService.getTournamentById(ongoingTournamentId));
        assertNotNull(tournamentService.getTournamentById(finishedTournamentId));

        loginAsOrganizer(GameType.YUGIOH);
        buildController("2\n5\n6\n7\n5\n").organizerMenu();

        assertEquals(TournamentStatus.ONGOING, tournamentService.getTournamentById(ongoingTournamentId).getStatus());
        assertEquals(TournamentStatus.FINISHED, tournamentService.getTournamentById(finishedTournamentId).getStatus());
    }
}
