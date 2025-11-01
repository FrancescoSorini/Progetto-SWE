package DomainModel.card;

import DomainModel.user.User;

import java.util.List;

public class Deck {
    private int deckId;
    private String deckName;
    private User owner;              // molti-a-uno
    private List<Card> cards;        // molti-a-molti
}
