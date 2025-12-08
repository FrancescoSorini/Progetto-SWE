package DomainModel.card.factory;

import DomainModel.card.Card;
import DomainModel.GameType;

public class MagicCardFactory implements CardFactory {
    @Override
    public Card createCard(String name) {
        return new Card(name, GameType.MAGIC);
    }
}