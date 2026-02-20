package ORM.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import DomainModel.tournament.Registration;
import DomainModel.tournament.Tournament;
import DomainModel.user.User;
import DomainModel.card.Deck;


public class RegistrationDAO {
    private final Connection connection;
    private final UserDAO userDAO;

    public RegistrationDAO(Connection connection) {
        this.connection = connection;
        this.userDAO = new UserDAO(connection);
    }

    // ====================================================================================
    // 1) CREATE REGISTRATION
    // ====================================================================================
    public void createRegistration(Registration registration) throws SQLException {
        String sql = """
            INSERT INTO registrations (tournament_id, user_id, registration_date, reg_deck)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, registration.getTournament().getTournamentId());
            ps.setInt(2, registration.getUser().getUserId());
            ps.setTimestamp(3, Timestamp.valueOf(registration.getRegistrationDate()));
            ps.setInt(4, registration.getRegDeck().getDeckId());
            ps.executeUpdate();
        }
    }

    // ====================================================================================
    // 2) READ REGISTRATION BY TOURNAMENT AND USER
    // ====================================================================================
    public Registration getRegistration(int tournamentId, int userId) throws SQLException {
        String sql = """
            SELECT tournament_id, user_id, registration_date, reg_deck
            FROM registrations
            WHERE tournament_id = ? AND user_id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    Tournament tournament = new Tournament("");
                    tournament.setTournamentId(tournamentId);

                    User user = new User("");
                    user.setUserId(userId);

                    Deck deck = new Deck("", user);
                    deck.setDeckId(rs.getInt("reg_deck"));

                    Registration registration = new Registration(tournament, user, deck);
                    registration.setRegistrationDate(rs.getTimestamp("registration_date").toLocalDateTime());
                    return registration;
                }
            }
        }
        return null;
    }

    // ====================================================================================
    // 3) READ ALL REGISTRATIONS BY USER
    // ====================================================================================
    public List<Registration> getRegistrationsByUser(int userId) throws SQLException {
        String sql = """
            SELECT tournament_id, user_id, registration_date, reg_deck
            FROM registrations
            WHERE user_id = ?
        """;

        List<Registration> registrations = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    User user = new User("");
                    user.setUserId(userId);

                    Deck deck = new Deck("", user);
                    deck.setDeckId(rs.getInt("reg_deck"));

                    // Ottieni il torneo completo
                    TournamentDAO tempDAO = new TournamentDAO(connection);
                    Tournament tournament = tempDAO.getTournamentById(rs.getInt("tournament_id"));

                    Registration registration = new Registration(tournament, user, deck);
                    registration.setRegistrationDate(rs.getTimestamp("registration_date").toLocalDateTime());
                    registrations.add(registration);
                }
            }
        }
        return registrations;
    }

    // ====================================================================================
    // 4) READ ALL REGISTRATIONS BY TOURNAMENT
    // ====================================================================================
    public List<Registration> getRegistrationsByTournament(int tournamentId) throws SQLException {
        String sql = """
            SELECT tournament_id, user_id, registration_date, reg_deck
            FROM registrations
            WHERE tournament_id = ?
        """;

        List<Registration> registrations = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    Tournament tournament = new Tournament("");
                    tournament.setTournamentId(tournamentId);

                    // Ottieni l'utente completo
                    User user = userDAO.getUserById(rs.getInt("user_id"));

                    /* equivalente a
                    User u = new User("");
                    u.setUserId(rs.getInt("user_id"));
                    */

                    Deck deck = new Deck("", user);
                    deck.setDeckId(rs.getInt("reg_deck"));


                    Registration registration = new Registration(tournament, user, deck);
                    registration.setRegistrationDate(rs.getTimestamp("registration_date").toLocalDateTime());
                    registrations.add(registration);
                }
            }
        }
        return registrations;
    }

    // ====================================================================================
    // 5) READ ALL REGISTRATIONS
    // ====================================================================================
    public List<Registration> getAllRegistrations() throws SQLException {
        String sql = """
        SELECT tournament_id, user_id, registration_date, reg_deck
        FROM registrations
    """;

        List<Registration> registrations = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Ottieni il torneo completo (senza ricaricare le registrazioni per evitare loop)
                TournamentDAO tempDAO = new TournamentDAO(connection);
                Tournament tournament = tempDAO.getTournamentById(rs.getInt("tournament_id"));
                tournament.setRegistrations(new ArrayList<>());  // Evita loop

                // Ottieni l'utente completo
                User user = userDAO.getUserById(rs.getInt("user_id"));

                Deck deck = new Deck("", user);
                deck.setDeckId(rs.getInt("reg_deck"));

                Registration registration = new Registration(tournament, user, deck);
                registration.setRegistrationDate(rs.getTimestamp("registration_date").toLocalDateTime());
                registrations.add(registration);
            }
        }
        return registrations;
    }

    // ====================================================================================
    // 6) DELETE REGISTRATION
    // ====================================================================================
    public void deleteRegistration(int tournamentId, int userId) throws SQLException {
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
    // 7) CHECK IF USER IS REGISTERED TO TOURNAMENT
    // ====================================================================================
    public boolean isUserRegistered(int tournamentId, int userId) throws SQLException {
        String sql = """
            SELECT 1
            FROM registrations
            WHERE tournament_id = ? AND user_id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}

