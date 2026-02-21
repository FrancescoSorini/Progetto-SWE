package DomainModel.tests;

import DomainModel.tournament.TournamentStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TournamentStatusTest {

    @Test
    void enumContainsExpectedValues() {
        assertEquals(TournamentStatus.PENDING, TournamentStatus.valueOf("PENDING"));
        assertEquals(TournamentStatus.APPROVED, TournamentStatus.valueOf("APPROVED"));
        assertEquals(TournamentStatus.REJECTED, TournamentStatus.valueOf("REJECTED"));
        assertEquals(TournamentStatus.READY, TournamentStatus.valueOf("READY"));
        assertEquals(TournamentStatus.ONGOING, TournamentStatus.valueOf("ONGOING"));
        assertEquals(TournamentStatus.FINISHED, TournamentStatus.valueOf("FINISHED"));
    }
}
