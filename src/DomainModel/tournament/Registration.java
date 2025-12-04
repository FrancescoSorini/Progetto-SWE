package DomainModel.tournament;

import DomainModel.card.Deck;
import DomainModel.user.User;
import java.time.LocalDateTime;

public class Registration {
    private Tournament tournament;
    private User user;
    private LocalDateTime registrationDate;
    private int regDeckId;

    // COSTRUTTORE
    public Registration(Tournament t, User u, int regDeck) {
        this.tournament = t;
        this.user = u;
        this.regDeckId = regDeck;
        this.registrationDate = LocalDateTime.now();
    }

    // GETTER
    public Tournament getTournament() { return tournament; }
    public User getUser() { return user; }
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public int getRegDeckId() { return regDeckId; }

    // SETTER
    public void setTournament(Tournament tournament) { this.tournament = tournament; }
    public void setUser(User user) { this.user = user; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
    public void setRegDeckId(int regDeckId) { this.regDeckId = regDeckId; }

}
