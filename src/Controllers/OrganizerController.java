package Controllers;

import Controllers.security.ControllerGuards;
import Controllers.session.UserSession;
import DomainModel.user.Role;

import java.util.Scanner;

public class OrganizerController {

    private final Scanner scanner;

    public OrganizerController(Scanner scanner) {
        this.scanner = scanner;
    }

    public void organizerMenu() {
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
}
