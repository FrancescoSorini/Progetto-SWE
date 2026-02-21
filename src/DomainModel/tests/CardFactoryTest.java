package DomainModel.tests;

import DomainModel.GameType;
import DomainModel.card.Card;
import DomainModel.card.factory.CardFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardFactoryTest {

    @Test
    void createCardContractWorksWithImplementation() {
        CardFactory factory = name -> new Card(name, GameType.MAGIC);
        Card card = factory.createCard("Lightning Bolt");

        assertEquals("Lightning Bolt", card.getName());
        assertEquals(GameType.MAGIC, card.getType());
    }
}
