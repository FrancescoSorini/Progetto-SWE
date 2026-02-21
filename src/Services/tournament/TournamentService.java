package Services.tournament;

import DomainModel.GameType;
import DomainModel.tournament.Registration;
import DomainModel.tournament.Tournament;
import DomainModel.tournament.TournamentStatus;
import DomainModel.tournament.observer.UserObserver;
import DomainModel.user.User;
import ORM.dao.TournamentDAO;

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
    // 1) CREATE TOURNAMENT
    // ============================================================================
    public void createTournament(User caller, Tournament t) throws SQLException {
        requireLoggedIn(caller);
        t.setOrganizer(caller);
        t.setStatus(TournamentStatus.PENDING);
        validateTournament(t);
        tournamentDAO.createTournament(t);
    }

    // ============================================================================
    // 2) UPDATE TOURNAMENT (ONLY PENDING)
    // ============================================================================
    public void updateTournament(User caller, Tournament updated) throws SQLException {
        Tournament existing = tournamentDAO.getTournamentById(updated.getTournamentId());
        if (existing == null) {
            throw new IllegalArgumentException("Tournament not found.");
        }

        checkTournamentIsPending(existing);
        validateTournament(updated);
        tournamentDAO.updateTournament(updated);
    }

    // ============================================================================
    // 3) DELETE TOURNAMENT
    // ============================================================================
    public void deleteTournament(User caller, int tournamentId) throws SQLException {
        Tournament t = tournamentDAO.getTournamentById(tournamentId);
        if (t == null) {
            throw new IllegalArgumentException("Tournament not found.");
        }

        tournamentDAO.deleteTournament(tournamentId);
    }

    // ============================================================================
    // 4) APPROVE / REJECT
    // ============================================================================
    public void approveTournament(User caller, int tournamentId) throws SQLException {
        Tournament t = tournamentDAO.getTournamentById(tournamentId);
        if (t.getStatus() != TournamentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING tournaments can be approved.");
        }

        attachObservers(t);
        t.setStatus(TournamentStatus.APPROVED);
        tournamentDAO.updateTournament(t);
    }

    public void rejectTournament(User caller, int tournamentId) throws SQLException {
        Tournament t = tournamentDAO.getTournamentById(tournamentId);
        if (t.getStatus() != TournamentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING tournaments can be rejected.");
        }

        t.setStatus(TournamentStatus.REJECTED);
        tournamentDAO.updateTournament(t);
    }

    // ============================================================================
    // 5) AUTO UPDATE (READY / CLOSED)
    // ============================================================================
    public void updateTournamentStatusesAutomatically() throws SQLException {
        List<Tournament> tournaments = tournamentDAO.getAllTournaments();
        LocalDate today = LocalDate.now();

        for (Tournament t : tournaments) {
            // APPROVED -> READY quando deadline raggiunta/superata (giorno incluso) o torneo full
            if (t.getStatus() == TournamentStatus.APPROVED &&
                    (t.isFull() || !today.isBefore(t.getDeadline()))) {
                attachObservers(t);
                t.setStatus(TournamentStatus.READY);
                tournamentDAO.updateTournament(t);
            }

            // READY -> ONGOING quando data corrente = startDate
            if (t.getStatus() == TournamentStatus.READY && today.isEqual(t.getStartDate())) {
                attachObservers(t);
                t.setStatus(TournamentStatus.ONGOING);
                tournamentDAO.updateTournament(t);
            }

            // READY/ONGOING -> FINISHED quando data corrente > startDate
            if ((t.getStatus() == TournamentStatus.READY || t.getStatus() == TournamentStatus.ONGOING)
                    && today.isAfter(t.getStartDate())) {
                attachObservers(t);
                t.setStatus(TournamentStatus.FINISHED);
                tournamentDAO.updateTournament(t);
            }
        }
    }

    // ============================================================================
    // 6) GETTERS
    // ============================================================================
    public Tournament getTournamentById(int tournamentId) throws SQLException {
        //
        // Serve mai chiamare un singolo specifico torneo? Lasciamola per ora
        //
        return tournamentDAO.getTournamentById(tournamentId);
    }

    public List<Tournament> getAllTournaments(User caller) throws SQLException {
        return tournamentDAO.getAllTournaments();
    }

    public List<Tournament> getTournamentsByOrganizer(User caller, int organizerId) throws SQLException {
        return tournamentDAO.getTournamentsByOrganizer(organizerId);
    }

    public List<Tournament> getTournamentsByGameType(GameType gameType) throws SQLException {
        return tournamentDAO.getTournamentsByGameType(gameType);
    }

    public List<Tournament> getTournamentsByStatus(TournamentStatus status) throws SQLException {
        return tournamentDAO.getTournamentsByStatus(status);
    }

    // ============================================================================
    // HELPERS
    // ============================================================================
    private void validateTournament(Tournament t) {
        if (t.getName() == null || t.getName().isBlank()) {
            throw new IllegalArgumentException("Tournament name cannot be empty.");
        }
        if (t.getCapacity() < 1) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        if (t.getDeadline() == null || t.getStartDate() == null) {
            throw new IllegalArgumentException("Dates must not be null.");
        }
        if (t.getStartDate().isBefore(t.getDeadline())) {
            throw new IllegalArgumentException("Start date must be after the registration deadline.");
        }
        if (t.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }
    }

    private void attachObservers(Tournament t) {
        if (t.getRegistrations() == null) {
            return;
        }
        for (Registration r : t.getRegistrations()) {
            t.addObserver(new UserObserver(r.getUser()));
        }
    }

    private void checkTournamentIsPending(Tournament tournament) {
        if (tournament.getStatus() != TournamentStatus.PENDING) {
            throw new IllegalStateException("Tournament can only be edited when in PENDING state.");
        }
    }

    private void requireLoggedIn(User caller) {
        if (caller == null) {
            throw new SecurityException("You must be logged in.");
        }
    }
}
