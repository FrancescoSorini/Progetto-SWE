package DomainModel.card.factory;

import DomainModel.card.Card;
import DomainModel.card.CardType;

public class MagicCardFactory implements CardFactory {
    @Override
    public Card createCard(String name) {
        return new Card(name, CardType.MAGIC);
    }
}