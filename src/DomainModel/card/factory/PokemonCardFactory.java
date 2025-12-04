package DomainModel.card.factory;

import DomainModel.card.Card;

public class PokemonCardFactory implements CardFactory {
    @Override
    public Card createCard(String name) {
        return new Card(name, CardType.POKEMON);
    }
}
