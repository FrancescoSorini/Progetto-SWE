package DomainModel.tests;

import DomainModel.GameType;
import DomainModel.card.Card;
import DomainModel.card.factory.MagicCardFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MagicCardFactoryTest {

    @Test
    void createCardBuildsMagicCard() {
        MagicCardFactory factory = new MagicCardFactory();
        Card card = factory.createCard("Counterspell");

        assertEquals("Counterspell", card.getName());
        assertEquals(GameType.MAGIC, card.getType());
    }
}
