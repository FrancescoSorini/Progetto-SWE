package ORM.dao.test;

import ORM.dao.*;
import DomainModel.card.Card;
import DomainModel.card.CardType;
import DomainModel.card.Deck;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class testDeckDAO {

    private static Connection connection;
    private static DeckDAO deckDAO;
    private static CardDAO cardDAO;
    private static UserDAO userDAO;

    private int roleId;
    private static int userId;
    private static int cardId1;
    private static int cardId2;

    @BeforeAll
    static void init() throws Exception {
        connection = DatabaseConnection.getConnection();
    }

    @BeforeEach
    void resetDatabase() throws Exception {
        // ============= RESET DATABASE PER TEST RIPETIBILI ======================
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
            TRUNCATE TABLE registrations,
                            tournaments,
                            decks_cards,
                            decks,
                            cards,
                            users
            RESTART IDENTITY CASCADE
        """);
        }

        deckDAO = new DeckDAO(connection);
        cardDAO = new CardDAO(connection);
        userDAO = new UserDAO(connection);


        // ================== CREAZIONE DATI DI BASE =============================

        // Utente
        User u = new User("testUser");
        u.setEmail("testEmail");
        u.setPassword("testPassword");
        u.setEnabled(true);
        u.setRole(Role.PLAYER);
        userDAO.createUser(u);
        userId = u.getUserId();

        // Carte
        Card c1 = new Card("Blue-Eyes White Dragon", CardType.YUGIOH);
        cardDAO.addCard(c1);
        cardId1 = c1.getCardId();

        Card c2 = new Card("Dark Magician", CardType.YUGIOH);
        cardDAO.addCard(c2);
        cardId2 = c2.getCardId();
    }

    @AfterAll
    static void cleanupDatabase() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                                TRUNCATE TABLE registrations,
                                               tournaments,
                                               decks_cards,
                                               decks,
                                               cards,
                                               users
                                RESTART IDENTITY CASCADE
                                """);
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }


    @Test
    @Order(1)
    void testCreateDeck() throws Exception {

        User owner = userDAO.getUserById(userId);
        Deck deck = new Deck("MyDeck", owner);

        deckDAO.createDeck(deck);

        assertTrue(deck.getDeckId() > 0, "Deck ID should be generated");

        Deck loaded = deckDAO.getDeckById(deck.getDeckId());

        assertNotNull(loaded);
        assertEquals("MyDeck", loaded.getDeckName());
        assertEquals(owner.getUserId(), loaded.getOwner().getUserId());
    }


    @Test
    @Order(2)
    void testGetDeckByIdWithCards() throws Exception {

        User owner = userDAO.getUserById(userId);
        Deck deck = new Deck("YugiDeck", owner);

        deckDAO.createDeck(deck);

        deckDAO.addCardToDeck(deck.getDeckId(), cardId1);
        deckDAO.addCardToDeck(deck.getDeckId(), cardId2);

        Deck loaded = deckDAO.getDeckById(deck.getDeckId());

        assertNotNull(loaded);
        assertEquals("YugiDeck", loaded.getDeckName());
        assertEquals(2, loaded.getCards().size());
    }


    @Test
    @Order(3)
    void testGetAllDecksByUser() throws Exception {

        User owner = userDAO.getUserById(userId);

        Deck d1 = new Deck("Deck1", owner);
        Deck d2 = new Deck("Deck2", owner);
        deckDAO.createDeck(d1);
        deckDAO.createDeck(d2);

        List<Deck> decks = deckDAO.getAllDecksByUser(userId);

        assertEquals(2, decks.size());
    }


    @Test
    @Order(4)
    void testUpdateDeckName() throws Exception {

        User owner = userDAO.getUserById(userId);
        Deck deck = new Deck("OldName", owner);
        deckDAO.createDeck(deck);

        deckDAO.updateDeckName(deck.getDeckId(), "NewName");

        Deck updated = deckDAO.getDeckById(deck.getDeckId());

        assertEquals("NewName", updated.getDeckName());
    }


    @Test
    @Order(5)
    void testAddCardToDeck() throws Exception {

        User owner = userDAO.getUserById(userId);
        Deck deck = new Deck("CardTestDeck", owner);

        deckDAO.createDeck(deck);
        deckDAO.addCardToDeck(deck.getDeckId(), cardId1);

        List<Card> cards = deckDAO.findCardsByDeck(deck.getDeckId());

        assertEquals(1, cards.size());
        assertEquals(cardId1, cards.get(0).getCardId());
    }


    @Test
    @Order(6)
    void testRemoveCardFromDeck() throws Exception {

        User owner = userDAO.getUserById(userId);
        Deck deck = new Deck("RemoveTestDeck", owner);

        deckDAO.createDeck(deck);
        deckDAO.addCardToDeck(deck.getDeckId(), cardId1);

        deckDAO.removeCardFromDeck(deck.getDeckId(), cardId1);

        List<Card> cards = deckDAO.findCardsByDeck(deck.getDeckId());

        assertEquals(0, cards.size());
    }


    @Test
    @Order(7)
    void testDeleteDeck() throws Exception {

        User owner = userDAO.getUserById(userId);
        Deck deck = new Deck("TempDeck", owner);

        deckDAO.createDeck(deck);
        deckDAO.addCardToDeck(deck.getDeckId(), cardId1);

        deckDAO.deleteDeck(deck.getDeckId());

        Deck deleted = deckDAO.getDeckById(deck.getDeckId());
        assertNull(deleted);

        List<Card> cards = deckDAO.findCardsByDeck(deck.getDeckId());
        assertEquals(0, cards.size());
    }
}
