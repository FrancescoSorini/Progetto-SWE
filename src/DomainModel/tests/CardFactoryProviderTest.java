package DomainModel.tests;

import DomainModel.GameType;
import DomainModel.card.factory.CardFactory;
import DomainModel.card.factory.CardFactoryProvider;
import DomainModel.card.factory.MagicCardFactory;
import DomainModel.card.factory.PokemonCardFactory;
import DomainModel.card.factory.YuGiOhCardFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CardFactoryProviderTest {

    @Test
    void returnsExpectedFactoryByGameType() {
        CardFactory magic = CardFactoryProvider.getFactory(GameType.MAGIC);
        CardFactory pokemon = CardFactoryProvider.getFactory(GameType.POKEMON);
        CardFactory yugioh = CardFactoryProvider.getFactory(GameType.YUGIOH);

        assertInstanceOf(MagicCardFactory.class, magic);
        assertInstanceOf(PokemonCardFactory.class, pokemon);
        assertInstanceOf(YuGiOhCardFactory.class, yugioh);
    }
}
