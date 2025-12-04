package DomainModel.card;

import DomainModel.GameType;

public class Card {
    private int cardId;
    private String name;
    private GameType gameType;

    // COSTRUTTORI
    public Card(){}

    public Card(String name, GameType type) {
        this.name = name;
        this.gameType = type;
    }

    //GETTER
    public String getName() { return name; }
    public GameType getType() { return gameType; }
    public int getCardId(){ return cardId; }

    //SETTER
    public void setCardName(String name) { this.name = name; }
    public void setCardType(GameType type) { this.gameType = type; }
    public void setCardId(int cardId) { this.cardId = cardId; }

    /*
    Mostra testualmente la carta
    public String displayCard() {
        return "Card{" +
                "name='" + name + '\'' +
                ", type=" + gameType.getDisplayName() +
                '}';
    }
    */

    //Sovrascrivo equals per confrontare le carte in base al loro ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card card = (Card) o;
        return cardId == card.cardId;
    }
}
