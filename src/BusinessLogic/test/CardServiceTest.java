package BusinessLogic.test;

import BusinessLogic.card.CardService;
import BusinessLogic.session.UserSession;
import DomainModel.card.Card;
import DomainModel.user.Role;
import DomainModel.user.User;
import DomainModel.GameType;
import ORM.connection.DatabaseConnection;
import ORM.dao.CardDAO;
import ORM.dao.UserDAO;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CardServiceTest {

    private static Connection connection;
    private static CardService cardService;
    private static CardDAO cardDAO;
    private static UserDAO userDAO;

    @BeforeAll
    static void setup() {
        connection = DatabaseConnection.getConnection();
        cardDAO = new CardDAO(connection);
        userDAO = new UserDAO(connection);
        cardService = new CardService(connection);
    }

    @BeforeEach
    void resetState() throws SQLException {
        resetCardsTable();
        resetCardIdSerial();
        resetUsersTable();
        resetUserIdSerial();
        UserSession.getInstance().logout();
    }

    // ======================
    // DATABASE RESET UTILS
    // ======================
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

    // ======================
    // ADMIN CREATION UTILS
    // ======================
    private User createAdmin(String username) throws SQLException {
        User admin = new User(username, username+"@mail.com", "1234", true, Role.ADMIN);
        userDAO.createUser(admin);
        UserSession.getInstance().login(admin);
        return admin;
    }

    private User createPlayer(String username) throws SQLException {
        return new User(username, username+"@mail.com", "1234", true, Role.PLAYER);
    }

    // ============================================================
    // 1. CREATE CARD (ADMIN)
    // ============================================================
    @Test
    @Order(1)
    void createCard_success() throws SQLException {
        createAdmin("admin1");
        Card c = new Card("Dragon", GameType.MAGIC);
        cardService.createCard(c);

        Card dbCard = cardDAO.getCardById(1);
        assertNotNull(dbCard);
        assertEquals("Dragon", dbCard.getName());
        assertEquals(GameType.MAGIC, dbCard.getType());
    }

    @Test
    @Order(2)
    void createCard_notAdmin() throws SQLException {
        UserSession.getInstance().login(createPlayer("player1"));
        Card c = new Card("Goblin", GameType.MAGIC);

        assertThrows(SecurityException.class, () -> cardService.createCard(c));
    }

    @Test
    @Order(4)
    void createCard_invalidInput() throws SQLException {
        createAdmin("admin3");

        assertThrows(IllegalArgumentException.class,
                () -> cardService.createCard(new Card("", GameType.MAGIC)));

        assertThrows(IllegalArgumentException.class,
                () -> cardService.createCard(new Card("CardX", null)));
    }

    // ============================================================
    // 2. GET CARD / SEARCH (everyone)
    // ============================================================
    @Test
    @Order(5)
    void getCardById_and_getAllCards() throws SQLException {
        createAdmin("admin4");
        Card c1 = new Card("Aqua", GameType.MAGIC);
        Card c2 = new Card("Flame", GameType.MAGIC);
        cardService.createCard(c1);
        cardService.createCard(c2);

        Card fetched = cardService.getCardById(1);
        assertEquals("Aqua", fetched.getName());

        List<Card> all = cardService.getAllCards();
        assertEquals(2, all.size());
    }

    @Test
    @Order(6)
    void getCardsByGameType() throws SQLException {
        createAdmin("admin4");
        Card c1 = new Card("Aqua", GameType.MAGIC);
        Card c2 = new Card("Flame", GameType.MAGIC);
        Card c3 = new Card("Pikachu", GameType.POKEMON);
        cardService.createCard(c1);
        cardService.createCard(c2);
        cardService.createCard(c3);

        List<Card> magicCards = cardDAO.getCardsByGameType(GameType.MAGIC);
        assertEquals(2, magicCards.size());
        assertTrue(magicCards.stream().allMatch(c -> c.getType() == GameType.MAGIC));
    }

    @Test
    @Order(7)
    void searchCardByName_success() throws SQLException {
        createAdmin("admin5");
        Card c = new Card("Earth", GameType.MAGIC);
        cardService.createCard(c);

        Card found = (Card) cardService.searchCardsByName(GameType.MAGIC, "Earth");
        assertNotNull(found);
        assertEquals("Earth", found.getName());
    }

    @Test
    @Order(8)
    void searchCardByName_invalid() {
        assertThrows(IllegalArgumentException.class,
                () -> cardService.searchCardsByName(GameType.MAGIC, ""));
    }

    // ============================================================
    // 3. UPDATE CARD (ADMIN)
    // ============================================================
    @Test
    @Order(9)
    void updateCardName_success() throws SQLException {
        createAdmin("admin6");
        Card c = new Card("Wind", GameType.MAGIC);
        cardService.createCard(c);

        cardService.updateCardName(1, "Windstorm");

        Card updated = cardDAO.getCardById(1);
        assertEquals("Windstorm", updated.getName());
    }

    @Test
    @Order(10)
    void updateCardName_duplicate() throws SQLException {
        createAdmin("admin7");
        cardService.createCard(new Card("Fire", GameType.MAGIC));
        cardService.createCard(new Card("Water", GameType.MAGIC));

        assertThrows(IllegalArgumentException.class, () -> cardService.updateCardName(2, "Fire"));
    }

    @Test
    @Order(11)
    void updateCardName_notAdmin() throws SQLException {
        UserSession.getInstance().login(createPlayer("player2"));
        assertThrows(SecurityException.class, () -> cardService.updateCardName(1, "Any"));
    }

    @Test
    @Order(12)
    void updateCardType_success() throws SQLException {
        createAdmin("admin8");
        Card c = new Card("Stone", GameType.MAGIC);
        cardService.createCard(c);

        cardService.updateCardType(1, GameType.YUGIOH);

        Card updated = cardDAO.getCardById(1);
        assertEquals(GameType.YUGIOH, updated.getType());
    }

    @Test
    @Order(13)
    void updateCardType_notAdmin() throws SQLException {
        UserSession.getInstance().login(createPlayer("player3"));
        assertThrows(SecurityException.class, () -> cardService.updateCardType(1, GameType.YUGIOH));
    }

    // ============================================================
    // 4. DELETE CARD (ADMIN)
    // ============================================================
    @Test
    @Order(14)
    void deleteCard_success() throws SQLException {
        createAdmin("admin9");
        Card c = new Card("Shadow", GameType.MAGIC);
        cardService.createCard(c);

        cardService.deleteCard(1);
        assertNull(cardDAO.getCardById(1));
    }

    @Test
    @Order(15)
    void deleteCard_notAdmin() throws SQLException {
        UserSession.getInstance().login(createPlayer("player4"));
        assertThrows(SecurityException.class, () -> cardService.deleteCard(1));
    }
}
