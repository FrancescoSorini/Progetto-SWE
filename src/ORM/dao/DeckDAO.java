package ORM.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import DomainModel.card.Deck;
import DomainModel.card.Card;
import DomainModel.user.User;
import DomainModel.GameType;


public class DeckDAO {
    private final Connection connection;

    public DeckDAO(Connection connection) {
        this.connection = connection;
    }

    // ====================================================================================
    // 1) CREATE DECK
    // ====================================================================================
    public void createDeck(Deck deck) throws SQLException {
        String sql = """
            INSERT INTO decks (deck_name, user_id, tcg_id)
            VALUES (?, ?, ?)
            """;
        try (PreparedStatement ps = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, deck.getDeckName());
            ps.setInt(2, deck.getOwner().getUserId());
            ps.setInt(3, deck.getGameType().getGameId()); // collegamento GameType
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    deck.setDeckId(rs.getInt(1));
                }
            }
        }
    }

    // ====================================================================================
    // 2) READ by id
    // ====================================================================================
    public Deck getDeckById(int deckId) throws SQLException {
        String sql = """
            SELECT d.deck_id, d.deck_name, d.tcg_id, u.user_id, u.username, c.card_id, c.card_name, c.tcg_id 
            FROM decks d
            JOIN users u ON d.user_id = u.user_id
            LEFT JOIN decks_cards dc ON d.deck_id = dc.deck_id
            LEFT JOIN cards c ON dc.card_id = c.card_id
            WHERE d.deck_id = ?
            """;

        Deck deck = null;
        User owner = null;
        List<Card> cards = new ArrayList<>();

        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setInt(1, deckId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (deck == null) {
                        // Crea owner una volta
                        owner = new User(rs.getString("username"));
                        owner.setUserId(rs.getInt("user_id"));

                        // Crea deck con lista vuota inizialmente
                        deck = new Deck(rs.getInt("deck_id"),
                                        rs.getString("deck_name"),
                                        owner,
                                        new ArrayList<>(),
                                        GameType.fromId(rs.getInt("tcg_id")) // collegamento GameType
                        );
                    }

                    // Aggiungi carte se presenti (fuori dall'if, per tutte le righe)
                    if (rs.getObject("card_id") != null) {  // Gestisce LEFT JOIN null
                        Card card = new Card();
                        card.setCardId(rs.getInt("card_id"));
                        card.setCardName(rs.getString("card_name"));
                        card.setCardType(GameType.fromId(rs.getInt("tcg_id"))); // collegamento GameType

                        if (!cards.contains(card)) {  // Evita duplicati
                            cards.add(card);
                        }
                    }
                }
            }
        }

        if (deck != null) {
            deck.setCards(cards);
        }
        return deck;
    }

    // ====================================================================================
    // 3) READ all decks by user
    // ====================================================================================
    public List<Deck> getAllDecksByUser(int userId) throws SQLException {
        List<Deck> decks = new ArrayList<>();
        String sql = """
            SELECT d.deck_id, d.deck_name, d.tcg_id, u.user_id, u.username 
            FROM decks d
            JOIN users u ON d.user_id = u.user_id
            WHERE d.user_id = ?
            """;

        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User owner = new User(rs.getString("username"));
                    owner.setUserId(rs.getInt("user_id"));
                    Deck deck = new Deck(rs.getInt("deck_id"),
                                         rs.getString("deck_name"),
                                         owner,
                                         new ArrayList<>(),
                                         GameType.fromId(rs.getInt("tcg_id")) // collegamento GameType
                    );  // Carte vuote
                    decks.add(deck);
                }
            }
        }
        return decks;
    }

    // ====================================================================================
    // 4) UPDATE NAME
    // ====================================================================================
    public void updateDeckName(int deckId, String newName) throws SQLException {
        String sql = """
            UPDATE decks
            SET deck_name = ?
            WHERE deck_id = ?
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setInt(2, deckId);
            ps.executeUpdate();
        }
    }

    // ====================================================================================
    // 5) DELETE
    // ====================================================================================
    public void deleteDeck(int deckId) throws SQLException {
        String deleteRelations = """
            DELETE
            FROM decks_cards
            WHERE deck_id = ?
            """;
        String deleteDeck = """
            DELETE
            FROM decks
            WHERE deck_id = ?
            """;

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement ps1 = connection.prepareStatement(deleteRelations);
                 PreparedStatement ps2 = connection.prepareStatement(deleteDeck)) {

                ps1.setInt(1, deckId);
                ps1.executeUpdate();

                ps2.setInt(1, deckId);
                ps2.executeUpdate();

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // ====================================================================================
    // 6) ADD CARD TO DECK
    // ====================================================================================
    public void addCardToDeck(int deckId, int cardId) throws SQLException {
        String sql = """
            INSERT INTO decks_cards (deck_id, card_id) VALUES (?, ?)
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, deckId);
            ps.setInt(2, cardId);
            ps.executeUpdate();
        }
    }

    // ====================================================================================
    // 7) DELETE CARD FROM DECK
    // ====================================================================================
    public void removeCardFromDeck(int deckId, int cardId) throws SQLException {
        String sql = """
            DELETE
            FROM decks_cards
            WHERE deck_id = ? AND card_id = ?
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, deckId);
            ps.setInt(2, cardId);
            ps.executeUpdate();
        }
    }

    // ====================================================================================
    // 8) READ CARDS IN DECK
    // ====================================================================================
    public List<Card> findCardsByDeck(int deckId) throws SQLException {
        String sql = """
        SELECT c.card_id, c.card_name, c.tcg_id
        FROM cards c
        JOIN decks_cards dc ON c.card_id = dc.card_id
        WHERE dc.deck_id = ?
    """;
        List<Card> cards = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, deckId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Card card = new Card();
                    card.setCardId(rs.getInt("card_id"));
                    card.setCardName(rs.getString("card_name"));
                    card.setCardType(GameType.fromId(rs.getInt("tcg_id"))); // collegamento GameType
                    cards.add(card);
                }
            }
        }
        return cards;
    }
}
