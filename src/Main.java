/*
* INTERFACCIA CLI DINAMICA PER PLAYER
* Esistono già due utenti admin e organizer, per i player si usa questa interfaccia
*
* */

import BusinessLogic.session.UserSession;
import BusinessLogic.user.UserService;
import DomainModel.GameType;
import DomainModel.card.Card;
import static DomainModel.card.Card.printCards;
import DomainModel.user.Role;
import DomainModel.user.User;
import static DomainModel.user.User.printUsers;
import ORM.connection.DatabaseConnection;
import BusinessLogic.card.CardService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;


public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final UserService userService = new UserService(DatabaseConnection.getConnection());
    private static final CardService cardService = new CardService(DatabaseConnection.getConnection());

    public static void main(String[] args) {

        boolean running = true;

        while (running) {
            showWelcomeMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> handleLogin();
                case "2" -> handleRegistration();
                case "3" -> {
                    System.out.println("Arrivederci!");
                    running = false;
                }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private static void showWelcomeMenu() {
        System.out.println("\n==============================");
        System.out.println("     TCG MANAGER");
        System.out.println("==============================");
        System.out.println("1) Login");
        System.out.println("2) Registrazione (Player)");
        System.out.println("3) Esci");
        System.out.print("Scelta: ");
    }

    private static void handleLogin() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            User user = userService.login(username, password);

            UserSession.getInstance().login(user);

            System.out.println("Login effettuato con successo!");
            System.out.println("Benvenuto " + user.getUsername() +
                    " (" + user.getRole() + ")");

            // Menu dinamico in base al ruolo
            UserSession.getInstance().login(user);

            selectGame();
            showRoleMenu();

        } catch (Exception e) {
            System.out.println("Errore durante il login: " + e.getMessage());
        }
    }

    private static void handleRegistration() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine();

            System.out.print("Email: ");
            String email = scanner.nextLine();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            User user = userService.registerUser(username, email, password);

            System.out.println("Registrazione completata!");

            // Menu dinamico in base al ruolo
            UserSession.getInstance().login(user);

            selectGame();
            showRoleMenu();

        } catch (Exception e) {
            System.out.println("Errore durante la registrazione: " + e.getMessage());
        }
    }

    // ------------------ SELEZIONE DEL GIOCO ------------------
    private static void selectGame() {
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

    // ------------------ SELEZIONE DEL MENU IN BASE ALL'UTENTE ------------------
    private static void showRoleMenu() throws SQLException {
        Role role = UserSession.getInstance().getCurrentUser().getRole();

        switch (role) {
            case PLAYER -> playerMenu();
            case ADMIN -> adminMenu();
            case ORGANIZER -> organizerMenu();
        }
    }

    // ------------------ MENU BASE PLAYER ------------------
    private static void playerMenu() throws SQLException {
        boolean running = true;

        while (running) {
            System.out.println("\n--- MENU PLAYER ---");
            System.out.println("1) Gestione Mazzi");
            System.out.println("2) Catalogo Carte");
            System.out.println("3) Check Tornei");
            System.out.println("4) Logout");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> System.out.println(">> Gestione Mazzi (da implementare)");
                case "2" -> catalogoCarteMenu();
                case "3" -> System.out.println(">> Check Tornei (da implementare)");
                case "4" -> {
                    UserSession.getInstance().logout();
                    running = false;
                }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    // ------------------ MENU BASE ADMIN ------------------
    private static void adminMenu() throws SQLException {
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

    // ------------------ MENU BASE ORGANIZER ------------------
    private static void organizerMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n--- MENU ORGANIZER ---");
            System.out.println("1) Crea Torneo");
            System.out.println("2) Gestisci Torneo");
            System.out.println("3) Logout");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> System.out.println(">> Crea Torneo (da implementare)");
                case "2" -> System.out.println(">> Gestisci Torneo (da implementare)");
                case "3" -> {
                    UserSession.getInstance().logout();
                    running = false;
                }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    // ------------------ SOTTOMENU 2 PLAYER (CATALOGO CARTE) ------------------
    private static void catalogoCarteMenu() throws SQLException {
        boolean running = true;

        while (running) {
            System.out.println("\n--- CATALOGO CARTE ---");
            System.out.println("1) Visualizza tutte le carte");
            System.out.println("2) Cerca carta per nome");
            System.out.println("3) Indietro");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine();
            GameType gameType = UserSession.getInstance().getGameType();

            switch (choice) {

                case "1" -> {
                    List<Card> cards = cardService.getCardsByGameType(gameType);
                    printCards(cards);
                }

                case "2" -> {
                    System.out.print("Inserisci nome (anche parziale): ");
                    String keyword = scanner.nextLine();

                    List<Card> results =
                            cardService.searchCardsByName(gameType, keyword);

                    printCards(results);
                }

                case "3" -> running = false;

                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    // ------------------ SOTTOMENU 2 ADMIN (LISTA UTENTI) ------------------
    private static void gestioneUtentiMenu() throws SQLException {

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
                    List<User> users = userService.getAllUsers();
                    printUsers(users);
                }

                case "2" -> {
                    System.out.print("Inserisci nome (anche parziale): ");
                    String keyword = scanner.nextLine();

                    List<User> results =
                            userService.searchUsersByName(keyword);

                    printUsers(results);
                }

                case "3" -> running = false;

                default -> System.out.println("Scelta non valida.");
            }
        }
    }


/*
Visualizza utenti
-
-
-
-
-

1) Vuoi eliminare un utente? getUserById per recuperare l'utente da eliminare
2) Vuoi modificare l'abilitazione di un utente? getUserById
3) Vuoi modificare il ruolo di un utente? getUserById


Cerca utente per nome (Fuzzy search)
-
-
-
1) Vuoi eliminare un utente? getUserById per recuperare l'utente da eliminare
2) Vuoi modificare l'abilitazione di un utente? getUserById
3) Vuoi modificare il ruolo di un utente? getUserById
 */



}
