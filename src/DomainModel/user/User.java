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

    // COSTRUTTORI
    public User() {}

    public User(String username){
        this.username = username;
    }

    public User(String username, String email, String pwd, boolean enabled, Role role) {
        this.username = username;
        this.email = email;
        this.pwd = pwd;
        this.enabled = enabled;
        this.role = role;
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

    public static void printUsers(List<User> users) {

        if (users.isEmpty()) {
            System.out.println("Nessun utente trovato.");
            return;
        }

        for (User user : users) {
            System.out.println("----------------------");
            System.out.println("ID: " + user.getUserId());
            System.out.println("Username: " + user.getUsername());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Ruolo: " + user.getRole());
            System.out.println("Stato: " + (user.isEnabled() ? "Abilitato" : "Bannato"));
        }
    }

}

