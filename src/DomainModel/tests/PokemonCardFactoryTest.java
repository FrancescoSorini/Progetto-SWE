package DomainModel.tests;

import DomainModel.GameType;
import DomainModel.card.Card;
import DomainModel.card.factory.PokemonCardFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PokemonCardFactoryTest {

    @Test
    void createCardBuildsPokemonCard() {
        PokemonCardFactory factory = new PokemonCardFactory();
        Card card = factory.createCard("Charizard");

        assertEquals("Charizard", card.getName());
        assertEquals(GameType.POKEMON, card.getType());
    }
}
