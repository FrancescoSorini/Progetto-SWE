package ORM.dao.test;

import ORM.dao.CardDAO;
import ORM.connection.DatabaseConnection;
import DomainModel.card.Card;
import DomainModel.GameType;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCardDAO {

    private static Connection connection;
    private static CardDAO cardDAO;

    @BeforeAll
    static void setup() throws SQLException {

        connection = DatabaseConnection.getConnection();
        if (connection == null){
            fail("Connesione db fallita");
        }
        cardDAO = new CardDAO(connection);

        //resetto il counter SERIAL prima di iniziare i test
        String sql = "TRUNCATE TABLE cards RESTART IDENTITY CASCADE";
        connection.prepareStatement(sql).executeUpdate();
    }

    @AfterAll
    static void cleanup() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                TRUNCATE TABLE cards
                RESTART IDENTITY CASCADE
            """);
        }
    }

    @Test
    @Order(1)
    void testInsertCard() throws SQLException {
        Card card = new Card("Blue-Eyes White Dragon", GameType.YUGIOH);
        cardDAO.addCard(card);

        Card loaded = cardDAO.getCardByName("Blue-Eyes White Dragon");
        assertNotNull(loaded);
        assertEquals("Blue-Eyes White Dragon", loaded.getName());
    }

    @Test
    @Order(2)
    void testGetCardById() throws SQLException {

        assertNotNull(cardDAO.getCardById(1));
        assertEquals("Blue-Eyes White Dragon", cardDAO.getCardById(1).getName());
    }

    @Test
    @Order(3)
    void testGetCardByName() throws SQLException {

        assertNotNull(cardDAO.getCardByName("Blue-Eyes White Dragon"));
        assertEquals(1, cardDAO.getCardByName("Blue-Eyes White Dragon").getCardId());
    }



    @Test
    @Order(4)
    void testGetAllCards() throws SQLException {
        List<Card> cards = cardDAO.getAllCards();
        assertTrue(cards.size() > 0, "Il database dovrebbe contenere almeno una carta");
    }

    @Test
    @Order(5)
    void testUpdateCard() throws SQLException {
        Card card = cardDAO.getCardByName("Blue-Eyes White Dragon");
        card.setCardName("Blue-Eyes Ultimate Dragon");

        cardDAO.updateCard(card);

        Card updated = cardDAO.getCardByName("Blue-Eyes Ultimate Dragon");
        assertNotNull(updated);
    }


    /*
    @Test
    @Order(6)
    void testDeleteCard() throws SQLException {
        Card card = cardDAO.getCardByName("Blue-Eyes Ultimate Dragon");
        cardDAO.deleteCard(card.getCardId());

        Card deleted = cardDAO.getCardById(card.getCardId());
        assertNull(deleted, "La carta dovrebbe essere stata eliminata");
    }
    */
}
