package BusinessLogic.session;

import DomainModel.user.User;

public class UserSession {

    private static UserSession instance;
    private static User currentUser;

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

