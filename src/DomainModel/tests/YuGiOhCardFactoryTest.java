package DomainModel.tests;

import DomainModel.GameType;
import DomainModel.card.Card;
import DomainModel.card.factory.YuGiOhCardFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YuGiOhCardFactoryTest {

    @Test
    void createCardBuildsYuGiOhCard() {
        YuGiOhCardFactory factory = new YuGiOhCardFactory();
        Card card = factory.createCard("Blue-Eyes White Dragon");

        assertEquals("Blue-Eyes White Dragon", card.getName());
        assertEquals(GameType.YUGIOH, card.getType());
    }
}
