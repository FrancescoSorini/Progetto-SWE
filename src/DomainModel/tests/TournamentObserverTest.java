package DomainModel.tests;

import DomainModel.tournament.Tournament;
import DomainModel.tournament.observer.TournamentObserver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TournamentObserverTest {

    @Test
    void observerImplementationCanReceiveUpdate() {
        TestObserver observer = new TestObserver();
        observer.update(new Tournament("Cup"));
        assertTrue(observer.called);
    }

    private static class TestObserver implements TournamentObserver {
        boolean called;

        @Override
        public void update(Tournament tournament) {
            called = true;
        }
    }
}
