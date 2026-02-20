package Controllers;

import Controllers.security.ControllerGuards;
import Controllers.session.UserSession;
import DomainModel.GameType;
import DomainModel.card.Card;
import DomainModel.card.Deck;
import DomainModel.tournament.Registration;
import DomainModel.tournament.Tournament;
import DomainModel.tournament.TournamentStatus;
import DomainModel.user.Role;
import DomainModel.user.User;
import Services.card.CardService;
import Services.card.DeckService;
import Services.tournament.RegistrationService;
import Services.tournament.TournamentService;
import Services.user.UserService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import static DomainModel.card.Card.printCards;

public class PlayerController {

    private final Scanner scanner;
    private final CardService cardService;
    private final DeckService deckService;
    private final TournamentService tournamentService;
    private final RegistrationService registrationService;
    private final TournamentStatusController tournamentStatusController;
    private final UserService userService;

    public PlayerController(
            Scanner scanner,
            CardService cardService,
            DeckService deckService,
            TournamentService tournamentService,
            RegistrationService registrationService,
            TournamentStatusController tournamentStatusController,
            UserService userService
    ) {
        this.scanner = scanner;
        this.cardService = cardService;
        this.deckService = deckService;
        this.tournamentService = tournamentService;
        this.registrationService = registrationService;
        this.tournamentStatusController = tournamentStatusController;
        this.userService = userService;
    }

