package DomainModel.tests;

import DomainModel.tournament.Tournament;
import DomainModel.tournament.TournamentStatus;
import DomainModel.tournament.observer.UserObserver;
import DomainModel.user.Role;
import DomainModel.user.User;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserObserverTest {

    @Test
    void updatePrintsNotificationMessage() {
        User user = new User("alice", "a@a.it", "pwd", true, Role.PLAYER);
        Tournament tournament = new Tournament("Spring Cup");
        tournament.setStatus(TournamentStatus.APPROVED);
        UserObserver observer = new UserObserver(user);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(output));
            observer.update(tournament);
        } finally {
            System.setOut(original);
        }

        String text = output.toString();
        assertTrue(text.contains("alice"));
        assertTrue(text.contains("Spring Cup"));
        assertTrue(text.contains("APPROVED"));
    }
}
