package DomainModel.card;

import java.util.List;

public class Card {
    private int cardId;
    private String name;
    private CardType type; // enum: YUGIOH, MAGIC, POKEMON
    //private List<Deck> decks;  // molti-a-molti tramite DeckCard

    public Card(String name, CardType type) {
        this.name = name;
        this.type = type;
    }
    public String getName() {
        return name;
    }

    public CardType getType() {
        return type;
    }

    public String displayCard() {
        return "Card{" +
                "name='" + name + '\'' +
                ", type=" + type.getDisplayName() +
                '}';
    }
}
