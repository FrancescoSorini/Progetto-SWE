package DomainModel.card.factory;

import DomainModel.card.Card;

public class MagicCardFactory implements CardFactory {
    @Override
    public Card createCard(String name) {
        return new Card(name, CardType.MAGIC);
    }
}