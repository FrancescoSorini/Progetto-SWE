package DomainModel.user;
import DomainModel.card.Deck;
import DomainModel.tournament.Registration;

import java.util.List;

public class User {
    private int userId;
    private String username;
    private String email;
    private String pwd;
    private boolean enabled;
    private Role role;
    private List<Deck> decks;// 1-N
    private List<Registration> registrations; // N-M tramite Registrations


    public User(String username){
        this.username = username;
    }

    // GETTER
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return pwd; }
    public Role getRole() { return role; }
    public int getRoleId() { return role.getRoleId();}


    // SETTER
    public void setUserId(int userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.pwd = password; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setRole(Role role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
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
