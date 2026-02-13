package Controllers.session;

import DomainModel.GameType;
import DomainModel.user.User;

public class UserSession {

    private static UserSession instance;
    private static User currentUser;
    private GameType currentGameType;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void login(User user) {
        currentUser = user;
    }

    public void logout() {
        currentUser = null;
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

    public void setGameType(GameType gameType) {
        currentGameType = gameType;
    }

    public GameType getGameType() {
        return currentGameType;
    }

    public boolean hasSelectedGame() {
        return currentGameType != null;
    }

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
