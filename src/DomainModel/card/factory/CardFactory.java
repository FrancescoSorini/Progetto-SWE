package DomainModel.card.factory;

import DomainModel.card.Card;

public interface CardFactory {
    Card createCard(String name);
}
