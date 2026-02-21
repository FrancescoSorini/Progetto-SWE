package Services.test;

import DomainModel.GameType;
import DomainModel.card.Card;
import DomainModel.card.Deck;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import ORM.dao.CardDAO;
import ORM.dao.UserDAO;
import Services.card.DeckService;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestDeckService {

    private static Connection connection;
    private static DeckService deckService;
    private static UserDAO userDAO;
    private static CardDAO cardDAO;
    private static User owner;

    @BeforeAll
    static void init() throws Exception {
        connection = DatabaseConnection.getConnection();
        deckService = new DeckService(connection);
        userDAO = new UserDAO(connection);
        cardDAO = new CardDAO(connection);
    }

    @BeforeEach
    void resetBeforeEach() throws Exception {
        truncateAll();
        owner = new User("owner", "o@mail.com", "pass123", true, Role.PLAYER);
        userDAO.createUser(owner);
    }

    @AfterEach
    void resetAfterEach() throws Exception {
        truncateAll();
    }

    @AfterAll
    static void closeAll() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void truncateAll() throws Exception {
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
    }

    @Test
    @Order(1)
    void test01_createDeck_success() throws Exception {
        Deck created = deckService.createDeck(owner, "MyDeck", GameType.YUGIOH);
        assertTrue(created.getDeckId() > 0);
        assertEquals("MyDeck", created.getDeckName());
    }

    @Test
    @Order(2)
    void test02_createDeck_validation() {
        assertThrows(SecurityException.class, () -> deckService.createDeck(null, "D", GameType.MAGIC));
        assertThrows(IllegalArgumentException.class, () -> deckService.createDeck(owner, " ", GameType.MAGIC));
        assertThrows(IllegalArgumentException.class, () -> deckService.createDeck(owner, "D", null));
    }

    @Test
    @Order(3)
    void test03_getMyDecksAndByGameType() throws Exception {
        deckService.createDeck(owner, "Y1", GameType.YUGIOH);
        deckService.createDeck(owner, "M1", GameType.MAGIC);

        assertEquals(2, deckService.getMyDecks(owner).size());
        assertEquals(1, deckService.getDecksByGameType(owner, GameType.MAGIC).size());
        assertThrows(SecurityException.class, () -> deckService.getMyDecks(null));
    }

    @Test
    @Order(4)
    void test04_getDeckById_notFound() {
        assertThrows(IllegalArgumentException.class, () -> deckService.getDeckById(99));
    }

    @Test
    @Order(5)
    void test05_renameDeck_successAndValidation() throws Exception {
        Deck d1 = deckService.createDeck(owner, "Deck1", GameType.YUGIOH);
        deckService.createDeck(owner, "Deck2", GameType.YUGIOH);

        deckService.renameDeck(owner, d1.getDeckId(), "Deck1Renamed");
        assertEquals("Deck1Renamed", deckService.getDeckById(d1.getDeckId()).getDeckName());

        assertThrows(IllegalArgumentException.class, () -> deckService.renameDeck(owner, d1.getDeckId(), " "));
        assertThrows(IllegalArgumentException.class, () -> deckService.renameDeck(owner, d1.getDeckId(), "Deck1Renamed"));
        assertThrows(IllegalArgumentException.class, () -> deckService.renameDeck(owner, d1.getDeckId(), "Deck2"));
    }

    @Test
    @Order(6)
    void test06_addCardToDeck_existingCompatible() throws Exception {
        Deck deck = deckService.createDeck(owner, "YDeck", GameType.YUGIOH);
        Card card = new Card("Blue-Eyes", GameType.YUGIOH);
        cardDAO.addCard(card);

        deckService.addCardToDeck(owner, deck.getDeckId(), "Blue-Eyes");
        List<Card> cards = deckService.getCardsInDeck(deck.getDeckId());
        assertEquals(1, cards.size());
        assertEquals("Blue-Eyes", cards.getFirst().getName());
    }

    @Test
    @Order(7)
    void test07_addCardToDeck_existingIncompatibleOrBlank() throws Exception {
        Deck deck = deckService.createDeck(owner, "YDeck", GameType.YUGIOH);
        Card card = new Card("Pikachu", GameType.POKEMON);
        cardDAO.addCard(card);

        assertThrows(IllegalArgumentException.class,
                () -> deckService.addCardToDeck(owner, deck.getDeckId(), "Pikachu"));
        assertThrows(IllegalArgumentException.class,
                () -> deckService.addCardToDeck(owner, deck.getDeckId(), " "));
    }

    @Test
    @Order(8)
    void test08_addCardToDeck_createsNewCardViaFactory() throws Exception {
        Deck deck = deckService.createDeck(owner, "MDeck", GameType.MAGIC);

        deckService.addCardToDeck(owner, deck.getDeckId(), "Lightning Bolt");
        List<Card> cards = deckService.getCardsInDeck(deck.getDeckId());

        assertEquals(1, cards.size());
        assertEquals("Lightning Bolt", cards.getFirst().getName());
        assertEquals(GameType.MAGIC, cards.getFirst().getType());
    }

    @Test
    @Order(9)
    void test09_removeCardAndDeleteDeck() throws Exception {
        Deck deck = deckService.createDeck(owner, "Deck", GameType.YUGIOH);
        Card card = new Card("Dark Magician", GameType.YUGIOH);
        cardDAO.addCard(card);
        deckService.addCardToDeck(owner, deck.getDeckId(), "Dark Magician");

        int cardId = deckService.getCardsInDeck(deck.getDeckId()).getFirst().getCardId();
        deckService.removeCardFromDeck(owner, deck.getDeckId(), cardId);
        assertEquals(0, deckService.getCardsInDeck(deck.getDeckId()).size());

        deckService.deleteDeck(owner, deck.getDeckId());
        assertThrows(IllegalArgumentException.class, () -> deckService.getDeckById(deck.getDeckId()));
    }
}
