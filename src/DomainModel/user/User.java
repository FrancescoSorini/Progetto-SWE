package DomainModel.user;
import DomainModel.card.Deck;
import DomainModel.tournament.Registration;

import java.util.List;

public class User {
    private int userId;
    private String username;
    private String email;
    private String password;
    private boolean enabled;
    private Role role;
    private List<Deck> decks;              // 1-N
    private List<Registration> registrations; // N-M tramite Registrations

    public User(String username){
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}

/*
Usage example in a main.java file:
import DomainModel.tournament.*;
import DomainModel.user.*;
import DomainModel.tournament.observer.*;

public class Main {
    public static void main(String[] args) {
        Tournament t = new Tournament("Spring Duel Cup");

        User u1 = new User("Francesco");
        User u2 = new User("Luca");

        t.addObserver(new UserObserver(u1));
        t.addObserver(new UserObserver(u2));

        t.setStatus(TournamentStatus.APPROVED);
        t.setStatus(TournamentStatus.CLOSED);
    }
}

 */
