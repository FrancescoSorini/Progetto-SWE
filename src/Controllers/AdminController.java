package Controllers;

import DomainModel.GameType;
import DomainModel.tournament.Tournament;
import DomainModel.tournament.TournamentStatus;
import Services.tournament.TournamentService;
import Services.user.UserService;
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
    private final TournamentService tournamentService;

    public AdminController(Scanner scanner, UserService userService, TournamentService tournamentService) {
        this.scanner = scanner;
        this.userService = userService;
        this.tournamentService = tournamentService;
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
                case "3" -> approvazioneTorneiMenu();
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
                    showUserManagementActions(caller);
                }
                case "2" -> {
                    User caller = ControllerGuards.requireRole(Role.ADMIN);
                    System.out.print("Inserisci nome (anche parziale): ");
                    String keyword = scanner.nextLine();

                    List<User> results = userService.searchUsersByName(keyword);
                    printUsers(results);
                    showUserManagementActions(caller);
                }
                case "3" -> running = false;
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void showUserManagementActions(User caller) throws SQLException {
        boolean managing = true;

        while (managing) {
            System.out.println("\n--- AZIONI UTENTE ---");
            System.out.println("1) Modifica Stato Utente (Ruolo)");
            System.out.println("2) Banna/Abilita Utente");
            System.out.println("3) Indietro");
            System.out.print("Scelta: ");

            String actionChoice = scanner.nextLine();

            switch (actionChoice) {
                case "1" -> handleChangeUserRole(caller);
                case "2" -> handleSetUserEnabled(caller);
                case "3" -> managing = false;
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void handleChangeUserRole(User caller) throws SQLException {
        int targetUserId = readUserIdFromCli();

        System.out.print("Inserisci nuovo ruolo (PLAYER, ORGANIZER, ADMIN): ");
        String roleInput = scanner.nextLine().trim().toUpperCase();

        Role newRole;
        try {
            newRole = Role.valueOf(roleInput);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ruolo non valido.");
        }

        userService.changeUserRole(caller, targetUserId, newRole);
        System.out.println("Ruolo utente aggiornato con successo.");
    }

    private void handleSetUserEnabled(User caller) throws SQLException {
        int targetUserId = readUserIdFromCli();

        System.out.print("Scegli se abilitare o disabilitare l'utente (si/no): ");
        String enabledInput = scanner.nextLine().trim().toLowerCase();

        boolean enabled;
        if (enabledInput.equals("si") || enabledInput.equals("s")) {
            enabled = true;
        } else if (enabledInput.equals("no") || enabledInput.equals("n")) {
            enabled = false;
        } else {
            throw new IllegalArgumentException("Scelta non valida. Inserisci 'si' oppure 'no'.");
        }

        userService.setUserEnabled(caller, targetUserId, enabled);
        System.out.println("Stato utente aggiornato con successo.");
    }

    private int readUserIdFromCli() {
        System.out.print("Inserisci ID utente target: ");
        String idInput = scanner.nextLine().trim();

        try {
            return Integer.parseInt(idInput);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID utente non valido.");
        }
    }

    private void approvazioneTorneiMenu() throws SQLException {
        User caller = ControllerGuards.requireRole(Role.ADMIN);
        GameType selectedGameType = UserSession.getInstance().getGameType();

        if (selectedGameType == null) {
            throw new IllegalStateException("GameType non selezionato in sessione.");
        }

        List<Tournament> pendingTournaments = tournamentService.getAllTournaments(caller).stream()
                .filter(t -> t.getStatus() == TournamentStatus.PENDING)
                .filter(t -> t.getGameType() == selectedGameType)
                .toList();

        System.out.println("\n--- APPROVAZIONE TORNEI ---");
        System.out.println("Filtro attivo: GameType = " + selectedGameType + ", Status = PENDING");

        if (pendingTournaments.isEmpty()) {
            System.out.println("Nessun torneo pending trovato per il GameType selezionato.");
            return;
        }

        printTournamentDetails(pendingTournaments);

        System.out.print("Inserisci ID torneo da valutare: ");
        String idInput = scanner.nextLine().trim();

        int tournamentId;
        try {
            tournamentId = Integer.parseInt(idInput);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID torneo non valido.");
        }

        boolean existsInFilteredList = pendingTournaments.stream()
                .anyMatch(t -> t.getTournamentId() == tournamentId);
        if (!existsInFilteredList) {
            throw new IllegalArgumentException("Il torneo selezionato non e nella lista pending filtrata.");
        }

        System.out.print("Vuoi approvare il torneo? (si/no): ");
        String approveInput = scanner.nextLine().trim().toLowerCase();

        if (approveInput.equals("si") || approveInput.equals("s")) {
            tournamentService.approveTournament(caller, tournamentId);
            System.out.println("Torneo approvato con successo.");
            return;
        }
        if (approveInput.equals("no") || approveInput.equals("n")) {
            tournamentService.rejectTournament(caller, tournamentId);
            System.out.println("Torneo rifiutato con successo.");
            return;
        }

        throw new IllegalArgumentException("Scelta non valida. Inserisci 'si' oppure 'no'.");
    }

    private void printTournamentDetails(List<Tournament> tournaments) {
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
}
