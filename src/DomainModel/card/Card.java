package DomainModel.card;

import java.util.List;

public class Card {
    private int cardId;
    private String name;
    private CardType type; // enum: YUGIOH, MAGIC, POKEMON
    //private List<Deck> decks;  // molti-a-molti tramite DeckCard

    // COSTRUTTORI
    public Card(){}

    public Card(String name, CardType type) {
        this.name = name;
        this.type = type;
    }

    //GETTER
    public String getName() { return name; }
    public CardType getType() { return type; }
    public int getCardId(){ return cardId; }

    //SETTER
    public void setCardName(String name) { this.name = name; }
    public void setCardType(CardType type) { this.type = type; }
    public void setCardId(int cardId) { this.cardId = cardId; }

    //Mostra testualmente la carta
    public String displayCard() {
        return "Card{" +
                "name='" + name + '\'' +
                ", type=" + type.getDisplayName() +
                '}';
    }

    //Sovrascrivo equals per confrontare le carte in base al loro ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card card = (Card) o;
        return cardId == card.cardId;
    }
}
