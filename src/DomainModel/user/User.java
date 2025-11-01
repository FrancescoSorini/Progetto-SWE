package DomainModel.user;
import DomainModel.card.Deck;
import DomainModel.tournament.Registration;

import java.util.List;

public class User {
    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private boolean enabled;
    private Role role;
    private List<Deck> decks;              // 1-N
    private List<Registration> registrations; // N-M tramite Registrations
}
