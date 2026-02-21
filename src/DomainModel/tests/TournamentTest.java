package DomainModel.tests;

import DomainModel.GameType;
import DomainModel.tournament.Registration;
import DomainModel.tournament.Tournament;
import DomainModel.tournament.TournamentStatus;
import DomainModel.tournament.observer.TournamentObserver;
import DomainModel.user.Role;
import DomainModel.user.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TournamentTest {

    @Test
    void constructorsSetPendingStatus() {
        Tournament a = new Tournament("T1");
        Tournament b = new Tournament("T2", GameType.MAGIC);

        assertEquals(TournamentStatus.PENDING, a.getStatus());
        assertEquals(TournamentStatus.PENDING, b.getStatus());
    }

    @Test
    void registrationAndCapacityChecksWork() {
        User organizer = new User("org", "o@o.it", "pwd", true, Role.ORGANIZER);
        Tournament t = new Tournament("Cup", "desc", organizer, 1,
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(3), GameType.MAGIC);
        t.setRegistrations(new ArrayList<>());

        assertFalse(t.isFull());
        Registration r = new Registration(t, new User("p"), null);
        t.addRegistration(r);
        assertTrue(t.isFull());
        t.removeRegistration(r);
        assertFalse(t.isFull());
    }

    @Test
    void dateChecksWork() {
        Tournament t = new Tournament("Cup", GameType.YUGIOH);
        t.setRegistrations(new ArrayList<>());

        t.setDeadline(LocalDate.now().plusDays(1));
        assertTrue(t.isRegistrationOpen());
        t.setDeadline(LocalDate.now().minusDays(1));
        assertFalse(t.isRegistrationOpen());

        t.setStartDate(LocalDate.now().minusDays(1));
        assertTrue(t.isStarted());
        t.setStartDate(LocalDate.now().plusDays(1));
        assertFalse(t.isStarted());
    }

    @Test
    void setStatusNotifiesObservers() {
        Tournament t = new Tournament("Cup", GameType.POKEMON);
        TestObserver observer = new TestObserver();
        t.addObserver(observer);

        t.setStatus(TournamentStatus.APPROVED);

        assertTrue(observer.called);
        assertEquals(TournamentStatus.APPROVED, observer.lastTournament.getStatus());
    }

    private static class TestObserver implements TournamentObserver {
        boolean called;
        Tournament lastTournament;

        @Override
        public void update(Tournament tournament) {
            called = true;
            lastTournament = tournament;
        }
    }
}
