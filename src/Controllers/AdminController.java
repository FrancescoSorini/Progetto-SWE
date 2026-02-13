package Controllers;

import BusinessLogic.user.UserService;
import Controllers.security.ControllerGuards;
import Controllers.session.UserSession;
import DomainModel.user.User;
import DomainModel.user.Role;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import static DomainModel.user.User.printUsers;

public class AdminController {

    private final Scanner scanner;
    private final UserService userService;

    public AdminController(Scanner scanner, UserService userService) {
        this.scanner = scanner;
        this.userService = userService;
    }

    public void adminMenu() throws SQLException {
        ControllerGuards.requireRole(Role.ADMIN);
        boolean running = true;

        while (running) {
            System.out.println("\n--- MENU ADMIN ---");
            System.out.println("1) Gestione Carte");
            System.out.println("2) Gestione Utenti");
            System.out.println("3) Approvazione Tornei");
            System.out.println("4) Logout");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> System.out.println(">> Gestione Carte (da implementare)");
                case "2" -> gestioneUtentiMenu();
                case "3" -> System.out.println(">> Approvazione Tornei (da implementare)");
                case "4" -> {
                    UserSession.getInstance().logout();
                    running = false;
                }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    public void gestioneUtentiMenu() throws SQLException {
        ControllerGuards.requireRole(Role.ADMIN);
        boolean running = true;

        while (running) {
            System.out.println("\n--- GESTIONE UTENTI ---");
            System.out.println("1) Visualizza tutti gli utenti");
            System.out.println("2) Cerca utente per nome");
            System.out.println("3) Indietro");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> {
                    User caller = ControllerGuards.requireRole(Role.ADMIN);
                    List<User> users = userService.getAllUsers(caller);
                    printUsers(users);
                }
                case "2" -> {
                    System.out.print("Inserisci nome (anche parziale): ");
                    String keyword = scanner.nextLine();

                    List<User> results = userService.searchUsersByName(keyword);
                    printUsers(results);
                }
                case "3" -> running = false;
                default -> System.out.println("Scelta non valida.");
            }
        }
    }
}