    public void playerMenu() throws SQLException {
        ControllerGuards.requireRole(Role.PLAYER);
        boolean running = true;

        while (running) {
            System.out.println("\n--- MENU PLAYER ---");
            System.out.println("1) Gestione Mazzi");
            System.out.println("2) Catalogo Carte");
            System.out.println("3) Check Tornei");
            System.out.println("4) Area Personale");
            System.out.println("5) Cambia Gioco");
            System.out.println("6) Logout");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1" -> gestioneMazziMenu();
                    case "2" -> catalogoCarteMenu();
                    case "3" -> checkTorneiMenu();
                    case "4" -> areaPersonaleMenu();
                    case "5" -> selectGameType();
                    case "6" -> {
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
        User caller = ControllerGuards.requireRole(Role.PLAYER);
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

    private void printPersonalData(User user) {
        System.out.println("\n--- AREA PERSONALE ---");
        System.out.println("ID: " + user.getUserId());
        System.out.println("Username: " + user.getUsername());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Ruolo: " + user.getRole());
        System.out.println("Stato: " + (user.isEnabled() ? "Abilitato" : "Bannato"));
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

    private void gestioneMazziMenu() throws SQLException {
        User caller = ControllerGuards.requireRole(Role.PLAYER);
        boolean running = true;

        while (running) {
            List<Deck> myDecks = getMyDecksForSessionGameType(caller);
            printDeckNames(myDecks);

            System.out.println("\n--- GESTIONE MAZZI ---");
            System.out.println("1) Seleziona Mazzo");
            System.out.println("2) Crea Mazzo");
            System.out.println("3) Indietro");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> selezionaMazzoFlow(caller, myDecks);
                    case "2" -> creaMazzoFlow(caller);
                    case "3" -> running = false;
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private void selezionaMazzoFlow(User caller, List<Deck> myDecks) throws SQLException {
        if (myDecks.isEmpty()) {
            System.out.println("Non hai mazzi disponibili.");
            return;
        }

        int deckId = readDeckId();
        Deck selectedDeck = myDecks.stream()
                .filter(d -> d.getDeckId() == deckId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Mazzo non trovato tra i tuoi mazzi."));

        stampaCarteNelMazzo(selectedDeck.getDeckId());

        boolean running = true;
        while (running) {
            System.out.println("\n--- AZIONI MAZZO ---");
            System.out.println("1) Modifica mazzo");
            System.out.println("2) Elimina mazzo");
            System.out.println("3) Indietro");
            System.out.print("Scelta: ");

            String action = scanner.nextLine().trim();
            try {
                switch (action) {
                    case "1" -> modificaMazzoFlow(caller, selectedDeck.getDeckId());
                    case "2" -> {
                        eliminaMazzoFlow(caller, selectedDeck.getDeckId());
                        running = false;
                    }
                    case "3" -> running = false;
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private void modificaMazzoFlow(User caller, int deckId) throws SQLException {
        boolean running = true;

        while (running) {
            System.out.println("\n--- MODIFICA MAZZO ---");
            System.out.println("1) Cambia nome mazzo");
            System.out.println("2) Aggiungi carta");
            System.out.println("3) Rimuovi carta");
            System.out.println("4) Indietro");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> cambiaNomeMazzoFlow(caller, deckId);
                    case "2" -> aggiungiCartaAlMazzoFlow(caller, deckId);
                    case "3" -> rimuoviCartaDalMazzoFlow(caller, deckId);
                    case "4" -> running = false;
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private void cambiaNomeMazzoFlow(User caller, int deckId) throws SQLException {
        System.out.print("Inserisci nuovo nome mazzo: ");
        String newName = scanner.nextLine().trim();
        deckService.renameDeck(caller, deckId, newName);
        System.out.println("Nome mazzo aggiornato.");
    }

    private void aggiungiCartaAlMazzoFlow(User caller, int deckId) throws SQLException {
        GameType sessionGameType = UserSession.getInstance().getGameType();
        if (sessionGameType == null) {
            throw new IllegalStateException("GameType non selezionato in sessione.");
        }

        List<Card> cards = cardService.getCardsByGameType(sessionGameType);
        printCardsWithId(cards);
        if (cards.isEmpty()) {
            return;
        }

        System.out.print("Inserisci ID carta da aggiungere: ");
        String cardIdInput = scanner.nextLine().trim();
        int cardId;
        try {
            cardId = Integer.parseInt(cardIdInput);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID carta non valido.");
        }

        Card selectedCard = cards.stream()
                .filter(c -> c.getCardId() == cardId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Carta non trovata nel catalogo selezionato."));

        deckService.addCardToDeck(caller, deckId, selectedCard.getName());
        System.out.println("Carta aggiunta al mazzo.");
    }

    private void rimuoviCartaDalMazzoFlow(User caller, int deckId) throws SQLException {
        stampaCarteNelMazzo(deckId);
        System.out.print("Inserisci ID carta da rimuovere: ");
        String cardIdInput = scanner.nextLine().trim();

        int cardId;
        try {
            cardId = Integer.parseInt(cardIdInput);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID carta non valido.");
        }

        deckService.removeCardFromDeck(caller, deckId, cardId);
        System.out.println("Carta rimossa dal mazzo.");
    }

    private void eliminaMazzoFlow(User caller, int deckId) throws SQLException {
        System.out.print("Confermi eliminazione mazzo? (si/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("si") || confirm.equals("s")) {
            deckService.deleteDeck(caller, deckId);
            System.out.println("Mazzo eliminato con successo.");
            return;
        }
        if (confirm.equals("no") || confirm.equals("n")) {
            System.out.println("Eliminazione annullata.");
            return;
        }

        throw new IllegalArgumentException("Scelta non valida. Inserisci 'si' oppure 'no'.");
    }

    private void creaMazzoFlow(User caller) throws SQLException {
        GameType sessionGameType = UserSession.getInstance().getGameType();
        if (sessionGameType == null) {
            throw new IllegalStateException("GameType non selezionato in sessione.");
        }

        System.out.print("Inserisci nome del nuovo mazzo: ");
        String deckName = scanner.nextLine().trim();

        deckService.createDeck(caller, deckName, sessionGameType);
        System.out.println("Mazzo creato con successo.");
    }

    private int readDeckId() {
        System.out.print("Inserisci ID mazzo: ");
        String deckIdInput = scanner.nextLine().trim();
        try {
            return Integer.parseInt(deckIdInput);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID mazzo non valido.");
        }
    }

    private void stampaCarteNelMazzo(int deckId) throws SQLException {
        List<Card> cardsInDeck = deckService.getCardsInDeck(deckId);
        System.out.println("\n--- CARTE NEL MAZZO ---");
        if (cardsInDeck.isEmpty()) {
            System.out.println("Nessuna carta presente nel mazzo.");
            return;
        }
        printCardsWithId(cardsInDeck);
    }

    private void printDeckNames(List<Deck> decks) {
        System.out.println("\n--- I TUOI MAZZI ---");
        if (decks.isEmpty()) {
            System.out.println("Nessun mazzo disponibile.");
            return;
        }
        for (Deck deck : decks) {
            System.out.println("----------------------");
            System.out.println("ID: " + deck.getDeckId());
            System.out.println("Nome: " + deck.getDeckName());
        }
    }

    private void printCardsWithId(List<Card> cards) {
        if (cards.isEmpty()) {
            System.out.println("Nessuna carta trovata.");
            return;
        }
        for (Card card : cards) {
            System.out.println("----------------------");
            System.out.println("ID: " + card.getCardId());
            System.out.println("Nome: " + card.getName());
            System.out.println("GameType: " + card.getType());
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
            try {
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
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private void checkTorneiMenu() throws SQLException {
        User caller = ControllerGuards.requireRole(Role.PLAYER);
        GameType sessionGameType = UserSession.getInstance().getGameType();
        if (sessionGameType == null) {
            throw new IllegalStateException("GameType non selezionato in sessione.");
        }

        showPendingNotifications();
        tournamentStatusController.syncTournamentStatuses();

        boolean running = true;
        while (running) {
            List<Tournament> approvedTournaments = tournamentService.getAllTournaments(caller).stream()
                    .filter(t -> t.getGameType() == sessionGameType)
                    .filter(t -> t.getStatus() == TournamentStatus.APPROVED)
                    .toList();

            System.out.println("\n--- CHECK TORNEI ---");
            printTournamentDetails(approvedTournaments);
            System.out.println("1) Iscriviti ad un torneo");
            System.out.println("2) I miei tornei");
            System.out.println("3) Indietro");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> iscrivitiATorneoFlow(caller, approvedTournaments);
                    case "2" -> mieiTorneiFlow(caller);
                    case "3" -> running = false;
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private void iscrivitiATorneoFlow(User caller, List<Tournament> approvedTournaments) throws SQLException {
        if (approvedTournaments.isEmpty()) {
            System.out.println("Nessun torneo disponibile per l'iscrizione.");
            return;
        }

        int tournamentId = readTournamentId();
        Tournament selectedTournament = approvedTournaments.stream()
                .filter(t -> t.getTournamentId() == tournamentId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Torneo non trovato nella lista visualizzata."));

        boolean chooseDeck = true;
        while (chooseDeck) {
            System.out.println("\nScegli il mazzo:");
            List<Deck> myDecks = getMyDecksForSessionGameType(caller);
            printDeckNames(myDecks);

            System.out.println("1) Seleziona mazzo");
            System.out.println("2) Indietro");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        if (myDecks.isEmpty()) {
                            System.out.println("Non hai mazzi disponibili.");
                            continue;
                        }

                        int deckId = readDeckId();
                        Deck selectedDeck = myDecks.stream()
                                .filter(d -> d.getDeckId() == deckId)
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Mazzo non trovato tra i tuoi mazzi."));

                        stampaCarteNelMazzo(selectedDeck.getDeckId());

                        System.out.print("Confermi iscrizione al torneo con questo mazzo? (si/no): ");
                        String confirm = scanner.nextLine().trim().toLowerCase();
                        if (confirm.equals("si") || confirm.equals("s")) {
                            Registration registration = new Registration(selectedTournament, caller, selectedDeck);
                            registrationService.registerUserToTournament(caller, selectedTournament.getTournamentId(), registration);
                            System.out.println("Iscrizione completata con successo.");
                            chooseDeck = false;
                        } else if (confirm.equals("no") || confirm.equals("n")) {
                            System.out.println("Iscrizione annullata. Torno alla lista mazzi.");
                        } else {
                            throw new IllegalArgumentException("Scelta non valida. Inserisci 'si' oppure 'no'.");
                        }
                    }
                    case "2" -> chooseDeck = false;
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private List<Deck> getMyDecksForSessionGameType(User caller) throws SQLException {
        GameType sessionGameType = UserSession.getInstance().getGameType();
        if (sessionGameType == null) {
            throw new IllegalStateException("GameType non selezionato in sessione.");
        }

        return deckService.getMyDecks(caller).stream()
                .filter(deck -> deck.getGameType() == sessionGameType)
                .toList();
    }

    private void mieiTorneiFlow(User caller) throws SQLException {
        showPendingNotifications();
        tournamentStatusController.syncTournamentStatuses();

        boolean running = true;
        while (running) {
            List<Tournament> myTournaments = registrationService.getRegistrationsByUser(caller, caller.getUserId())
                    .stream()
                    .map(Registration::getTournament)
                    .filter(t -> t.getStatus() == TournamentStatus.APPROVED
                            || t.getStatus() == TournamentStatus.READY
                            || t.getStatus() == TournamentStatus.ONGOING
                            || t.getStatus() == TournamentStatus.FINISHED)
                    .toList();

            System.out.println("\n--- I MIEI TORNEI ---");
            printTournamentDetails(myTournaments);
            System.out.println("1) Disiscriviti da un torneo");
            System.out.println("2) Indietro");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        if (myTournaments.isEmpty()) {
                            System.out.println("Non sei iscritto a nessun torneo.");
                            continue;
                        }
                        int tournamentId = readTournamentId();
                        boolean exists = myTournaments.stream().anyMatch(t -> t.getTournamentId() == tournamentId);
                        if (!exists) {
                            throw new IllegalArgumentException("Torneo non trovato tra i tuoi tornei.");
                        }
                        registrationService.unregisterFromTournament(caller, tournamentId);
                        System.out.println("Disiscrizione completata.");
                    }
                    case "2" -> running = false;
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
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

    private void showPendingNotifications() {
        List<String> notifications = UserSession.getAndClearNotificationsForCurrentUser();
        if (notifications.isEmpty()) {
            return;
        }

        System.out.println("\n--- NOTIFICHE ---");
        for (String message : notifications) {
            System.out.println("* " + message);
        }
    }
}
