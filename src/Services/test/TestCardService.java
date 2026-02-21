package Services.test;

import DomainModel.GameType;
import DomainModel.card.Card;
import DomainModel.user.Role;
import DomainModel.user.User;
import ORM.connection.DatabaseConnection;
import Services.card.CardService;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCardService {

    private static Connection connection;
    private static CardService cardService;
    private static User caller;

    @BeforeAll
    static void init() throws Exception {
        connection = DatabaseConnection.getConnection();
        cardService = new CardService(connection);
        caller = new User("admin", "a@mail.com", "pass123", true, Role.ADMIN);
    }

    @BeforeEach
    void resetBeforeEach() throws Exception {
        truncateAll();
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
    void test01_createCard_success() throws Exception {
        cardService.createCard(caller, new Card("Blue-Eyes", GameType.YUGIOH));
        Card loaded = cardService.getCardById(1);
        assertNotNull(loaded);
        assertEquals("Blue-Eyes", loaded.getName());
        assertEquals(GameType.YUGIOH, loaded.getType());
    }

    @Test
    @Order(2)
    void test02_createCard_invalidData() {
        assertThrows(IllegalArgumentException.class, () -> cardService.createCard(caller, new Card(" ", GameType.MAGIC)));
        assertThrows(IllegalArgumentException.class, () -> cardService.createCard(caller, new Card("X", null)));
    }

    @Test
    @Order(3)
    void test03_createCard_duplicateName() throws Exception {
        cardService.createCard(caller, new Card("Pikachu", GameType.POKEMON));
        assertThrows(IllegalArgumentException.class,
                () -> cardService.createCard(caller, new Card("Pikachu", GameType.POKEMON)));
    }

    @Test
    @Order(4)
    void test04_getCardsAndDuplicateCheck() throws Exception {
        cardService.createCard(caller, new Card("M1", GameType.MAGIC));
        cardService.createCard(caller, new Card("M2", GameType.MAGIC));
        cardService.createCard(caller, new Card("Y1", GameType.YUGIOH));

        assertEquals(3, cardService.getAllCards().size());
        assertEquals(2, cardService.getCardsByGameType(GameType.MAGIC).size());
        assertTrue(cardService.isDuplicate("M1"));
        assertFalse(cardService.isDuplicate("NONE"));
        assertThrows(IllegalArgumentException.class, () -> cardService.getCardsByGameType(null));
    }

    @Test
    @Order(5)
    void test05_searchCardsByName() throws Exception {
        cardService.createCard(caller, new Card("Dark Magician", GameType.YUGIOH));
        cardService.createCard(caller, new Card("Magician of Faith", GameType.YUGIOH));
        cardService.createCard(caller, new Card("Pikachu", GameType.POKEMON));

        List<Card> results = cardService.searchCardsByName(GameType.YUGIOH, "magician");
        assertEquals(2, results.size());
        assertThrows(IllegalArgumentException.class, () -> cardService.searchCardsByName(GameType.YUGIOH, " "));
    }

    @Test
    @Order(6)
    void test06_updateCardName() throws Exception {
        cardService.createCard(caller, new Card("OldName", GameType.MAGIC));
        cardService.updateCardName(caller, 1, "NewName");
        assertEquals("NewName", cardService.getCardById(1).getName());
    }

    @Test
    @Order(7)
    void test07_updateCardName_validation() throws Exception {
        cardService.createCard(caller, new Card("A", GameType.MAGIC));
        cardService.createCard(caller, new Card("B", GameType.MAGIC));

        assertThrows(IllegalArgumentException.class, () -> cardService.updateCardName(caller, 1, " "));
        assertThrows(IllegalArgumentException.class, () -> cardService.updateCardName(caller, 1, "B"));
        assertThrows(IllegalArgumentException.class, () -> cardService.updateCardName(caller, 999, "X"));
    }

    @Test
    @Order(8)
    void test08_updateCardTypeAndDelete() throws Exception {
        cardService.createCard(caller, new Card("Swap", GameType.MAGIC));

        cardService.updateCardType(caller, 1, GameType.POKEMON);
        assertEquals(GameType.POKEMON, cardService.getCardById(1).getType());
        assertThrows(IllegalArgumentException.class, () -> cardService.updateCardType(caller, 1, null));
        assertThrows(IllegalArgumentException.class, () -> cardService.updateCardType(caller, 999, GameType.MAGIC));

        cardService.deleteCard(caller, 1);
        assertNull(cardService.getCardById(1));
    }
}
