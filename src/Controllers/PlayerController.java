package Controllers;

import BusinessLogic.card.CardService;
import Controllers.security.ControllerGuards;
import Controllers.session.UserSession;
import DomainModel.GameType;
import DomainModel.card.Card;
import DomainModel.user.Role;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import static DomainModel.card.Card.printCards;

public class PlayerController {

    private final Scanner scanner;
    private final CardService cardService;

    public PlayerController(Scanner scanner, CardService cardService) {
        this.scanner = scanner;
        this.cardService = cardService;
    }

    public void playerMenu() throws SQLException {
        ControllerGuards.requireRole(Role.PLAYER);
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

    public void catalogoCarteMenu() throws SQLException {
        ControllerGuards.requireRole(Role.PLAYER);
        boolean running = true;

        while (running) {
            System.out.println("\n--- CATALOGO CARTE ---");
            System.out.println("1) Visualizza tutte le carte");
            System.out.println("2) Cerca carta per nome");
            System.out.println("3) Indietro");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine();
            GameType gameType = UserSession.getInstance().getGameType();
            if (gameType == null) {
                throw new IllegalStateException("Seleziona prima un gioco.");
            }

            switch (choice) {
                case "1" -> {
                    List<Card> cards = cardService.getCardsByGameType(gameType);
                    printCards(cards);
                }
                case "2" -> {
                    System.out.print("Inserisci nome (anche parziale): ");
                    String keyword = scanner.nextLine();

                    List<Card> results = cardService.searchCardsByName(gameType, keyword);
                    printCards(results);
                }
                case "3" -> running = false;
                default -> System.out.println("Scelta non valida.");
            }
        }
    }
}
