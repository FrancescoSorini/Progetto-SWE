package DomainModel.tournament;

import DomainModel.card.Deck;
import DomainModel.user.User;
import java.time.LocalDateTime;

public class Registration {
    private Tournament tournament;
    private User user;
    private LocalDateTime registrationDate;
    private Deck regDeck;

    // COSTRUTTORE
    public Registration(Tournament t, User u, Deck regDeck) {
        this.tournament = t;
        this.user = u;
        this.regDeck = regDeck;
        this.registrationDate = LocalDateTime.now();
    }

    // GETTER
    public Tournament getTournament() { return tournament; }
    public User getUser() { return user; }
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public Deck getRegDeck() { return regDeck; }

    // SETTER
    public void setTournament(Tournament tournament) { this.tournament = tournament; }
    public void setUser(User user) { this.user = user; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

}
