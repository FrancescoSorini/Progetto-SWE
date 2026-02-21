package DomainModel.tests;

import DomainModel.GameType;
import DomainModel.card.Deck;
import DomainModel.tournament.Registration;
import DomainModel.tournament.Tournament;
import DomainModel.user.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RegistrationTest {

    @Test
    void constructorSetsFieldsAndDate() {
        Tournament tournament = new Tournament("Cup", GameType.MAGIC);
        User user = new User("player");
        Deck deck = new Deck("D1", user, GameType.MAGIC);

        Registration registration = new Registration(tournament, user, deck);

        assertEquals(tournament, registration.getTournament());
        assertEquals(user, registration.getUser());
        assertEquals(deck, registration.getRegDeck());
        assertNotNull(registration.getRegistrationDate());
    }

    @Test
    void settersUpdateFields() {
        Registration registration = new Registration(new Tournament("A"), new User("u1"), null);
        Tournament tournament = new Tournament("B");
        User user = new User("u2");
        LocalDateTime date = LocalDateTime.now().minusDays(1);

        registration.setTournament(tournament);
        registration.setUser(user);
        registration.setRegistrationDate(date);

        assertEquals(tournament, registration.getTournament());
        assertEquals(user, registration.getUser());
        assertEquals(date, registration.getRegistrationDate());
    }
}
