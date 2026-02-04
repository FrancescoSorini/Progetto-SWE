package BusinessLogic.card;

import DomainModel.GameType;
import DomainModel.card.*;
import DomainModel.user.*;
import BusinessLogic.session.UserSession;
import ORM.dao.CardDAO;
import ORM.dao.DeckDAO;

import DomainModel.card.factory.CardFactory;
import DomainModel.card.factory.MagicCardFactory;
import DomainModel.card.factory.PokemonCardFactory;
import DomainModel.card.factory.YuGiOhCardFactory;

import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;
/**
 * Service per la gestione dei Deck.
 * Applica logiche di controllo su ruoli e ownership.
 */
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

    /**
     * Crea un nuovo deck per lâ€™utente loggato.
     */
    public Deck createDeck(String deckName, GameType gameType) throws SQLException {
        if (!UserSession.isLoggedIn()) {
            throw new SecurityException("Devi essere loggato per creare un deck.");
        }

        if (deckName == null || deckName.isBlank()) {
            throw new IllegalArgumentException("Il nome del deck non può essere vuoto.");
        }

        if (gameType == null) {
            throw new IllegalArgumentException("Il tipo di gioco non può essere nullo.");
        }

        User current = UserSession.getCurrentUser();

        Deck deck = new Deck(deckName, current, gameType);

        deckDAO.createDeck(deck);
        return deck;
    }

    /**
     * Restituisce tutti i deck dellâ€™utente loggato.
     */
    public List<Deck> getMyDecks() throws SQLException {
        if (!UserSession.isLoggedIn()) {
            throw new SecurityException("Devi essere loggato.");
        }
        return deckDAO.getAllDecksByUser(UserSession.getUserId());
    }

    /**
     * Restituisce un deck
     */
    public Deck getDeckById(int deckId) throws SQLException {
        Deck deck = deckDAO.getDeckById(deckId);

        if (deck == null) {
            throw new IllegalArgumentException("Deck non trovato");
        }

        return deck;
    }

    /**
     * Modifica il nome del deck (solo owner o admin).
     */
    public void renameDeck(int deckId, String newName) throws SQLException {
        Deck deck = getDeckById(deckId);

        if (!isOwnerOrAdmin(deck.getOwner().getUserId())) {
            throw new SecurityException("Non hai permessi per visualizzare questo deck.");
        }


        if(newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Il nome del deck non puÃ² essere vuoto.");
        }

        if(newName.equals(deck.getDeckName())) {
            throw new IllegalArgumentException("Il nuovo nome del deck deve essere diverso da quello attuale.");
        }

        // Controlla che il nuovo nome non sia giÃ  usato dallo stesso utente per un altro deck
        List<Deck> userDecks = deckDAO.getAllDecksByUser(deck.getOwner().getUserId());
        for(Deck d : userDecks) {
            if(d.getDeckName().equals(newName) && d.getDeckId() != deckId) {
                throw new IllegalArgumentException("Hai giÃ  un deck con questo nome.");
            }
        }

        deck.setDeckName(newName);
        deckDAO.updateDeckName(deckId, newName);
    }

    /**
     * Cancella il deck (solo owner o admin).
     */
    public void deleteDeck(int deckId) throws SQLException {
        Deck deck = getDeckById(deckId);

        if (!isOwnerOrAdmin(deck.getOwner().getUserId())) {
            throw new SecurityException("Non hai permessi per visualizzare questo deck.");
        }

        deckDAO.deleteDeck(deckId);
    }

    // -------------------------------------------------------------
    // GESTIONE CARTE NEL DECK
    // -------------------------------------------------------------

    /**
     * Aggiunge una carta al deck creando/riusando la carta via Factory (solo owner).
     */
    public void addCardToDeck(int deckId, String cardName) throws SQLException {
        Deck deck = getDeckById(deckId);

        if (!isOwner(deck.getOwner().getUserId())) {
            throw new SecurityException("Non hai permessi per modificare questo deck.");
        }

        if (cardName == null || cardName.isBlank()) {
            throw new IllegalArgumentException("Il nome della carta non puÃ² essere vuoto.");
        }

        Card existing = cardDAO.getCardByName(cardName);
        if (existing != null) {
            if (existing.getType() != deck.getGameType()) {
                throw new IllegalArgumentException("La carta non Ã¨ compatibile con il tipo di gioco del deck.");
            }
            deckDAO.addCardToDeck(deckId, existing.getCardId());
            return;
        }

        Card card = createCardViaFactory(cardName, deck.getGameType());
        cardDAO.addCard(card);
        deckDAO.addCardToDeck(deckId, card.getCardId());
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

    /**
     * Rimuove una carta da un deck (solo owner o admin).
     */
    public void removeCardFromDeck(int deckId, int cardId) throws SQLException {
        Deck deck = getDeckById(deckId);

        if (!isOwnerOrAdmin(deck.getOwner().getUserId())) {
            throw new SecurityException("Non hai permessi per visualizzare questo deck.");
        }

        deckDAO.removeCardFromDeck(deckId, cardId);
    }

    /**
     * Ottiene tutte le carte presenti nel deck.
     */
    public List<Card> getCardsInDeck(int deckId) throws SQLException {
        Deck deck = getDeckById(deckId);
        return deckDAO.findCardsByDeck(deckId);
    }

    // -------------------------------------------------------------
    // METODI DI SUPPORTO
    // -------------------------------------------------------------

    private boolean isOwnerOrAdmin(int ownerUserId) {
        int loggedId = UserSession.getUserId();
        return loggedId == ownerUserId || UserSession.isAdmin();
    }

    private boolean isOwner(int ownerUserId) {
        int loggedId = UserSession.getUserId();
        return loggedId == ownerUserId;
    }
}
