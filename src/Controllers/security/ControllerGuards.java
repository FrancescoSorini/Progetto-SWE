package Controllers.security;

import Controllers.session.UserSession;
import DomainModel.user.Role;
import DomainModel.user.User;

public final class ControllerGuards {

    private ControllerGuards() {}

    public static User requireLoggedIn() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("Devi effettuare il login.");
        }
        return currentUser;
    }

    public static User requireRole(Role expectedRole) {
        User currentUser = requireLoggedIn();
        if (currentUser.getRole() != expectedRole) {
            throw new SecurityException("Accesso negato per il ruolo corrente.");
        }
        return currentUser;
    }
}
