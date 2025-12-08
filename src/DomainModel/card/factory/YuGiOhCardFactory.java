package DomainModel.card.factory;

import DomainModel.card.Card;
import DomainModel.GameType;

public class YuGiOhCardFactory implements CardFactory {
    @Override
    public Card createCard(String name) {
        return new Card(name, GameType.YUGIOH);
    }
}
