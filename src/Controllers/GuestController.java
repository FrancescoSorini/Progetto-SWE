package Controllers;

import Controllers.session.UserSession;
import BusinessLogic.user.UserService;
import DomainModel.GameType;
import DomainModel.user.User;

import java.sql.SQLException;
import java.util.Scanner;

public class GuestController {

    private final Scanner scanner;
    private final UserService userService;
    private final RoleMenuController roleMenuController;

    public GuestController(Scanner scanner, UserService userService, RoleMenuController roleMenuController) {
        this.scanner = scanner;
        this.userService = userService;
        this.roleMenuController = roleMenuController;
    }

    public boolean showWelcomeMenuAndHandleSelection() {
        showWelcomeMenu();
        String choice = scanner.nextLine();

        try {
            switch (choice) {
                case "1" -> handleLogin();
                case "2" -> handleRegistration();
                case "3" -> {
                    System.out.println("Arrivederci!");
                    return false;
                }
                default -> System.out.println("Scelta non valida.");
            }
        } catch (Exception e) {
            System.out.println("Errore: " + e.getMessage());
        }

        return true;
    }

    private void showWelcomeMenu() {
        System.out.println("\n==============================");
        System.out.println("     TCG MANAGER");
        System.out.println("==============================");
        System.out.println("1) Login");
        System.out.println("2) Registrazione (Player)");
        System.out.println("3) Esci");
        System.out.print("Scelta: ");
    }

    private void handleLogin() throws SQLException {
        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userService.login(username, password);
        if (user == null) {
            throw new IllegalArgumentException("Credenziali non valide.");
        }

        UserSession.getInstance().login(user);

        System.out.println("Login effettuato con successo!");
        System.out.println("Benvenuto " + user.getUsername() + " (" + user.getRole() + ")");

        selectGame();
        roleMenuController.showRoleMenu();
    }

    private void handleRegistration() throws SQLException {
        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userService.registerUser(username, email, password);
        System.out.println("Registrazione completata!");

        UserSession.getInstance().login(user);

        selectGame();
        roleMenuController.showRoleMenu();
    }

    private void selectGame() {
        System.out.println("\nScegli il gioco:");
        System.out.println("1) Magic");
        System.out.println("2) Yu-Gi-Oh");
        System.out.println("3) Pokemon");
        System.out.print("Scelta: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1" -> UserSession.getInstance().setGameType(GameType.MAGIC);
            case "2" -> UserSession.getInstance().setGameType(GameType.YUGIOH);
            case "3" -> UserSession.getInstance().setGameType(GameType.POKEMON);
            default -> {
                System.out.println("Scelta non valida.");
                selectGame();
            }
        }
    }
}
