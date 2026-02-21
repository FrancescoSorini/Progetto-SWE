package DomainModel.tests;

import DomainModel.tournament.Tournament;
import DomainModel.tournament.observer.TournamentObserver;
import DomainModel.tournament.observer.TournamentSubject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TournamentSubjectTest {

    @Test
    void notifyObserversCallsAllRegisteredObservers() {
        TournamentSubject subject = new TournamentSubject();
        CounterObserver a = new CounterObserver();
        CounterObserver b = new CounterObserver();

        subject.addObserver(a);
        subject.addObserver(b);
        subject.notifyObservers(new Tournament("Cup"));

        assertEquals(1, a.calls);
        assertEquals(1, b.calls);
    }

    @Test
    void removeObserverStopsNotifications() {
        TournamentSubject subject = new TournamentSubject();
        CounterObserver a = new CounterObserver();

        subject.addObserver(a);
        subject.removeObserver(a);
        subject.notifyObservers(new Tournament("Cup"));

        assertEquals(0, a.calls);
    }

    private static class CounterObserver implements TournamentObserver {
        int calls;

        @Override
        public void update(Tournament tournament) {
            calls++;
        }
    }
}
