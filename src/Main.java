import Services.card.CardService;
import Services.card.DeckService;
import Services.tournament.RegistrationService;
import Services.tournament.TournamentService;
import Services.user.UserService;
import Controllers.AdminController;
import Controllers.GuestController;
import Controllers.OrganizerController;
import Controllers.PlayerController;
import Controllers.RoleMenuController;
import Controllers.TournamentStatusController;
import ORM.connection.DatabaseConnection;

import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final UserService userService = new UserService(DatabaseConnection.getConnection());
    private static final CardService cardService = new CardService(DatabaseConnection.getConnection());
    private static final DeckService deckService = new DeckService(DatabaseConnection.getConnection());
    private static final TournamentService tournamentService = new TournamentService(DatabaseConnection.getConnection());
    private static final RegistrationService registrationService = new RegistrationService(DatabaseConnection.getConnection());
    private static final TournamentStatusController tournamentStatusController = new TournamentStatusController(tournamentService);

    public static void main(String[] args) {
        PlayerController playerController = new PlayerController(
                scanner, cardService, deckService, tournamentService, registrationService, tournamentStatusController, userService
        );
        AdminController adminController = new AdminController(scanner, userService, cardService, tournamentService);
        OrganizerController organizerController = new OrganizerController(
                scanner, tournamentService, registrationService, tournamentStatusController, userService
        );
        RoleMenuController roleMenuController =
                new RoleMenuController(playerController, adminController, organizerController);
        GuestController guestController = new GuestController(scanner, userService, roleMenuController);

        boolean running = true;
        while (running) {
            running = guestController.showWelcomeMenuAndHandleSelection();
        }
    }
}
