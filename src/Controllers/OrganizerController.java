package Controllers;

import DomainModel.GameType;
import DomainModel.tournament.Tournament;
import Controllers.security.ControllerGuards;
import Controllers.session.UserSession;
import DomainModel.user.Role;
import DomainModel.user.User;
import Services.tournament.TournamentService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class OrganizerController {

    private final Scanner scanner;
    private final TournamentService tournamentService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public OrganizerController(Scanner scanner, TournamentService tournamentService) {
        this.scanner = scanner;
        this.tournamentService = tournamentService;
    }

    public void organizerMenu() throws SQLException {
        ControllerGuards.requireRole(Role.ORGANIZER);
        boolean running = true;

        while (running) {
            System.out.println("\n--- MENU ORGANIZER ---");
            System.out.println("1) Crea Torneo");
            System.out.println("2) Gestisci Torneo");
            System.out.println("3) Logout");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> createTournamentFlow();
                case "2" -> System.out.println(">> Gestisci Torneo (da implementare)");
                case "3" -> {
                    UserSession.getInstance().logout();
                    running = false;
                }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void createTournamentFlow() throws SQLException {
        User organizer = ControllerGuards.requireRole(Role.ORGANIZER);

        System.out.println("\n--- CREA TORNEO ---");
        System.out.print("Nome torneo: ");
        String name = scanner.nextLine().trim();

        System.out.print("Descrizione: ");
        String description = scanner.nextLine().trim();

        int capacity = readCapacity();
        LocalDate deadline = readDate("Data termine iscrizioni (gg/mm/aaaa): ");
        LocalDate startDate = readDate("Data inizio torneo (gg/mm/aaaa): ");
        GameType gameType = readGameType();

        Tournament tournament = new Tournament();
        tournament.setName(name);
        tournament.setDescription(description);
        tournament.setCapacity(capacity);
        tournament.setDeadline(deadline);
        tournament.setStartDate(startDate);
        tournament.setGameType(gameType);

        tournamentService.createTournament(organizer, tournament);
        System.out.println("Torneo creato con successo (stato: PENDING).");
    }

    private int readCapacity() {
        System.out.print("Capacita: ");
        String input = scanner.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Capacita non valida.");
        }
    }

    private LocalDate readDate(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            return LocalDate.parse(input, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato data non valido. Usa gg/mm/aaaa.");
        }
    }

    private GameType readGameType() {
        System.out.println("Scegli GameType:");
        System.out.println("1) MAGIC");
        System.out.println("2) POKEMON");
        System.out.println("3) YUGIOH");
        System.out.print("Scelta: ");

        String choice = scanner.nextLine().trim();
        return switch (choice) {
            case "1" -> GameType.MAGIC;
            case "2" -> GameType.POKEMON;
            case "3" -> GameType.YUGIOH;
            default -> throw new IllegalArgumentException("GameType non valido.");
        };
    }
}
