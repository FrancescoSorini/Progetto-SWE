package BusinessLogic.card;

import DomainModel.card.*;
import DomainModel.user.*;
import BusinessLogic.session.UserSession;
import ORM.dao.CardDAO;
import ORM.dao.DeckDAO;

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
     * Crea un nuovo deck per l’utente loggato.
     */
    public Deck createDeck(String deckName) throws SQLException {
        if (!UserSession.isLoggedIn()) {
            throw new SecurityException("Devi essere loggato per creare un deck.");
        }

        User current = UserSession.getCurrentUser();

        Deck deck = new Deck(deckName, current);

        deckDAO.createDeck(deck);
        return deck;
    }

    /**
     * Restituisce tutti i deck dell’utente loggato.
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

        // TODO Controllo se esiste già un deck con quel nome per lo stesso utente

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
     * Aggiunge una carta al deck (solo owner).
     */
    public void addCardToDeck(int deckId, int cardId) throws SQLException {
        Deck deck = getDeckById(deckId);
        Card card = cardDAO.getCardById(cardId);

        if (card == null) {
            throw new IllegalArgumentException("Carta inesistente");
        }

        if (!isOwner(deck.getOwner().getUserId())) {
            throw new SecurityException("Non hai permessi per modificare questo deck.");
        }

        deckDAO.addCardToDeck(deckId, cardId);
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

