package BusinessLogic.tournament;

import ORM.dao.RegistrationDAO;
import ORM.dao.TournamentDAO;
import DomainModel.tournament.*;
import DomainModel.user.*;
import BusinessLogic.session.UserSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class RegistrationService {

    private final RegistrationDAO registrationDAO;
    private final TournamentDAO tournamentDAO;

    public RegistrationService(Connection connection) {
        this.registrationDAO = new RegistrationDAO(connection);
        this.tournamentDAO = new TournamentDAO(connection);
    }

    // ====================================================================================
    // SECURITY HELPERS
    // ====================================================================================
    private User currentUser() {
        User u = UserSession.getInstance().getCurrentUser();
        if (u == null)
            throw new SecurityException("You must be logged in.");
        return u;
    }

    private void checkPlayerPermission() {
        if (currentUser().getRole() != Role.PLAYER)
            throw new SecurityException("Only a PLAYER can register to tournaments.");
    }

    private void checkAdminPermission() {
        if (currentUser().getRole() != Role.ADMIN)
            throw new SecurityException("Only ADMIN can perform this action.");
    }

    private void checkOrganizerPermission(Tournament tournament) {
        User u = currentUser();
        if (u.getRole() != Role.ORGANIZER || tournament.getOrganizer().getUserId() != u.getUserId())
            throw new SecurityException("Only the tournament organizer can perform this action.");
    }

    // ====================================================================================
    // BUSINESS LOGIC VALIDATION
    // ====================================================================================
    private void validateRegistrationRules(Tournament t, User u, Registration r) throws SQLException {

        if (t.getStatus() != TournamentStatus.APPROVED)
            throw new IllegalStateException("Tournament is not open for registration.");

        if (registrationDAO.isUserRegistered(t.getTournamentId(), u.getUserId()))
            throw new IllegalStateException("User already registered.");

        if (t.isFull())
            throw new IllegalStateException("Tournament capacity reached.");

        if (!t.isRegistrationOpen())
            throw new IllegalStateException("Registration deadline has passed.");

        if(r.getRegDeck().getGameType() != t.getGameType())
            throw new IllegalArgumentException("Deck game type does not match tournament game type.");
    }

    // ====================================================================================
    // 1) REGISTER USER TO TOURNAMENT (PLAYER)
    // ====================================================================================
    public void registerUserToTournament(int tournamentId, Registration reg) throws SQLException {
        checkPlayerPermission();
        User user = currentUser();

        Tournament tournament = tournamentDAO.getTournamentById(tournamentId);
        if (tournament == null)
            throw new IllegalArgumentException("Tournament not found");

        validateRegistrationRules(tournament, user, reg);

        //Registration registration = new Registration(tournament, user);
        reg.setRegistrationDate(LocalDateTime.now());

        registrationDAO.createRegistration(reg);
    }
    // ====================================================================================
    // 2) CANCEL REGISTRATION
    // ====================================================================================
    public void unregisterFromTournament(int tournamentId) throws SQLException {
        User user = currentUser();

        Tournament t = tournamentDAO.getTournamentById(tournamentId);
        if (t == null)
            throw new IllegalArgumentException("Tournament not found");

        boolean registered = registrationDAO.isUserRegistered(tournamentId, user.getUserId());
        if (!registered)
            throw new IllegalStateException("User is not registered.");

        // PLAYER → può cancellare solo le sue registrazioni
        if (user.getRole() == Role.PLAYER) {
            registrationDAO.deleteRegistration(tournamentId, user.getUserId());
            return;
        }

        throw new SecurityException("You cannot remove this registration.");
    }

    // ====================================================================================
    // 2a) CANCEL REGISTRATION (ORGANIZER & ADMIN ONLY)
    // ====================================================================================
    public void unregisterUserFromTournament(int tournamentId, int userId) throws SQLException {
        User caller = currentUser();

        Tournament t = tournamentDAO.getTournamentById(tournamentId);
        if (t == null)
            throw new IllegalArgumentException("Tournament not found");

        boolean registered = registrationDAO.isUserRegistered(tournamentId, userId);
        if (!registered)
            throw new IllegalStateException("User is not registered.");

        // ORGANIZER → può rimuovere gli utenti dal suo torneo
        if (caller.getRole() == Role.ORGANIZER &&
                t.getOrganizer().getUserId() == caller.getUserId()) {
            registrationDAO.deleteRegistration(tournamentId, userId);
            return;
        }

        // ADMIN → può fare tutto
        if (caller.getRole() == Role.ADMIN) {
            registrationDAO.deleteRegistration(tournamentId, userId);
            return;
        }

        throw new SecurityException("You cannot remove this registration.");
    }

    // ====================================================================================
    // 3) GET REGISTRATIONS BY USER (everyone, only their own unless admin)
    // ====================================================================================
    public List<Registration> getRegistrationsByUser(int userId) throws SQLException {
        User caller = currentUser();

        if (caller.getRole() != Role.ADMIN && caller.getUserId() != userId)
            throw new SecurityException("You can only view your own registrations.");

        return registrationDAO.getRegistrationsByUser(userId);
    }

    // ====================================================================================
    // 4) GET REGISTRATIONS BY TOURNAMENT
    // ====================================================================================
    public List<Registration> getRegistrationsByTournament(int tournamentId) throws SQLException {
        Tournament t = tournamentDAO.getTournamentById(tournamentId);
        if (t == null)
            throw new IllegalArgumentException("Tournament not found");

        User caller = currentUser();

        // Organizer can only view their own tournaments
        if (caller.getRole() == Role.ORGANIZER &&
                t.getOrganizer().getUserId() != caller.getUserId())
            throw new SecurityException("You can only view registrations for your tournaments.");

        // Players cannot access this
        if (caller.getRole() == Role.PLAYER)
            throw new SecurityException("Players cannot view tournament registrations.");

        return registrationDAO.getRegistrationsByTournament(tournamentId);
    }

    // ====================================================================================
    // 5) GET ALL REGISTRATIONS (ADMIN)
    // ====================================================================================
    public List<Registration> getAllRegistrations() throws SQLException {
        checkAdminPermission();
        return registrationDAO.getAllRegistrations();
    }
}
