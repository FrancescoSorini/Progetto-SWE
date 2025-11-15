package ORM.dao;

import ORM.connection.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import DomainModel.card.Card;
import DomainModel.card.CardType;


public class CardDAO {
    private final Connection connection;

    public CardDAO(Connection connection) {
        this.connection = connection;
    }

    // ====================================================================================
    // 1) CREATE
    // ====================================================================================
    public void addCard(Card card) throws SQLException {
        String sql = " INSERT INTO cards (card_name, tcg_id) VALUES (?, ?)";
        try(PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, card.getName());
            stmt.setInt(2, card.getType().getId()); // collegamento a CardType
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    card.setCardId(rs.getInt(1));
                }
            }
        }
    }

    // ====================================================================================
    // 2) READ by Id
    // ====================================================================================
    public Card getCardById(int id) throws SQLException{
        String sql = """ 
            SELECT card_id, card_name, tcg_id 
            FROM cards 
            WHERE card_id = ? 
            """;
        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try(ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Card card = new Card();
                    card.setCardId(rs.getInt("card_id"));
                    card.setCardName(rs.getString("card_name"));
                    card.setCardType(CardType.values()[rs.getInt("tcg_id") -1]); // collegamento a CardType
                    return card;
                }
            }
        }
        return null; // Carta non trovata
    }

    // ====================================================================================
    // 3) READ by name
    // ====================================================================================
    public Card getCardByName(String name) throws SQLException {
        String sql = """
            SELECT card_id, card_name, tcg_id 
            FROM cards 
            WHERE card_name = ?
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            try(ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Card card = new Card();
                    card.setCardId(rs.getInt("card_id"));
                    card.setCardName(rs.getString("card_name"));
                    card.setCardType(CardType.values()[rs.getInt("tcg_id") -1]); // collegamento a CardType
                    return card;
                }
            }
        }
        return null; // Carta non trovata
    }

    // ====================================================================================
    // 4) READ all
    // ====================================================================================
    public List<Card> getAllCards() throws SQLException {
        List<Card> cards = new ArrayList<>();

        String sql = """
            SELECT card_id, card_name, tcg_id 
            FROM cards
            """;
        try(
                PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Card card = new Card();
                card.setCardId(rs.getInt("card_id"));
                card.setCardName(rs.getString("card_name"));
                card.setCardType(CardType.values()[rs.getInt("tcg_id")-1]); // collegamento a CardType
                cards.add(card);
            }
        }
        return cards;
    }

    // ====================================================================================
    // 5) UPDATE
    // ====================================================================================
    public void updateCard(Card card) throws SQLException {
        String sql = """
            UPDATE cards SET card_name = ?, tcg_id = ? 
            WHERE card_id = ?
            """;
        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, card.getName());
            stmt.setInt(2, card.getType().getId()); // collegamento a CardType
            stmt.setInt(3, card.getCardId());
            stmt.executeUpdate();
        }
    }

    // ====================================================================================
    // 6) DELETE
    // ====================================================================================
    public void deleteCard(int id) throws SQLException {
        String sql = """
            DELETE 
            FROM cards 
            WHERE card_id = ?
            """;
        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

}
