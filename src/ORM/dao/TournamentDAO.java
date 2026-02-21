package ORM.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ORM.connection.DatabaseConnection;

import DomainModel.tournament.*;
import DomainModel.user.User;
import DomainModel.GameType;


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
            INSERT INTO tournaments (tournament_name, description, organizer_id, capacity, deadline, start_date, status_id, tcg_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tournament.getName());
            ps.setString(2, tournament.getDescription());
            ps.setInt(3, tournament.getOrganizer().getUserId());
            ps.setInt(4, tournament.getCapacity());
            ps.setDate(5, Date.valueOf(tournament.getDeadline()));
            ps.setDate(6, Date.valueOf(tournament.getStartDate()));
            ps.setInt(7, mapStatusToId(tournament.getStatus()));
            ps.setInt(8, tournament.getGameType().getGameId());

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
            SELECT tournament_id, tournament_name, description, organizer_id, capacity, deadline, start_date, status_id, tcg_id
            FROM tournaments
            WHERE tournament_id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Tournament tournament = new Tournament(rs.getString("tournament_name"),
                                                            GameType.fromId(rs.getInt("tcg_id"))
                    );
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
                    RegistrationDAO tempDAO = new RegistrationDAO(connection);
                    List<Registration> registrations = tempDAO.getRegistrationsByTournament(tournamentId);
                    tournament.setRegistrations(registrations);

                    return tournament;
                }
            }
        }
        return null;
    }

    // ====================================================================================
    // 3) READ TOURNAMENT BY GAME TYPE
    // ====================================================================================
    public List<Tournament> getTournamentsByGameType(GameType gameType) throws SQLException {
        String sql = """
            SELECT tournament_id, tournament_name, description, organizer_id, capacity, deadline, start_date, status_id, tcg_id
            FROM tournaments
            WHERE tcg_id = ?
        """;

        List<Tournament> tournaments = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, gameType.getGameId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Tournament tournament = new Tournament(rs.getString("tournament_name"),
                                                           GameType.fromId(rs.getInt("tcg_id"))
                    );
                    tournament.setTournamentId(rs.getInt("tournament_id"));
                    tournament.setDescription(rs.getString("description"));

                    User organizer = userDAO.getUserById(rs.getInt("organizer_id"));
                    tournament.setOrganizer(organizer);

                    tournament.setCapacity(rs.getInt("capacity"));
                    tournament.setDeadline(rs.getDate("deadline").toLocalDate());
                    tournament.setStartDate(rs.getDate("start_date").toLocalDate());
                    tournament.setStatus(mapIdToStatus(rs.getInt("status_id")));

                    // Load registrations
                    RegistrationDAO tempDAO = new RegistrationDAO(connection);
                    List<Registration> registrations = tempDAO.getRegistrationsByTournament(rs.getInt("tournament_id"));
                    tournament.setRegistrations(registrations);

                    tournaments.add(tournament);
                }
            }
        }
        return tournaments;
    }

    // ====================================================================================
    // 4) READ BY ORGANIZER
    // ====================================================================================
    public List<Tournament> getTournamentsByOrganizer(int organizerId) throws SQLException {
        String sql = """
            SELECT tournament_id, tournament_name, description, organizer_id, capacity, deadline, start_date, status_id, tcg_id
            FROM tournaments
            WHERE organizer_id = ?
        """;

        List<Tournament> tournaments = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, organizerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Tournament tournament = new Tournament(rs.getString("tournament_name"),
                                                           GameType.fromId(rs.getInt("tcg_id"))
                    );
                    tournament.setTournamentId(rs.getInt("tournament_id"));
                    tournament.setDescription(rs.getString("description"));

                    User organizer = userDAO.getUserById(rs.getInt("organizer_id"));
                    tournament.setOrganizer(organizer);

                    tournament.setCapacity(rs.getInt("capacity"));
                    tournament.setDeadline(rs.getDate("deadline").toLocalDate());
                    tournament.setStartDate(rs.getDate("start_date").toLocalDate());
                    tournament.setStatus(mapIdToStatus(rs.getInt("status_id")));

                    // Load registrations
                    RegistrationDAO tempDAO = new RegistrationDAO(connection);
                    List<Registration> registrations = tempDAO.getRegistrationsByTournament(rs.getInt("tournament_id"));
                    tournament.setRegistrations(registrations);

                    tournaments.add(tournament);
                }
            }
        }
        return tournaments;
    }

    // ====================================================================================
    // 5) READ BY STATUS
    // ====================================================================================
    public List<Tournament> getTournamentsByStatus(TournamentStatus status) throws SQLException {
        String sql = """
            SELECT tournament_id, tournament_name, description, organizer_id, capacity, deadline, start_date, status_id, tcg_id
            FROM tournaments
            WHERE status_id = ?
        """;

        List<Tournament> tournaments = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, mapStatusToId(status));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Tournament tournament = new Tournament(rs.getString("tournament_name"),
                                                           GameType.fromId(rs.getInt("tcg_id"))
                    );
                    tournament.setTournamentId(rs.getInt("tournament_id"));
                    tournament.setDescription(rs.getString("description"));

                    User organizer = userDAO.getUserById(rs.getInt("organizer_id"));
                    tournament.setOrganizer(organizer);

                    tournament.setCapacity(rs.getInt("capacity"));
                    tournament.setDeadline(rs.getDate("deadline").toLocalDate());
                    tournament.setStartDate(rs.getDate("start_date").toLocalDate());
                    tournament.setStatus(mapIdToStatus(rs.getInt("status_id")));

                    // Load registrations
                    RegistrationDAO tempDAO = new RegistrationDAO(connection);
                    List<Registration> registrations = tempDAO.getRegistrationsByTournament(rs.getInt("tournament_id"));
                    tournament.setRegistrations(registrations);

                    tournaments.add(tournament);
                }
            }
        }
        return tournaments;
    }

    // ====================================================================================
    // 6) READ ALL TOURNAMENTS
    // ====================================================================================
    public List<Tournament> getAllTournaments() throws SQLException {
        String sql = """
            SELECT tournament_id, tournament_name, description, organizer_id, capacity, deadline, start_date, status_id, tcg_id
            FROM tournaments
        """;

        List<Tournament> tournaments = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Tournament tournament = new Tournament(rs.getString("tournament_name"),
                                                       GameType.fromId(rs.getInt("tcg_id")));

                tournament.setTournamentId(rs.getInt("tournament_id"));
                tournament.setDescription(rs.getString("description"));

                User organizer = userDAO.getUserById(rs.getInt("organizer_id"));
                tournament.setOrganizer(organizer);

                tournament.setCapacity(rs.getInt("capacity"));
                tournament.setDeadline(rs.getDate("deadline").toLocalDate());
                tournament.setStartDate(rs.getDate("start_date").toLocalDate());
                tournament.setStatus(mapIdToStatus(rs.getInt("status_id")));

                // Load registrations
                RegistrationDAO tempDAO = new RegistrationDAO(connection);
                List<Registration> registrations = tempDAO.getRegistrationsByTournament(rs.getInt("tournament_id"));
                tournament.setRegistrations(registrations);

                tournaments.add(tournament);
            }
        }
        return tournaments;
    }

    // ====================================================================================
    // 7) UPDATE TOURNAMENT
    // ====================================================================================
    public void updateTournament(Tournament tournament) throws SQLException {
        String sql = """
            UPDATE tournaments
            SET tournament_name = ?, description = ?, organizer_id = ?, capacity = ?, deadline = ?, start_date = ?, status_id = ?, tcg_id = ?
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
            ps.setInt(8, tournament.getGameType().getGameId());
            ps.setInt(9, tournament.getTournamentId());

            ps.executeUpdate();
        }
    }

    // ====================================================================================
    // 8) DELETE TOURNAMENT
    // ====================================================================================
    public void deleteTournament(int tournamentId) throws SQLException {
        // Prima elimina tutte le registrazioni correlate per evitare violazioni FK
        String deleteRegistrations = """
        DELETE FROM registrations
        WHERE tournament_id = ?
    """;
        String deleteTournament = """
        DELETE FROM tournaments
        WHERE tournament_id = ?
    """;

        try {
            connection.setAutoCommit(false);  // Inizia transazione

            // Elimina registrazioni
            try (PreparedStatement ps1 = connection.prepareStatement(deleteRegistrations)) {
                ps1.setInt(1, tournamentId);
                ps1.executeUpdate();
            }

            // Elimina torneo
            try (PreparedStatement ps2 = connection.prepareStatement(deleteTournament)) {
                ps2.setInt(1, tournamentId);
                ps2.executeUpdate();
            }

            connection.commit();  // Conferma transazione

        } catch (SQLException e) {
            connection.rollback();  // Annulla se errore
            throw e;  // Rilancia l'eccezione
        } finally {
            connection.setAutoCommit(true);  // Ripristina auto-commit
        }
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
            case ONGOING -> 5;
            case FINISHED -> 6;
        };
    }

    private TournamentStatus mapIdToStatus(int id) {
        return switch (id) {
            case 1 -> TournamentStatus.PENDING;
            case 2 -> TournamentStatus.APPROVED;
            case 3 -> TournamentStatus.REJECTED;
            case 4 -> TournamentStatus.READY;
            case 5 -> TournamentStatus.ONGOING;
            case 6 -> TournamentStatus.FINISHED;
            default -> throw new IllegalArgumentException("Invalid status id: " + id);
        };
    }
}
