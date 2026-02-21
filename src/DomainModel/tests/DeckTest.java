package DomainModel.tests;

import DomainModel.GameType;
import DomainModel.card.Card;
import DomainModel.card.Deck;
import DomainModel.user.Role;
import DomainModel.user.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeckTest {

    @Test
    void constructorWithGameTypeInitializesCards() {
        Deck deck = new Deck("MyDeck", new User("alice"), GameType.MAGIC);
        assertNotNull(deck.getCards());
        assertEquals(0, deck.getCards().size());
        assertEquals(GameType.MAGIC, deck.getGameType());
    }

    @Test
    void addRemoveAndContainsCardWork() {
        Deck deck = new Deck(1, "MyDeck", new User("alice"), new ArrayList<>(), GameType.YUGIOH);
        Card card = new Card("Dark Magician", GameType.YUGIOH);
        card.setCardId(11);

        deck.addCard(card);
        assertTrue(deck.containsCard(card));
        assertEquals(1, deck.getDeckSize());

        deck.removeCard(card);
        assertFalse(deck.containsCard(card));
        assertEquals(0, deck.getDeckSize());
    }

    @Test
    void addCardDoesNotDuplicateSameCardId() {
        Deck deck = new Deck(1, "MyDeck", new User("alice"), new ArrayList<>(), GameType.YUGIOH);
        Card first = new Card("A", GameType.YUGIOH);
        Card second = new Card("B", GameType.YUGIOH);
        first.setCardId(5);
        second.setCardId(5);

        deck.addCard(first);
        deck.addCard(second);
        assertEquals(1, deck.getDeckSize());
    }

    @Test
    void displayDeckContainsMainFields() {
        User owner = new User("alice", "a@a.com", "pwd", true, Role.PLAYER);
        Deck deck = new Deck(1, "DeckA", owner, new ArrayList<>(), GameType.POKEMON);
        Card card = new Card("Pikachu", GameType.POKEMON);
        card.setCardId(1);
        deck.addCard(card);

        String text = deck.displayDeck();
        assertTrue(text.contains("DeckA"));
        assertTrue(text.contains("alice"));
        assertTrue(text.contains("Pikachu"));
    }
}
