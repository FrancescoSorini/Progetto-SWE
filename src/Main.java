import BusinessLogic.card.CardService;
import BusinessLogic.user.UserService;
import Controllers.AdminController;
import Controllers.GuestController;
import Controllers.OrganizerController;
import Controllers.PlayerController;
import Controllers.RoleMenuController;
import ORM.connection.DatabaseConnection;

import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final UserService userService = new UserService(DatabaseConnection.getConnection());
    private static final CardService cardService = new CardService(DatabaseConnection.getConnection());

    public static void main(String[] args) {
        PlayerController playerController = new PlayerController(scanner, cardService);
        AdminController adminController = new AdminController(scanner, userService);
        OrganizerController organizerController = new OrganizerController(scanner);
        RoleMenuController roleMenuController =
                new RoleMenuController(playerController, adminController, organizerController);
        GuestController guestController = new GuestController(scanner, userService, roleMenuController);

        boolean running = true;
        while (running) {
            running = guestController.showWelcomeMenuAndHandleSelection();
        }
    }
}
