package DomainModel.tests;

import DomainModel.GameType;
import DomainModel.card.Card;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardTest {

    @Test
    void gettersAndSettersWork() {
        Card card = new Card();
        card.setCardId(10);
        card.setCardName("Blue-Eyes");
        card.setCardType(GameType.YUGIOH);

        assertEquals(10, card.getCardId());
        assertEquals("Blue-Eyes", card.getName());
        assertEquals(GameType.YUGIOH, card.getType());
    }

    @Test
    void equalsUsesCardId() {
        Card a = new Card("A", GameType.MAGIC);
        Card b = new Card("B", GameType.POKEMON);
        a.setCardId(7);
        b.setCardId(7);

        assertTrue(a.equals(b));
    }

    @Test
    void printCardsHandlesEmptyList() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(output));
            Card.printCards(List.of());
        } finally {
            System.setOut(original);
        }
        assertTrue(output.toString().contains("Nessuna carta trovata."));
    }
}
