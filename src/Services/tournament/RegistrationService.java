package Services.tournament;

import DomainModel.tournament.Registration;
import DomainModel.tournament.Tournament;
import DomainModel.tournament.TournamentStatus;
import DomainModel.user.User;
import ORM.dao.RegistrationDAO;
import ORM.dao.TournamentDAO;

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
    // 1) REGISTER USER TO TOURNAMENT
    // ====================================================================================
    public void registerUserToTournament(User caller, int tournamentId, Registration reg) throws SQLException {
        requireLoggedIn(caller);

        Tournament tournament = tournamentDAO.getTournamentById(tournamentId);
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament not found");
        }

        validateRegistrationRules(tournament, caller, reg);
        reg.setRegistrationDate(LocalDateTime.now());
        registrationDAO.createRegistration(reg);
    }

    // ====================================================================================
    // 2) CANCEL REGISTRATION (SELF)
    // ====================================================================================
    public void unregisterFromTournament(User caller, int tournamentId) throws SQLException {
        requireLoggedIn(caller);

        Tournament tournament = tournamentDAO.getTournamentById(tournamentId);
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament not found");
        }

        boolean registered = registrationDAO.isUserRegistered(tournamentId, caller.getUserId());
        if (!registered) {
            throw new IllegalStateException("User is not registered.");
        }

        registrationDAO.deleteRegistration(tournamentId, caller.getUserId());
    }

    // ====================================================================================
    // 2a) CANCEL REGISTRATION (TARGET USER)
    // ====================================================================================
    public void unregisterUserFromTournament(User caller, int tournamentId, int userId) throws SQLException {
        requireLoggedIn(caller);

        Tournament tournament = tournamentDAO.getTournamentById(tournamentId);
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament not found");
        }

        boolean registered = registrationDAO.isUserRegistered(tournamentId, userId);
        if (!registered) {
            throw new IllegalStateException("User is not registered.");
        }

        registrationDAO.deleteRegistration(tournamentId, userId);
    }

    // ====================================================================================
    // 3) GET REGISTRATIONS BY USER
    // ====================================================================================
    public List<Registration> getRegistrationsByUser(User caller, int userId) throws SQLException {
        requireLoggedIn(caller);
        return registrationDAO.getRegistrationsByUser(userId);
    }

    // ====================================================================================
    // 4) GET REGISTRATIONS BY TOURNAMENT
    // ====================================================================================
    public List<Registration> getRegistrationsByTournament(User caller, int tournamentId) throws SQLException {
        requireLoggedIn(caller);

        Tournament tournament = tournamentDAO.getTournamentById(tournamentId);
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament not found");
        }

        return registrationDAO.getRegistrationsByTournament(tournamentId);
    }

    // ====================================================================================
    // 5) GET ALL REGISTRATIONS
    // ====================================================================================
    public List<Registration> getAllRegistrations(User caller) throws SQLException {
        requireLoggedIn(caller);
        return registrationDAO.getAllRegistrations();
    }

    // ====================================================================================
    // HELPERS
    // ====================================================================================
    private void validateRegistrationRules(Tournament tournament, User caller, Registration registration)
            throws SQLException {
        if (tournament.getStatus() != TournamentStatus.APPROVED) {
            throw new IllegalStateException("Tournament is not open for registration.");
        }
        if (registrationDAO.isUserRegistered(tournament.getTournamentId(), caller.getUserId())) {
            throw new IllegalStateException("User already registered.");
        }
        if (tournament.isFull()) {
            throw new IllegalStateException("Tournament capacity reached.");
        }
        if (!tournament.isRegistrationOpen()) {
            throw new IllegalStateException("Registration deadline has passed.");
        }
        if (registration.getRegDeck().getGameType() != tournament.getGameType()) {
            throw new IllegalArgumentException("Deck game type does not match tournament game type.");
        }
    }

    private void requireLoggedIn(User caller) {
        if (caller == null) {
            throw new SecurityException("You must be logged in.");
        }
    }
}
