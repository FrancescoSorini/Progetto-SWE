package BusinessLogic.session;

import DomainModel.user.User;
import DomainModel.GameType;

public class UserSession {

    private static UserSession instance;
    private static User currentUser;
    private GameType currentGameType;   // <-- NUOVO CAMPO

    private UserSession() {}

    // Singleton
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // ---- Gestione sessione ----

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static int getUserId() {
        return (currentUser != null) ? currentUser.getUserId() : -1;
    }

    public String getUsername() {
        return (currentUser != null) ? currentUser.getUsername() : null;
    }

    public String getEmail() {
        return (currentUser != null) ? currentUser.getEmail() : null;
    }

    // ---- Gestione GameType ----
    public void setGameType(GameType gameType) {
        this.currentGameType = gameType;
    }

    public GameType getGameType() {
        return currentGameType;
    }

    public boolean hasSelectedGame() {
        return currentGameType != null;
    }

    // ---- Controlli ruolo ----
    public static boolean isAdmin() {
        return isLoggedIn() && currentUser.getRole().name().equals("ADMIN");
    }

    public boolean isOrganizer() {
        return isLoggedIn() && currentUser.getRole().name().equals("ORGANIZER");
    }

    public boolean isPlayer() {
        return isLoggedIn() && currentUser.getRole().name().equals("PLAYER");
    }
}

