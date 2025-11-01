package DomainModel.card;

import java.util.List;

public class Card {
    private int cardId;
    private String cardName;
    private CardType cardType; // enum: YUGIOH, MAGIC, POKEMON
    private List<Deck> decks;  // molti-a-molti tramite DeckCard
}
