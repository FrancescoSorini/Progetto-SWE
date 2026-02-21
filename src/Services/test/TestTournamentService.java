package Services.test;

import DomainModel.GameType;
import DomainModel.tournament.Tournament;
import DomainModel.tournament.TournamentStatus;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import ORM.dao.TournamentDAO;
import ORM.dao.UserDAO;
import Services.tournament.TournamentService;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestTournamentService {

    private static Connection connection;
    private static TournamentService tournamentService;
    private static UserDAO userDAO;
    private static TournamentDAO tournamentDAO;
    private static User organizer;
    private static User player;

    @BeforeAll
    static void init() throws Exception {
        connection = DatabaseConnection.getConnection();
        tournamentService = new TournamentService(connection);
        userDAO = new UserDAO(connection);
        tournamentDAO = new TournamentDAO(connection);
    }

    @BeforeEach
    void resetBeforeEach() throws Exception {
        truncateAll();
        organizer = new User("org", "org@mail.com", "pass123", true, Role.ORGANIZER);
        player = new User("player", "p@mail.com", "pass123", true, Role.PLAYER);
        userDAO.createUser(organizer);
        userDAO.createUser(player);
    }

    @AfterEach
    void resetAfterEach() throws Exception {
        truncateAll();
    }

    @AfterAll
    static void closeAll() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void truncateAll() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                TRUNCATE TABLE registrations,
                               tournaments,
                               decks_cards,
                               decks,
                               cards,
                               users
                RESTART IDENTITY CASCADE
            """);
        }
    }

    private Tournament validTournament(String name, TournamentStatus status) {
        Tournament t = new Tournament();
        t.setName(name);
        t.setDescription("desc");
        t.setOrganizer(organizer);
        t.setCapacity(8);
        t.setDeadline(LocalDate.now().plusDays(3));
        t.setStartDate(LocalDate.now().plusDays(5));
        t.setStatus(status);
        t.setGameType(GameType.YUGIOH);
        t.setRegistrations(new ArrayList<>());
        return t;
    }

    @Test
    @Order(1)
    void test01_createTournament_success() throws Exception {
        Tournament t = validTournament("Cup1", TournamentStatus.PENDING);
        tournamentService.createTournament(organizer, t);

        Tournament loaded = tournamentService.getTournamentById(t.getTournamentId());
        assertNotNull(loaded);
        assertEquals(TournamentStatus.PENDING, loaded.getStatus());
        assertEquals(organizer.getUserId(), loaded.getOrganizer().getUserId());
    }

    @Test
    @Order(2)
    void test02_createTournament_validationAndCaller() {
        Tournament t = validTournament("Cup", TournamentStatus.PENDING);
        assertThrows(SecurityException.class, () -> tournamentService.createTournament(null, t));

        Tournament badName = validTournament(" ", TournamentStatus.PENDING);
        assertThrows(IllegalArgumentException.class, () -> tournamentService.createTournament(organizer, badName));

        Tournament badCap = validTournament("X", TournamentStatus.PENDING);
        badCap.setCapacity(1);
        assertThrows(IllegalArgumentException.class, () -> tournamentService.createTournament(organizer, badCap));

        Tournament badDates = validTournament("X", TournamentStatus.PENDING);
        badDates.setStartDate(LocalDate.now().plusDays(1));
        badDates.setDeadline(LocalDate.now().plusDays(2));
        assertThrows(IllegalArgumentException.class, () -> tournamentService.createTournament(organizer, badDates));
    }

    @Test
    @Order(3)
    void test03_updateTournament_successAndConstraints() throws Exception {
        Tournament t = validTournament("Cup1", TournamentStatus.PENDING);
        tournamentService.createTournament(organizer, t);

        t.setName("Cup1Updated");
        tournamentService.updateTournament(organizer, t);
        assertEquals("Cup1Updated", tournamentService.getTournamentById(t.getTournamentId()).getName());

        Tournament loaded = tournamentService.getTournamentById(t.getTournamentId());
        loaded.setStatus(TournamentStatus.APPROVED);
        tournamentService.approveTournament(organizer, loaded.getTournamentId());
        loaded = tournamentService.getTournamentById(t.getTournamentId());
        loaded.setName("ShouldFail");
        Tournament finalLoaded = loaded;
        assertThrows(IllegalStateException.class, () -> tournamentService.updateTournament(organizer, finalLoaded));

        Tournament notExisting = validTournament("Nope", TournamentStatus.PENDING);
        notExisting.setTournamentId(999);
        assertThrows(IllegalArgumentException.class, () -> tournamentService.updateTournament(organizer, notExisting));
    }

    @Test
    @Order(4)
    void test04_deleteTournament() throws Exception {
        Tournament t = validTournament("CupDel", TournamentStatus.PENDING);
        tournamentService.createTournament(organizer, t);
        tournamentService.deleteTournament(organizer, t.getTournamentId());
        assertNull(tournamentService.getTournamentById(t.getTournamentId()));
        assertThrows(IllegalArgumentException.class, () -> tournamentService.deleteTournament(organizer, 999));
    }

    @Test
    @Order(5)
    void test05_approveRejectTournament() throws Exception {
        Tournament t1 = validTournament("CupA", TournamentStatus.PENDING);
        tournamentService.createTournament(organizer, t1);
        tournamentService.approveTournament(organizer, t1.getTournamentId());
        assertEquals(TournamentStatus.APPROVED, tournamentService.getTournamentById(t1.getTournamentId()).getStatus());

        Tournament t2 = validTournament("CupR", TournamentStatus.PENDING);
        tournamentService.createTournament(organizer, t2);
        tournamentService.rejectTournament(organizer, t2.getTournamentId());
        assertEquals(TournamentStatus.REJECTED, tournamentService.getTournamentById(t2.getTournamentId()).getStatus());
    }

    @Test
    @Order(6)
    void test06_approveRejectOnlyPending() throws Exception {
        Tournament t = validTournament("Cup", TournamentStatus.PENDING);
        tournamentService.createTournament(organizer, t);
        tournamentService.approveTournament(organizer, t.getTournamentId());

        assertThrows(IllegalStateException.class, () -> tournamentService.approveTournament(organizer, t.getTournamentId()));
        assertThrows(IllegalStateException.class, () -> tournamentService.rejectTournament(organizer, t.getTournamentId()));
    }

    @Test
    @Order(7)
    void test07_updateTournamentStatusesAutomatically() throws Exception {
        Tournament toReadyByDeadline = validTournament("DeadlineReady", TournamentStatus.PENDING);
        toReadyByDeadline.setDeadline(LocalDate.now());
        toReadyByDeadline.setStartDate(LocalDate.now().plusDays(2));
        tournamentService.createTournament(organizer, toReadyByDeadline);
        tournamentService.approveTournament(organizer, toReadyByDeadline.getTournamentId());

        Tournament toOngoing = validTournament("ToOngoing", TournamentStatus.PENDING);
        toOngoing.setStartDate(LocalDate.now());
        toOngoing.setDeadline(LocalDate.now().minusDays(2));
        tournamentService.createTournament(organizer, toOngoing);
        toOngoing = tournamentService.getTournamentById(toOngoing.getTournamentId());
        toOngoing.setStatus(TournamentStatus.READY);
        tournamentDAO.updateTournament(toOngoing);

        /*
        questo test è commentato perchè c' è un controllo nel TournamentService che impedisce di creare tornei che
        hanno startDate precedente alla data odierna
         */

        /*Tournament toFinished = validTournament("ToFinished", TournamentStatus.PENDING);
        toFinished.setStartDate(LocalDate.now().minusDays(1));
        toFinished.setDeadline(LocalDate.now().minusDays(3));
        tournamentService.createTournament(organizer, toFinished);
        toFinished = tournamentService.getTournamentById(toFinished.getTournamentId());
        toFinished.setStatus(TournamentStatus.ONGOING);
        tournamentDAO.updateTournament(toFinished);*/

        tournamentService.updateTournamentStatusesAutomatically();

        assertEquals(TournamentStatus.READY, tournamentService.getTournamentById(toReadyByDeadline.getTournamentId()).getStatus());
        assertEquals(TournamentStatus.ONGOING, tournamentService.getTournamentById(toOngoing.getTournamentId()).getStatus());

       // assertEquals(TournamentStatus.FINISHED, tournamentService.getTournamentById(toFinished.getTournamentId()).getStatus());
    }

    @Test
    @Order(8)
    void test08_gettersByOrganizerGameTypeStatus() throws Exception {
        Tournament a = validTournament("A", TournamentStatus.PENDING);
        a.setGameType(GameType.MAGIC);
        tournamentService.createTournament(organizer, a);

        Tournament b = validTournament("B", TournamentStatus.PENDING);
        b.setGameType(GameType.YUGIOH);
        tournamentService.createTournament(organizer, b);

        tournamentService.approveTournament(organizer, b.getTournamentId());

        List<Tournament> byOrganizer = tournamentService.getTournamentsByOrganizer(organizer, organizer.getUserId());
        List<Tournament> byMagic = tournamentService.getTournamentsByGameType(GameType.MAGIC);
        List<Tournament> byApproved = tournamentService.getTournamentsByStatus(TournamentStatus.APPROVED);
        List<Tournament> all = tournamentService.getAllTournaments(organizer);

        assertEquals(2, byOrganizer.size());
        assertEquals(1, byMagic.size());
        assertEquals(1, byApproved.size());
        assertEquals(2, all.size());
    }
}
