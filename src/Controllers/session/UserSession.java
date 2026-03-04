package Controllers.session;

import DomainModel.GameType;
import DomainModel.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserSession {

    private static UserSession instance;
    private static User currentUser;
    private GameType currentGameType;
    private static final Map<Integer, List<SessionNotification>> notificationsByUserId = new HashMap<>();

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

    public static void addNotificationForUser(int userId, String message) {
        addNotificationForUser(userId, message, null);
    }

    public static List<String> getAndClearNotificationsForCurrentUser() {
        return getAndClearNotificationsForCurrentUser(null);
    }

    public static void addNotificationForUser(int userId, String message, GameType gameType) {
        notificationsByUserId
                .computeIfAbsent(userId, key -> new ArrayList<>())
                .add(new SessionNotification(message, gameType));
    }

    public static List<String> getAndClearNotificationsForCurrentUser(GameType selectedGameType) {
        if (!isLoggedIn()) {
            return List.of();
        }

        int userId = currentUser.getUserId();
        List<SessionNotification> notifications = notificationsByUserId.getOrDefault(userId, new ArrayList<>());

        List<SessionNotification> matching = notifications.stream()
                .filter(n -> selectedGameType == null || n.gameType == null || n.gameType == selectedGameType)
                .toList();

        List<SessionNotification> notMatching = notifications.stream()
                .filter(n -> !(selectedGameType == null || n.gameType == null || n.gameType == selectedGameType))
                .toList();

        if (notMatching.isEmpty()) {
            notificationsByUserId.remove(userId);
        } else {
            notificationsByUserId.put(userId, new ArrayList<>(notMatching));
        }

        return matching.stream()
                .map(n -> n.message)
                .toList();
    }

    private static class SessionNotification {
        private final String message;
        private final GameType gameType;

        private SessionNotification(String message, GameType gameType) {
            this.message = message;
            this.gameType = gameType;
        }
    }
}
