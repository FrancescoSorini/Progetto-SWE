package ORM.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ORM.connection.DatabaseConnection;

import DomainModel.tournament.*;
import DomainModel.user.User;

public class TournamentDAO {

    private final Connection connection;
    private final UserDAO userDAO;

    public TournamentDAO(Connection connection) {
        this.connection = connection;
        this.userDAO = new UserDAO(connection);
    }

    // ====================================================================================
    // 1) CREATE TOURNAMENT
    // ====================================================================================
    public void createTournament(Tournament tournament) throws SQLException {
        String sql = """
            INSERT INTO tournaments (tournament_name, description, organizer_id, capacity, deadline, start_date, status_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tournament.getName());
            ps.setString(2, tournament.getDescription());
            ps.setInt(3, tournament.getOrganizer().getUserId());
            ps.setInt(4, tournament.getCapacity());
            ps.setDate(5, Date.valueOf(tournament.getDeadline()));
            ps.setDate(6, Date.valueOf(tournament.getStartDate()));
            ps.setInt(7, mapStatusToId(tournament.getStatus()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tournament.setTournamentId(rs.getInt(1));
                }
            }
        }
    }

    // ====================================================================================
    // 2) READ TOURNAMENT BY ID
    // ====================================================================================
    public Tournament getTournamentById(int tournamentId) throws SQLException {
        String sql = """
            SELECT tournament_id, tournament_name, description, organizer_id, capacity, deadline, start_date, status_id
            FROM tournaments
            WHERE tournament_id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Tournament tournament = new Tournament(rs.getString("tournament_name"));
                    tournament.setTournamentId(rs.getInt("tournament_id"));
                    tournament.setDescription(rs.getString("description"));

                    //creo UserDAO per avere anche le info dell'organizzatore
                    User organizer = userDAO.getUserById(rs.getInt("organizer_id"));
                    tournament.setOrganizer(organizer);

                    tournament.setCapacity(rs.getInt("capacity"));
                    tournament.setDeadline(rs.getDate("deadline").toLocalDate());
                    tournament.setStartDate(rs.getDate("start_date").toLocalDate());
                    tournament.setStatus(mapIdToStatus(rs.getInt("status_id")));

                    // Load registrations
                    List<Registration> registrations = getRegistrationsForTournament(tournamentId);
                    tournament.setRegistrations(registrations);

                    return tournament;
                }
            }
        }
        return null;
    }

    // ====================================================================================
    // 3) READ ALL TOURNAMENTS
    // ====================================================================================
    public List<Tournament> getAllTournaments() throws SQLException {
        String sql = """
            SELECT tournament_id, tournament_name, description, organizer_id, capacity, deadline, start_date, status_id
            FROM tournaments
        """;

        List<Tournament> tournaments = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Tournament tournament = new Tournament(rs.getString("tournament_name"));
                tournament.setTournamentId(rs.getInt("tournament_id"));
                tournament.setDescription(rs.getString("description"));

                User organizer = userDAO.getUserById(rs.getInt("organizer_id"));
                tournament.setOrganizer(organizer);

                tournament.setCapacity(rs.getInt("capacity"));
                tournament.setDeadline(rs.getDate("deadline").toLocalDate());
                tournament.setStartDate(rs.getDate("start_date").toLocalDate());
                tournament.setStatus(mapIdToStatus(rs.getInt("status_id")));

                // Load registrations
                List<Registration> registrations = getRegistrationsForTournament(rs.getInt("tournament_id"));
                tournament.setRegistrations(registrations);

                tournaments.add(tournament);
            }
        }
        return tournaments;
    }

    // ====================================================================================
    // 4) UPDATE TOURNAMENT
    // ====================================================================================
    public void updateTournament(Tournament tournament) throws SQLException {
        String sql = """
            UPDATE tournaments
            SET tournament_name = ?, description = ?, organizer_id = ?, capacity = ?, deadline = ?, start_date = ?, status_id = ?
            WHERE tournament_id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tournament.getName());
            ps.setString(2, tournament.getDescription());
            ps.setInt(3, tournament.getOrganizer().getUserId());
            ps.setInt(4, tournament.getCapacity());
            ps.setDate(5, Date.valueOf(tournament.getDeadline()));
            ps.setDate(6, Date.valueOf(tournament.getStartDate()));
            ps.setInt(7, mapStatusToId(tournament.getStatus()));
            ps.setInt(8, tournament.getTournamentId());

            ps.executeUpdate();
        }
    }

    // ====================================================================================
    // 5) DELETE TOURNAMENT
    // ====================================================================================
    public void deleteTournament(int tournamentId) throws SQLException {
        String sql = """
            DELETE 
            FROM tournaments
            WHERE tournament_id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            ps.executeUpdate();
        }
    }

    // ====================================================================================
    // 6) ADD REGISTRATION
    // ====================================================================================
    public void addRegistration(int tournamentId, int userId) throws SQLException {
        String sql = """
            INSERT INTO registrations (tournament_id, user_id)
            VALUES (?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ====================================================================================
    // 7) REMOVE REGISTRATION
    // ====================================================================================
    public void removeRegistration(int tournamentId, int userId) throws SQLException {
        String sql = """
            DELETE FROM registrations
            WHERE tournament_id = ? AND user_id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ====================================================================================
    // 8) GET REGISTRATIONS FOR TOURNAMENT
    // ====================================================================================
    public List<Registration> getRegistrationsForTournament(int tournamentId) throws SQLException {
        String sql = """
            SELECT tournament_id, user_id, registration_date
            FROM registrations 
            WHERE tournament_id = ?
        """;

        List<Registration> registrations = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = userDAO.getUserById(rs.getInt("user_id"));
                    Tournament tournament = new Tournament(""); // Dummy tournament for Registration
                    tournament.setTournamentId(tournamentId);

                    Registration registration = new Registration(tournament, user);
                    registration.setRegistrationDate(rs.getTimestamp("registration_date").toLocalDateTime());

                    registrations.add(registration);
                }
            }
        }
        return registrations;
    }

    // ====================================================================================
    // PRIVATE UTILITIES
    // ====================================================================================

    private int mapStatusToId(TournamentStatus status) {
        return switch (status) {
            case PENDING -> 1;
            case APPROVED -> 2;
            case REJECTED -> 3;
            case READY -> 4;
            case CLOSED -> 5;
        };
    }

    private TournamentStatus mapIdToStatus(int id) {
        return switch (id) {
            case 1 -> TournamentStatus.PENDING;
            case 2 -> TournamentStatus.APPROVED;
            case 3 -> TournamentStatus.REJECTED;
            case 4 -> TournamentStatus.READY;
            case 5 -> TournamentStatus.CLOSED;
            default -> throw new IllegalArgumentException("Invalid status id: " + id);
        };
    }
}
