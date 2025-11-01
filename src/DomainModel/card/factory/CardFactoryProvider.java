//TODO: Approfondisci (Un piccolo Factory of Factories, utile per scegliere dinamicamente quale fabbrica usare.)
package DomainModel.card.factory;

import DomainModel.card.CardType;

public class CardFactoryProvider {

    public static CardFactory getFactory(CardType type) {
        switch (type) {
            case YUGIOH:
                return new YuGiOhCardFactory();
            case MAGIC:
                return new MagicCardFactory();
            case POKEMON:
                return new PokemonCardFactory();
            default:
                throw new IllegalArgumentException("Unsupported CardType: " + type);
        }
    }
}

/*
Usage Example:

CardFactory factory = CardFactoryProvider.getFactory(CardType.YUGIOH);
Card c = factory.createCard("Dark Magician");
System.out.println(c);
 */
