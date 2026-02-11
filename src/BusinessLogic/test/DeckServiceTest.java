package BusinessLogic.test;


import BusinessLogic.card.DeckService;
import BusinessLogic.session.UserSession;
import DomainModel.card.Card;
import DomainModel.card.Deck;
import DomainModel.user.Role;
import DomainModel.user.User;
import DomainModel.GameType;
import ORM.connection.DatabaseConnection;
import ORM.dao.CardDAO;
import ORM.dao.DeckDAO;
import ORM.dao.UserDAO;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeckServiceTest {

    private static Connection connection;
    private static DeckService deckService;
    private static DeckDAO deckDAO;
    private static CardDAO cardDAO;
    private static UserDAO userDAO;

    @BeforeAll
    static void setup() {
        connection = DatabaseConnection.getConnection();
        deckDAO = new DeckDAO(connection);
        cardDAO = new CardDAO(connection);
        userDAO = new UserDAO(connection);
        deckService = new DeckService(connection);
    }

    @BeforeEach
    void resetState() throws SQLException {
        resetDecksTable();
        resetDeckIdSerial();
        resetCardsTable();
        resetCardIdSerial();
        resetUsersTable();
        resetUserIdSerial();
        UserSession.getInstance().logout();
    }

    @AfterAll
    public static void cleanDatabase() throws SQLException {
        // Disabiliti momentaneamente le foreign key (PostgreSQL usa "SET CONSTRAINTS ALL DEFERRED" oppure si cancella nell'ordine corretto)
        try (Statement stmt = connection.createStatement()) {

            // 1) Rimuovi tutte le relazioni deck-card
            stmt.executeUpdate("DELETE FROM decks_cards");

            // 2) Rimuovi tutti i deck
            stmt.executeUpdate("DELETE FROM decks");

            // 3) Rimuovi eventuali carte di test create
            stmt.executeUpdate("DELETE FROM cards");

            // 4) Rimuovi eventuali utenti di test
            stmt.executeUpdate("DELETE FROM users");

            //connection.commit();
        }
    }

    // =======================================
    // DATABASE RESET UTILS
    // =======================================
    private void resetDecksTable() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM decks")) {
            ps.executeUpdate();
        }
    }

    private void resetDeckIdSerial() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "ALTER SEQUENCE decks_deck_id_seq RESTART WITH 1")) {
            ps.executeUpdate();
        }
    }

    private void resetCardsTable() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM cards")) {
            ps.executeUpdate();
        }
    }

    private void resetCardIdSerial() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "ALTER SEQUENCE cards_card_id_seq RESTART WITH 1")) {
            ps.executeUpdate();
        }
    }

    private void resetUsersTable() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM users")) {
            ps.executeUpdate();
        }
    }

    private void resetUserIdSerial() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "ALTER SEQUENCE users_user_id_seq RESTART WITH 1")) {
            ps.executeUpdate();
        }
    }

    // =======================================
    // HELPERS: CREAZIONE ADMIN / PLAYER
    // =======================================
    private User createAdmin(String username) throws SQLException {
        User admin = new User(username, username+"@mail.com", "1234", true, Role.ADMIN);
        userDAO.createUser(admin);
        UserSession.getInstance().login(admin);
        return admin;
    }

    private User createPlayer(String username) throws SQLException {
        User player = new User(username, username+"@mail.com", "1234", true, Role.PLAYER);
        userDAO.createUser(player);
        UserSession.getInstance().login(player);
        return player;
    }

    private Card createCard(String name, GameType type) throws SQLException {
        Card card = new Card(name, type);
        cardDAO.addCard(card);
        return card;
    }

    // ====================================================
    // 1. CREATE DECK
    // ====================================================
    @Test
    @Order(1)
    void createDeck_success() throws SQLException {
        User u = createPlayer("player1");
        Deck deck = deckService.createDeck("MyDeck", GameType.MAGIC);
        assertNotNull(deck);
        assertEquals("MyDeck", deck.getDeckName());
        assertEquals(u.getUserId(), deck.getOwner().getUserId());
    }

    @Test
    @Order(2)
    void createDeck_notLoggedIn() {
        assertThrows(SecurityException.class, () -> deckService.createDeck("NoLoginDeck", GameType.POKEMON));
    }

    // ====================================================
    // 2. GET MY DECKS
    // ====================================================
    @Test
    @Order(3)
    void getMyDecks_success() throws SQLException {
        User u = createPlayer("player2");
        deckService.createDeck("Deck1", GameType.YUGIOH);
        deckService.createDeck("Deck2", GameType.MAGIC);

        List<Deck> decks = deckService.getMyDecks();
        assertEquals(2, decks.size());
    }

    @Test
    @Order(4)
    void getMyDecks_notLoggedIn() {
        assertThrows(SecurityException.class, () -> deckService.getMyDecks());
    }

    @Test
    @Order(5)
    void getDecksByGameType() throws SQLException {
        User u = createPlayer("player3");
        deckService.createDeck("DeckPokemon", GameType.POKEMON);
        deckService.createDeck("DeckMagic", GameType.MAGIC);

        List<Deck> pokemonDecks = deckService.getDecksByGameType(GameType.POKEMON);
        assertEquals(1, pokemonDecks.size());
        assertEquals("DeckPokemon", pokemonDecks.get(0).getDeckName());

        List<Deck> magicDecks = deckService.getDecksByGameType(GameType.MAGIC);
        assertEquals(1, magicDecks.size());
        assertEquals("DeckMagic", magicDecks.get(0).getDeckName());
    }

    // ====================================================
    // 3. RENAME DECK
    // ====================================================
    @Test
    @Order(6)
    void renameDeck_success() throws SQLException {
        User u = createPlayer("player3");
        Deck deck = deckService.createDeck("OldName", GameType.POKEMON);

        deckService.renameDeck(deck.getDeckId(), "NewName");

        Deck updated = deckService.getDeckById(deck.getDeckId());
        assertEquals("NewName", updated.getDeckName());
    }

    @Test
    @Order(7)
    void renameDeck_notOwner() throws SQLException {
        User u1 = createPlayer("player4");
        Deck deck = deckService.createDeck("Deck4", GameType.MAGIC);
        User u2 = createPlayer("player5");

        assertThrows(SecurityException.class,
                () -> deckService.renameDeck(deck.getDeckId(), "IllegalRename"));
    }

    @Test
    @Order(8)
    void renameDeck_invalidName() throws SQLException {
        User u = createPlayer("player6");
        Deck deck = deckService.createDeck("Deck6", GameType.YUGIOH);

        assertThrows(IllegalArgumentException.class,
                () -> deckService.renameDeck(deck.getDeckId(), ""));
        assertThrows(IllegalArgumentException.class,
                () -> deckService.renameDeck(deck.getDeckId(), "Deck6")); // stesso nome
    }

    // ====================================================
    // 4. DELETE DECK
    // ====================================================
    @Test
    @Order(9)
    void deleteDeck_success() throws SQLException {
        User u = createPlayer("player7");
        Deck deck = deckService.createDeck("DeckToDelete", GameType.POKEMON);

        deckService.deleteDeck(deck.getDeckId());
        assertThrows(IllegalArgumentException.class, () -> deckService.getDeckById(deck.getDeckId()));
    }

    @Test
    @Order(10)
    void deleteDeck_notOwner() throws SQLException {
        User u1 = createPlayer("player8");
        Deck deck = deckService.createDeck("DeckNotOwner", GameType.MAGIC);
        User u2 = createPlayer("player9");

        assertThrows(SecurityException.class, () -> deckService.deleteDeck(deck.getDeckId()));
    }

    // ====================================================
    // 5. ADD CARD TO DECK
    // ====================================================
    @Test
    @Order(11)
    void addCardToDeck_success() throws SQLException {
        User u = createPlayer("player10");
        Deck deck = deckService.createDeck("DeckWithCard", GameType.MAGIC);
        Card card = createCard("Card1", deck.getGameType());

        deckService.addCardToDeck(deck.getDeckId(), card.getName());
        List<Card> cards = deckService.getCardsInDeck(deck.getDeckId());
        assertEquals(1, cards.size());
        assertEquals(card.getName(), cards.get(0).getName());
    }

    @Test
    @Order(12)
    void addCardToDeck_wrongType() throws SQLException {
        User u = createPlayer("player11");
        Deck deck = deckService.createDeck("DeckWrongType", GameType.POKEMON);
        Card card = createCard("CardWrong", GameType.YUGIOH); // tipo incompatibile

        assertThrows(IllegalArgumentException.class,
                () -> deckService.addCardToDeck(deck.getDeckId(), card.getName()));
    }

    @Test
    @Order(13)
    void addCardToDeck_notOwner() throws SQLException {
        User u1 = createPlayer("player12");
        Deck deck = deckService.createDeck("DeckNotOwnerCard", GameType.YUGIOH);
        Card card = createCard("Card2", deck.getGameType());
        User u2 = createPlayer("player13");

        assertThrows(SecurityException.class,
                () -> deckService.addCardToDeck(deck.getDeckId(), card.getName()));
    }

    // ====================================================
    // 6. REMOVE CARD FROM DECK
    // ====================================================
    @Test
    @Order(14)
    void removeCardFromDeck_success() throws SQLException {
        User u = createPlayer("player14");
        Deck deck = deckService.createDeck("DeckRemoveCard", GameType.MAGIC);
        Card card = createCard("Card3", deck.getGameType());
        deckService.addCardToDeck(deck.getDeckId(), card.getName());

        deckService.removeCardFromDeck(deck.getDeckId(), card.getCardId());
        List<Card> cards = deckService.getCardsInDeck(deck.getDeckId());
        assertTrue(cards.isEmpty());
    }

    @Test
    @Order(15)
    void removeCardFromDeck_notOwnerOrAdmin() throws SQLException {
        User u1 = createPlayer("player15");
        Deck deck = deckService.createDeck("DeckRemoveIllegal", GameType.POKEMON);
        Card card = createCard("Card4", deck.getGameType());
        deckService.addCardToDeck(deck.getDeckId(), card.getName());

        User u2 = createPlayer("player16");
        assertThrows(SecurityException.class,
                () -> deckService.removeCardFromDeck(deck.getDeckId(), card.getCardId()));
    }
}
