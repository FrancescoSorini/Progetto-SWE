package Controllers;

import Controllers.security.ControllerGuards;
import DomainModel.user.Role;

import java.sql.SQLException;

public class RoleMenuController {

    private final PlayerController playerController;
    private final AdminController adminController;
    private final OrganizerController organizerController;

    public RoleMenuController(
            PlayerController playerController,
            AdminController adminController,
            OrganizerController organizerController
    ) {
        this.playerController = playerController;
        this.adminController = adminController;
        this.organizerController = organizerController;
    }

    public void showRoleMenu() throws SQLException {
        Role role = ControllerGuards.requireLoggedIn().getRole();

        switch (role) {
            case PLAYER -> playerController.playerMenu();
            case ADMIN -> adminController.adminMenu();
            case ORGANIZER -> organizerController.organizerMenu();
        }
    }
}
