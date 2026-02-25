package Controllers.test;

import Controllers.TournamentStatusController;
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
import ORM.dao.TournamentDAO;
import ORM.dao.UserDAO;
import Services.tournament.RegistrationService;
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
    private static RegistrationService registrationService;
    private static TournamentDAO tournamentDAO;
    private static TournamentStatusController controller;
    private static User organizer;

    private static int approvedDeadlineTodayId;
    private static int readyTodayStartId;
    private static int ongoingPastStartId;
    private static int pendingStableId;

    @BeforeAll
    static void init() throws Exception {
        connection = DatabaseConnection.getConnection();
        tournamentService = new TournamentService(connection);
        registrationService = new RegistrationService(connection);
        tournamentDAO = new TournamentDAO(connection);
        controller = new TournamentStatusController(tournamentService);

        truncateAll();
        seedBaseData();
    }

    @AfterAll
    static void cleanupAll() throws Exception {
        truncateAll();
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
        UserDAO userDAO = new UserDAO(connection);
        CardDAO cardDAO = new CardDAO(connection);
        DeckDAO deckDAO = new DeckDAO(connection);

        organizer = new User("org_status", "org_status@mail.com", "pass123", true, Role.ORGANIZER);
        User player = new User("player_status", "player_status@mail.com", "pass123", true, Role.PLAYER);
        userDAO.createUser(organizer);
        userDAO.createUser(player);

        Card c = new Card("StatusSeedCard", GameType.MAGIC);
        cardDAO.addCard(c);
        Deck d = new Deck("StatusSeedDeck", player, GameType.MAGIC);
        deckDAO.createDeck(d);
        deckDAO.addCardToDeck(d.getDeckId(), c.getCardId());

        Tournament approvedDeadlineToday = buildTournament("T_APPROVED_READY", TournamentStatus.PENDING,
                LocalDate.now(), LocalDate.now().plusDays(2));
        tournamentService.createTournament(organizer, approvedDeadlineToday);
        tournamentService.approveTournament(organizer, approvedDeadlineToday.getTournamentId());
        registrationService.registerUserToTournament(
                player,
                approvedDeadlineToday.getTournamentId(),
                new Registration(tournamentService.getTournamentById(approvedDeadlineToday.getTournamentId()), player, d)
        );
        approvedDeadlineTodayId = approvedDeadlineToday.getTournamentId();

        Tournament readyTodayStart = buildTournament("T_READY_ONGOING", TournamentStatus.PENDING,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(2));
        tournamentService.createTournament(organizer, readyTodayStart);
        readyTodayStart = tournamentService.getTournamentById(readyTodayStart.getTournamentId());
        readyTodayStart.setStatus(TournamentStatus.READY);
        readyTodayStart.setStartDate(LocalDate.now());
        tournamentDAO.updateTournament(readyTodayStart);
        readyTodayStartId = readyTodayStart.getTournamentId();

        Tournament ongoingPastStart = buildTournament("T_ONGOING_FINISHED", TournamentStatus.PENDING,
                LocalDate.now().minusDays(2), LocalDate.now().plusDays(2));
        tournamentService.createTournament(organizer, ongoingPastStart);
        ongoingPastStart = tournamentService.getTournamentById(ongoingPastStart.getTournamentId());
        ongoingPastStart.setStatus(TournamentStatus.ONGOING);
        ongoingPastStart.setStartDate(LocalDate.now().minusDays(1));
        tournamentDAO.updateTournament(ongoingPastStart);
        ongoingPastStartId = ongoingPastStart.getTournamentId();

        Tournament pendingStable = buildTournament("T_PENDING_STABLE", TournamentStatus.PENDING,
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        tournamentService.createTournament(organizer, pendingStable);
        pendingStableId = pendingStable.getTournamentId();
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
        t.setGameType(GameType.MAGIC);
        t.setRegistrations(new ArrayList<>());
        return t;
    }

    @Test
    @Order(1)
    void test01_syncMovesApprovedToReady() throws Exception {
        controller.syncTournamentStatuses();
        Tournament loaded = tournamentService.getTournamentById(approvedDeadlineTodayId);
        assertEquals(TournamentStatus.READY, loaded.getStatus());
    }

    @Test
    @Order(2)
    void test02_syncMovesReadyToOngoing() throws Exception {
        controller.syncTournamentStatuses();
        Tournament loaded = tournamentService.getTournamentById(readyTodayStartId);
        assertEquals(TournamentStatus.ONGOING, loaded.getStatus());
    }

    @Test
    @Order(3)
    void test03_syncMovesOngoingToFinished() throws Exception {
        controller.syncTournamentStatuses();
        Tournament loaded = tournamentService.getTournamentById(ongoingPastStartId);
        assertEquals(TournamentStatus.FINISHED, loaded.getStatus());
    }

    @Test
    @Order(4)
    void test04_syncKeepsPendingUnchanged() throws Exception {
        controller.syncTournamentStatuses();
        Tournament loaded = tournamentService.getTournamentById(pendingStableId);
        assertEquals(TournamentStatus.PENDING, loaded.getStatus());
    }
}
