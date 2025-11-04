package DomainModel.card;

import DomainModel.user.User;

import java.util.List;

public class Deck {
    private int deckId;
    private String deckName;
    private User owner;              // molti-a-uno
    private List<Card> cards;        // molti-a-molti

    //COSTRUTTORI
    public Deck() {}

    public Deck(String deckName, User owner) {
        this.deckName = deckName;
        this.owner = owner;
    }

    public Deck(int deckId, String deckName, User owner, List<Card> cards) {
        this.deckId = deckId;
        this.deckName = deckName;
        this.owner = owner;
        this.cards = cards;
    }

    //GETTER
    public int getDeckId() { return deckId; }
    public String getDeckName() { return deckName; }
    public User getOwner() { return owner; }
    public List<Card> getCards() { return cards; }

    //SETTER
    public void setDeckId(int deckId) { this.deckId = deckId; }
    public void setDeckName(String deckName) { this.deckName = deckName; }
    public void setOwner(User owner) { this.owner = owner; }
    public void setCards(List<Card> cards) { this.cards = cards; }


    //Mostra testualmente il deck
    public String displayDeck() {
        return "Deck{" +
                "deckName='" + deckName + '\'' +
                ", owner=" + owner.getUsername() +
                ", cards=" + cards.stream().map(Card::getName).toList() +
                '}';
    }

    //CRUD: aggiungi carta
    public void addCard(Card card) {
        if (card == null) return;
        if (cards.contains(card)) {
            System.out.println("Carta già presente nel deck: " + card.getName());
            return;
        }
        cards.add(card);
    }

    //CRUD: rimuovi carta
    public void removeCard(Card card) {
        cards.remove(card);
    }

    // Verifica se il deck contiene una carta specifica (ricerca per oggetto Card)
    public boolean containsCard(Card card) {
        return cards.contains(card);
    }

    // Ottieni il numero totale di carte nel deck
    public int getDeckSize() {
        return cards.size();
    }

    // Verifica se il deck è valido: nome non nullo, owner non nullo, tutte le carte dello stesso tipo
    public boolean isValid() {
        if (deckName == null || owner == null) {
            return false;
        }
        else {
            CardType firstType = cards.get(0).getType();
            for (Card card : cards) {
                if (card == null || card.getType() != firstType) {
                    return false;
                }
            }
        }
        return true;
    }

    //Sovrascrivo equals per confrontare i deck in base al loro ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Deck)) return false;
        Deck deck = (Deck) o;
        return deckId == deck.deckId;
    }

}

