package Controllers;

import Controllers.security.ControllerGuards;
import Controllers.session.UserSession;
import DomainModel.GameType;
import DomainModel.tournament.Registration;
import DomainModel.tournament.Tournament;
import DomainModel.tournament.TournamentStatus;
import DomainModel.user.Role;
import DomainModel.user.User;
import Services.tournament.RegistrationService;
import Services.tournament.TournamentService;
import Services.user.UserService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class OrganizerController {

    private final Scanner scanner;
    private final TournamentService tournamentService;
    private final RegistrationService registrationService;
    private final TournamentStatusController tournamentStatusController;
    private final UserService userService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter REGISTRATION_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public OrganizerController(
            Scanner scanner,
            TournamentService tournamentService,
            RegistrationService registrationService,
            TournamentStatusController tournamentStatusController,
            UserService userService
    ) {
        this.scanner = scanner;
        this.tournamentService = tournamentService;
        this.registrationService = registrationService;
        this.tournamentStatusController = tournamentStatusController;
        this.userService = userService;
    }

    public void organizerMenu() throws SQLException {
        ControllerGuards.requireRole(Role.ORGANIZER);
        boolean running = true;

        while (running) {
            System.out.println("\n--- MENU ORGANIZER ---");
            System.out.println("1) Crea Torneo");
            System.out.println("2) Gestisci Torneo");
            System.out.println("3) Area Personale");
            System.out.println("4) Cambia Gioco");
            System.out.println("5) Logout");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1" -> createTournamentFlow();
                    case "2" -> gestisciTorneoMenu();
                    case "3" -> areaPersonaleMenu();
                    case "4" -> selectGameType();
                    case "5" -> {
                        UserSession.getInstance().logout();
                        running = false;
                    }
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private void areaPersonaleMenu() throws SQLException {
        User caller = ControllerGuards.requireRole(Role.ORGANIZER);
        boolean running = true;

        while (running) {
            User freshUser = userService.getUser(caller.getUserId());
            printPersonalData(freshUser);
            System.out.println("1) Modifica username");
            System.out.println("2) Modifica email");
            System.out.println("3) Modifica password");
            System.out.println("4) Indietro");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        System.out.print("Nuovo username: ");
                        String newUsername = scanner.nextLine().trim();
                        userService.changeUsername(caller.getUserId(), newUsername);
                        caller.setUsername(newUsername);
                        System.out.println("Username aggiornato con successo.");
                    }
                    case "2" -> {
                        System.out.print("Nuova email: ");
                        String newEmail = scanner.nextLine().trim();
                        userService.changeEmail(caller.getUserId(), newEmail);
                        caller.setEmail(newEmail);
                        System.out.println("Email aggiornata con successo.");
                    }
                    case "3" -> {
                        System.out.print("Nuova password: ");
                        String newPassword = scanner.nextLine().trim();
                        userService.changePassword(caller.getUserId(), newPassword);
                        caller.setPassword(newPassword);
                        System.out.println("Password aggiornata con successo.");
                    }
                    case "4" -> running = false;
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private void selectGameType() {
        System.out.println("\nScegli il gioco:");
        System.out.println("1) Magic");
        System.out.println("2) Yu-Gi-Oh");
        System.out.println("3) Pokemon");
        System.out.print("Scelta: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> UserSession.getInstance().setGameType(GameType.MAGIC);
            case "2" -> UserSession.getInstance().setGameType(GameType.YUGIOH);
            case "3" -> UserSession.getInstance().setGameType(GameType.POKEMON);
            default -> throw new IllegalArgumentException("Scelta gioco non valida.");
        }
    }

    private void gestisciTorneoMenu() throws SQLException {
        User caller = ControllerGuards.requireRole(Role.ORGANIZER);
        tournamentStatusController.syncTournamentStatuses();

        boolean running = true;
        while (running) {
            System.out.println("\n--- GESTISCI TORNEO ---");
            System.out.println("1) Tornei PENDING");
            System.out.println("2) Tornei APPROVED");
            System.out.println("3) Tornei REJECTED");
            System.out.println("4) Tornei READY");
            System.out.println("5) Tornei ONGOING");
            System.out.println("6) Tornei FINISHED");
            System.out.println("7) Indietro");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> pendingTournamentsMenu(caller);
                    case "2" -> approvedTournamentsMenu(caller);
                    case "3" -> rejectedTournamentsMenu(caller);
                    case "4" -> readyTournamentsMenu(caller);
                    case "5" -> ongoingTournamentsMenu(caller);
                    case "6" -> finishedTournamentsMenu(caller);
                    case "7" -> running = false;
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private void pendingTournamentsMenu(User caller) throws SQLException {
        boolean running = true;
        while (running) {
            List<Tournament> tournaments = getOrganizerTournamentsByStatus(caller, TournamentStatus.PENDING);
            System.out.println("\n--- TORNEI PENDING ---");
            printTournamentDetails(tournaments);

            System.out.println("1) Modifica Torneo");
            System.out.println("2) Elimina Torneo");
            System.out.println("3) Indietro");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> modifyTournamentFlow(caller, tournaments);
                    case "2" -> deleteTournamentFlow(caller, tournaments);
                    case "3" -> running = false;
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private void approvedTournamentsMenu(User caller) throws SQLException {
        boolean running = true;
        while (running) {
            List<Tournament> tournaments = getOrganizerTournamentsByStatus(caller, TournamentStatus.APPROVED);
            System.out.println("\n--- TORNEI APPROVED ---");
            printTournamentDetails(tournaments);

            System.out.println("1) Visualizza partecipanti torneo");
            System.out.println("2) Elimina Torneo");
            System.out.println("3) Indietro");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        Tournament target = selectTournamentFromList(tournaments);
                        partecipantiMenu(caller, target);
                    }
                    case "2" -> deleteTournamentFlow(caller, tournaments);
                    case "3" -> running = false;
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private void rejectedTournamentsMenu(User caller) throws SQLException {
        boolean running = true;
        while (running) {
            List<Tournament> tournaments = getOrganizerTournamentsByStatus(caller, TournamentStatus.REJECTED);
            System.out.println("\n--- TORNEI REJECTED ---");
            printTournamentDetails(tournaments);

            System.out.println("1) Elimina Torneo");
            System.out.println("2) Indietro");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> deleteTournamentFlow(caller, tournaments);
                    case "2" -> running = false;
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private void readyTournamentsMenu(User caller) throws SQLException {
        boolean running = true;
        while (running) {
            List<Tournament> tournaments = getOrganizerTournamentsByStatus(caller, TournamentStatus.READY);
            System.out.println("\n--- TORNEI READY ---");
            printTournamentDetails(tournaments);

            System.out.println("1) Visualizza partecipanti torneo");
            System.out.println("2) Indietro");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        Tournament target = selectTournamentFromList(tournaments);
                        partecipantiMenu(caller, target);
                    }
                    case "2" -> running = false;
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private void ongoingTournamentsMenu(User caller) throws SQLException {
        List<Tournament> tournaments = getOrganizerTournamentsByStatus(caller, TournamentStatus.ONGOING);
        System.out.println("\n--- TORNEI ONGOING ---");
        printTournamentDetails(tournaments);
    }

    private void finishedTournamentsMenu(User caller) throws SQLException {
        List<Tournament> tournaments = getOrganizerTournamentsByStatus(caller, TournamentStatus.FINISHED);
        System.out.println("\n--- TORNEI FINISHED ---");
        printTournamentDetails(tournaments);
    }

    private void partecipantiMenu(User caller, Tournament target) throws SQLException {
        boolean running = true;
        while (running) {
            List<Registration> participants = registrationService.getRegistrationsByTournament(caller, target.getTournamentId());
            System.out.println("\n--- PARTECIPANTI TORNEO ID " + target.getTournamentId() + " ---");
            printParticipants(participants);

            System.out.println("1) Rimuovi iscrizione utente");
            System.out.println("2) Indietro");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        if (participants.isEmpty()) {
                            System.out.println("Nessun partecipante da rimuovere.");
                            continue;
                        }
                        int userId = readUserId();
                        boolean exists = participants.stream().anyMatch(r -> r.getUser().getUserId() == userId);
                        if (!exists) {
                            throw new IllegalArgumentException("Utente non trovato tra i partecipanti.");
                        }
                        registrationService.unregisterUserFromTournament(caller, target.getTournamentId(), userId);
                        System.out.println("Iscrizione utente rimossa con successo.");
                    }
                    case "2" -> running = false;
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private Tournament selectTournamentFromList(List<Tournament> tournaments) {
        if (tournaments.isEmpty()) {
            throw new IllegalArgumentException("Nessun torneo disponibile.");
        }

        int tournamentId = readTournamentId();
        return tournaments.stream()
                .filter(t -> t.getTournamentId() == tournamentId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Torneo non trovato nella lista."));
    }

    private void modifyTournamentFlow(User caller, List<Tournament> tournaments) throws SQLException {
        if (tournaments.isEmpty()) {
            System.out.println("Nessun torneo pending da modificare.");
            return;
        }

        int tournamentId = readTournamentId();
        Tournament target = tournaments.stream()
                .filter(t -> t.getTournamentId() == tournamentId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Torneo non trovato nella lista."));

        System.out.println("\n--- INFO TORNEO SELEZIONATO ---");
        printTournamentDetails(List.of(target));

        System.out.println("Ricompila i campi (invio per mantenere valore corrente).");

        System.out.print("Nome torneo [" + target.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isBlank()) {
            target.setName(name);
        }

        System.out.print("Descrizione [" + target.getDescription() + "]: ");
        String description = scanner.nextLine().trim();
        if (!description.isBlank()) {
            target.setDescription(description);
        }

        System.out.print("Capacita [" + target.getCapacity() + "]: ");
        String capacityInput = scanner.nextLine().trim();
        if (!capacityInput.isBlank()) {
            try {
                target.setCapacity(Integer.parseInt(capacityInput));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Capacita non valida.");
            }
        }

        LocalDate deadline = readOptionalDate("Deadline iscrizioni [" + target.getDeadline() + "] (gg/mm/aaaa): ");
        if (deadline != null) {
            target.setDeadline(deadline);
        }

        LocalDate startDate = readOptionalDate("Data inizio [" + target.getStartDate() + "] (gg/mm/aaaa): ");
        if (startDate != null) {
            target.setStartDate(startDate);
        }

        GameType gameType = readOptionalGameType(target.getGameType());
        if (gameType != null) {
            target.setGameType(gameType);
        }

        tournamentService.updateTournament(caller, target);
        System.out.println("Torneo aggiornato con successo.");
    }

    private void deleteTournamentFlow(User caller, List<Tournament> tournaments) throws SQLException {
        if (tournaments.isEmpty()) {
            System.out.println("Nessun torneo disponibile.");
            return;
        }

        int tournamentId = readTournamentId();
        boolean exists = tournaments.stream().anyMatch(t -> t.getTournamentId() == tournamentId);
        if (!exists) {
            throw new IllegalArgumentException("Torneo non trovato nella lista.");
        }

        System.out.print("Confermi eliminazione torneo? (si/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("si") || confirm.equals("s")) {
            tournamentService.deleteTournament(caller, tournamentId);
            System.out.println("Torneo eliminato con successo.");
            return;
        }
        if (confirm.equals("no") || confirm.equals("n")) {
            System.out.println("Eliminazione annullata.");
            return;
        }

        throw new IllegalArgumentException("Scelta non valida. Inserisci 'si' oppure 'no'.");
    }

    private void createTournamentFlow() throws SQLException {
        User organizer = ControllerGuards.requireRole(Role.ORGANIZER);

        System.out.println("\n--- CREA TORNEO ---");
        String name = readTournamentName();

        System.out.print("Descrizione: ");
        String description = scanner.nextLine().trim();

        int capacity = readCapacity();
        LocalDate deadline = readDate("Data termine iscrizioni (gg/mm/aaaa): ");
        LocalDate startDate = readDate("Data inizio torneo (gg/mm/aaaa): ");
        GameType gameType = UserSession.getInstance().getGameType();

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

    private List<Tournament> getOrganizerTournamentsByStatus(User caller, TournamentStatus status) throws SQLException {
        return tournamentService.getTournamentsByOrganizer(caller, caller.getUserId())
                .stream()
                .filter(t -> t.getStatus() == status)
                .toList();
    }

    private String readTournamentName() {
        System.out.print("Nome torneo: ");
        String name = scanner.nextLine().trim();
        if (name.isBlank()) {
            throw new IllegalArgumentException("Nome torneo non valido.");
        }
        return name;
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

    private int readTournamentId() {
        System.out.print("Inserisci ID torneo: ");
        String input = scanner.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID torneo non valido.");
        }
    }

    private int readUserId() {
        System.out.print("Inserisci ID utente: ");
        String input = scanner.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID utente non valido.");
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

    private LocalDate readOptionalDate(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(input, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato data non valido. Usa gg/mm/aaaa.");
        }
    }

    private GameType readOptionalGameType(GameType currentType) {
        System.out.println("GameType corrente: " + currentType);
        System.out.println("Nuovo GameType (invio per non modificare):");
        System.out.println("1) MAGIC");
        System.out.println("2) POKEMON");
        System.out.println("3) YUGIOH");
        System.out.print("Scelta: ");

        String choice = scanner.nextLine().trim();
        if (choice.isBlank()) {
            return null;
        }
        return switch (choice) {
            case "1" -> GameType.MAGIC;
            case "2" -> GameType.POKEMON;
            case "3" -> GameType.YUGIOH;
            default -> throw new IllegalArgumentException("GameType non valido.");
        };
    }

    private void printTournamentDetails(List<Tournament> tournaments) {
        if (tournaments.isEmpty()) {
            System.out.println("Nessun torneo trovato.");
            return;
        }

        for (Tournament tournament : tournaments) {
            int registrationsCount = tournament.getRegistrations() == null ? 0 : tournament.getRegistrations().size();
            System.out.println("----------------------");
            System.out.println("ID: " + tournament.getTournamentId());
            System.out.println("Nome: " + tournament.getName());
            System.out.println("Descrizione: " + tournament.getDescription());
            System.out.println("Organizer: " + (tournament.getOrganizer() == null ? "N/D" : tournament.getOrganizer().getUsername()));
            System.out.println("Capacita: " + tournament.getCapacity());
            System.out.println("Iscritti: " + registrationsCount);
            System.out.println("Deadline: " + tournament.getDeadline());
            System.out.println("Start date: " + tournament.getStartDate());
            System.out.println("GameType: " + tournament.getGameType());
            System.out.println("Status: " + tournament.getStatus());
        }
    }

    private void printParticipants(List<Registration> participants) {
        if (participants.isEmpty()) {
            System.out.println("Nessun partecipante trovato.");
            return;
        }

        for (Registration registration : participants) {
            String formattedRegistrationDateTime =
                    registration.getRegistrationDate().format(REGISTRATION_DATE_TIME_FORMATTER);

            System.out.println("----------------------");
            System.out.println("User ID: " + registration.getUser().getUserId());
            System.out.println("Username: " + registration.getUser().getUsername());
            System.out.println("Email: " + registration.getUser().getEmail());
            System.out.println("Deck ID: " + registration.getRegDeck().getDeckId());
            System.out.println("Data/Ora iscrizione: " + formattedRegistrationDateTime);
        }
    }

    private void printPersonalData(User user) {
        System.out.println("\n--- AREA PERSONALE ---");
        System.out.println("ID: " + user.getUserId());
        System.out.println("Username: " + user.getUsername());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Ruolo: " + user.getRole());
        System.out.println("Stato: " + (user.isEnabled() ? "Abilitato" : "Bannato"));
    }
}
