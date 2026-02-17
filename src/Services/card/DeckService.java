package Services.card;

import DomainModel.GameType;
import DomainModel.card.Card;
import DomainModel.card.Deck;
import DomainModel.card.factory.CardFactory;
import DomainModel.card.factory.MagicCardFactory;
import DomainModel.card.factory.PokemonCardFactory;
import DomainModel.card.factory.YuGiOhCardFactory;
import DomainModel.user.User;
import ORM.dao.CardDAO;
import ORM.dao.DeckDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DeckService {

    private final DeckDAO deckDAO;
    private final CardDAO cardDAO;

    public DeckService(Connection connection) {
        this.deckDAO = new DeckDAO(connection);
        this.cardDAO = new CardDAO(connection);
    }

    // -------------------------------------------------------------
    // CRUD DECK
    // -------------------------------------------------------------
    public Deck createDeck(User caller, String deckName, GameType gameType) throws SQLException {
        requireCaller(caller, "Owner non valido.");

        if (deckName == null || deckName.isBlank()) {
            throw new IllegalArgumentException("Il nome del deck non puo essere vuoto.");
        }
        if (gameType == null) {
            throw new IllegalArgumentException("Il tipo di gioco non puo essere nullo.");
        }

        Deck deck = new Deck(deckName, caller, gameType);
        deckDAO.createDeck(deck);
        return deck;
    }

    public List<Deck> getMyDecks(User caller) throws SQLException {
        requireCaller(caller, "Owner non valido.");
        return deckDAO.getAllDecksByUser(caller.getUserId());
    }

    public List<Deck> getDecksByGameType(User caller, GameType gameType) throws SQLException {
        return deckDAO.getDecksByGameType(gameType);
    }

    public Deck getDeckById(int deckId) throws SQLException {
        Deck deck = deckDAO.getDeckById(deckId);
        if (deck == null) {
            throw new IllegalArgumentException("Deck non trovato");
        }
        return deck;
    }

    public void renameDeck(User caller, int deckId, String newName) throws SQLException {
        Deck deck = getDeckById(deckId);

        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Il nome del deck non puo essere vuoto.");
        }
        if (newName.equals(deck.getDeckName())) {
            throw new IllegalArgumentException("Il nuovo nome del deck deve essere diverso da quello attuale.");
        }

        List<Deck> userDecks = deckDAO.getAllDecksByUser(deck.getOwner().getUserId());
        for (Deck d : userDecks) {
            if (d.getDeckName().equals(newName) && d.getDeckId() != deckId) {
                throw new IllegalArgumentException("Hai gia un deck con questo nome.");
            }
        }

        deck.setDeckName(newName);
        deckDAO.updateDeckName(deckId, newName);
    }

    public void deleteDeck(User caller, int deckId) throws SQLException {
        getDeckById(deckId);
        deckDAO.deleteDeck(deckId);
    }

    // -------------------------------------------------------------
    // GESTIONE CARTE NEL DECK
    // -------------------------------------------------------------
    public void addCardToDeck(User caller, int deckId, String cardName) throws SQLException {
        Deck deck = getDeckById(deckId);

        if (cardName == null || cardName.isBlank()) {
            throw new IllegalArgumentException("Il nome della carta non puo essere vuoto.");
        }

        Card existing = cardDAO.getCardByName(cardName);
        if (existing != null) {
            if (existing.getType() != deck.getGameType()) {
                throw new IllegalArgumentException("La carta non e compatibile con il tipo di gioco del deck.");
            }
            deckDAO.addCardToDeck(deckId, existing.getCardId());
            return;
        }

        Card card = createCardViaFactory(cardName, deck.getGameType());
        cardDAO.addCard(card);
        deckDAO.addCardToDeck(deckId, card.getCardId());
    }

    public void removeCardFromDeck(User caller, int deckId, int cardId) throws SQLException {
        getDeckById(deckId);
        deckDAO.removeCardFromDeck(deckId, cardId);
    }

    public List<Card> getCardsInDeck(int deckId) throws SQLException {
        getDeckById(deckId);
        return deckDAO.findCardsByDeck(deckId);
    }

    private Card createCardViaFactory(String name, GameType type) {
        CardFactory factory;
        switch (type) {
            case MAGIC:
                factory = new MagicCardFactory();
                break;
            case POKEMON:
                factory = new PokemonCardFactory();
                break;
            case YUGIOH:
                factory = new YuGiOhCardFactory();
                break;
            default:
                throw new IllegalArgumentException("Unsupported game type: " + type);
        }
        return factory.createCard(name);
    }

    private void requireCaller(User caller, String message) {
        if (caller == null) {
            throw new SecurityException(message);
        }
    }
}
