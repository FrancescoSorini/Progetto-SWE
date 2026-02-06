package BusinessLogic.tournament;

import ORM.dao.TournamentDAO;
import DomainModel.tournament.*;
import DomainModel.tournament.observer.UserObserver;
import DomainModel.user.Role;
import DomainModel.user.User;
import BusinessLogic.session.UserSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TournamentService {

    private final TournamentDAO tournamentDAO;

    public TournamentService(Connection connection) {
        this.tournamentDAO = new TournamentDAO(connection);
    }

    // ============================================================================
    // SECURITY HELPERS
    // ============================================================================
    private User currentUser() {
        User u = UserSession.getInstance().getCurrentUser();
        if (u == null)
            throw new SecurityException("You must be logged in.");
        return u;
    }

    private void checkAdmin() {
        if (currentUser().getRole() != Role.ADMIN)
            throw new SecurityException("Only ADMIN can perform this action.");
    }

    private void checkOrganizer() {
        if (currentUser().getRole() != Role.ORGANIZER)
            throw new SecurityException("Only ORGANIZER can perform this action.");
    }

    private void checkOrganizerOwnership(Tournament t) {
        User u = currentUser();
        if (u.getRole() != Role.ORGANIZER || t.getOrganizer().getUserId() != u.getUserId())
            throw new SecurityException("You can only modify tournaments you created.");
    }

    private void checkTournamentIsPending(Tournament t) {
        if (t.getStatus() != TournamentStatus.PENDING)
            throw new IllegalStateException("Tournament can only be edited when in PENDING state.");
    }

    // ============================================================================
    // OBSERVER HELPERS
    // ============================================================================
    private void attachObservers(Tournament t) {
        if (t.getRegistrations() == null) return;
        for (Registration r : t.getRegistrations()) {
            t.addObserver(new UserObserver(r.getUser()));
        }
    }

    // ============================================================================
    // 1) CREATE TOURNAMENT (ORGANIZER)
    // ============================================================================
    public void createTournament(Tournament t) throws SQLException {
        checkOrganizer();

        t.setOrganizer(currentUser());
        t.setStatus(TournamentStatus.PENDING);

        validateTournament(t);

        tournamentDAO.createTournament(t);
    }

    // ============================================================================
    // BUSINESS VALIDATION
    // ============================================================================
    private void validateTournament(Tournament t) {

        if (t.getName() == null || t.getName().isBlank())
            throw new IllegalArgumentException("Tournament name cannot be empty.");

        if (t.getCapacity() <= 1)
            throw new IllegalArgumentException("Capacity must be positive.");

        if (t.getDeadline() == null || t.getStartDate() == null)
            throw new IllegalArgumentException("Dates must not be null.");

        if (t.getStartDate().isBefore(t.getDeadline()))
            throw new IllegalArgumentException("Start date must be after the registration deadline.");

        if (t.getStartDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Start date cannot be in the past.");
    }

    // ============================================================================
    // 2) UPDATE TOURNAMENT (ORGANIZER ONLY, ONLY PENDING)
    // ============================================================================
    public void updateTournament(Tournament updated) throws SQLException {
        checkOrganizer();

        Tournament existing = tournamentDAO.getTournamentById(updated.getTournamentId());
        if (existing == null)
            throw new IllegalArgumentException("Tournament not found.");

        checkOrganizerOwnership(existing);
        checkTournamentIsPending(existing);

        validateTournament(updated);

        tournamentDAO.updateTournament(updated);
    }

    // ============================================================================
    // 3) DELETE TOURNAMENT (Organizer only for own tournaments, Admin for all)
    // ============================================================================
    public void deleteTournament(int tournamentId) throws SQLException {
        Tournament t = tournamentDAO.getTournamentById(tournamentId);
        if (t == null)
            throw new IllegalArgumentException("Tournament not found.");

        User user = currentUser();

        if (user.getRole() == Role.ADMIN) {
            tournamentDAO.deleteTournament(tournamentId);
            return;
        }

        if (user.getRole() == Role.ORGANIZER &&
                t.getOrganizer().getUserId() == user.getUserId()) {
            tournamentDAO.deleteTournament(tournamentId);
            return;
        }

        throw new SecurityException("You are not allowed to delete this tournament.");
    }

    // ============================================================================
    // 4) APPROVE / REJECT (ADMIN)
    // ============================================================================
    public void approveTournament(int tournamentId) throws SQLException {
        checkAdmin();
        Tournament t = tournamentDAO.getTournamentById(tournamentId);

        if (t.getStatus() != TournamentStatus.PENDING)
            throw new IllegalStateException("Only PENDING tournaments can be approved.");

        attachObservers(t);
        t.setStatus(TournamentStatus.APPROVED);
        tournamentDAO.updateTournament(t);
    }

    public void rejectTournament(int tournamentId) throws SQLException {
        checkAdmin();
        Tournament t = tournamentDAO.getTournamentById(tournamentId);

        if (t.getStatus() != TournamentStatus.PENDING)
            throw new IllegalStateException("Only PENDING tournaments can be rejected.");

        // Nessuna notifica quando un torneo viene rifiutato
        t.setStatus(TournamentStatus.REJECTED);
        tournamentDAO.updateTournament(t);
    }

    // ============================================================================
    // 5) AUTO UPDATE (READY / CLOSED)
    // ============================================================================
    public void updateTournamentStatusesAutomatically() throws SQLException {
        List<Tournament> tournaments = tournamentDAO.getAllTournaments();

        for (Tournament t : tournaments) {

            if (t.getStatus() == TournamentStatus.APPROVED &&
                    LocalDate.now().isAfter(t.getDeadline())) {
                attachObservers(t);
                t.setStatus(TournamentStatus.READY);
                tournamentDAO.updateTournament(t);
            }

            if (t.getStatus() == TournamentStatus.READY &&
                    LocalDate.now().isAfter(t.getStartDate())) {
                attachObservers(t);
                t.setStatus(TournamentStatus.CLOSED);
                tournamentDAO.updateTournament(t);
            }

            if(t.getStatus() == TournamentStatus.APPROVED && t.isFull()){
                attachObservers(t);
                t.setStatus(TournamentStatus.READY);
                tournamentDAO.updateTournament(t);
            }
        }
    }

    // ============================================================================
    // 6) GETTERS (permissions vary)
    // ============================================================================
    public Tournament getTournamentById(int tournamentId) throws SQLException {
        return tournamentDAO.getTournamentById(tournamentId);
    }

    public List<Tournament> getAllTournaments() throws SQLException {
        User u = currentUser();

        if (u.getRole() == Role.ADMIN)
            return tournamentDAO.getAllTournaments();

        if (u.getRole() == Role.ORGANIZER)
            return tournamentDAO.getAllTournaments();

        // Players can only see APPROVED or READY
        return tournamentDAO.getAllTournaments()
                .stream()
                .filter(t -> t.getStatus() == TournamentStatus.APPROVED ||
                        t.getStatus() == TournamentStatus.READY)
                .toList();
    }

    public List<Tournament> getTournamentsByOrganizer(int organizerId) throws SQLException {
        User caller = currentUser();

        if (caller.getRole() != Role.ADMIN &&
                caller.getUserId() != organizerId)
            throw new SecurityException("You can only view your own tournaments.");

        return tournamentDAO.getAllTournaments()
                .stream()
                .filter(t -> t.getOrganizer().getUserId() == organizerId)
                .toList();
    }
}
