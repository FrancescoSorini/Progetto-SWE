package BusinessLogic.card;

import DomainModel.GameType;
import DomainModel.card.Card;
import DomainModel.card.factory.CardFactory;
import DomainModel.card.factory.MagicCardFactory;
import DomainModel.card.factory.PokemonCardFactory;
import DomainModel.card.factory.YuGiOhCardFactory;
import DomainModel.user.User;
import ORM.dao.CardDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CardService {

    private final CardDAO cardDAO;

    public CardService(Connection connection) {
        this.cardDAO = new CardDAO(connection);
    }

    // ====================================================================================
    // 1. CREATE CARD - via Factory
    // ====================================================================================
    public void createCard(User caller, Card card) throws SQLException {
        createCardViaFactory(card.getName(), card.getType());
    }

    private void createCardViaFactory(String name, GameType type) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Card name cannot be empty.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Card type must be provided.");
        }
        if (isDuplicate(name)) {
            throw new IllegalArgumentException("A card with this name already exists.");
        }

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

        Card card = factory.createCard(name);
        validateCard(card);
        cardDAO.addCard(card);
    }

    // ====================================================================================
    // 2. GET CARD BY ID (everyone)
    // ====================================================================================
    public Card getCardById(int cardId) throws SQLException {
        return cardDAO.getCardById(cardId);
    }

    // ====================================================================================
    // 3. GET ALL CARDS (everyone)
    // ====================================================================================
    public List<Card> getAllCards() throws SQLException {
        return cardDAO.getAllCards();
    }

    // ====================================================================================
    // 4. SEARCH CARDS BY NAME (everyone)
    // ====================================================================================
    public List<Card> searchCardsByName(GameType gameType, String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Nome non valido");
        }

        List<Card> allCards = cardDAO.getCardsByGameType(gameType);
        String lowerKeyword = keyword.toLowerCase();

        return allCards.stream()
                .filter(card -> card.getName().toLowerCase().contains(lowerKeyword))
                .toList();
    }

    // ====================================================================================
    // 5. GET CARDS BY GAMETYPE (everyone)
    // ====================================================================================
    public List<Card> getCardsByGameType(GameType type) throws SQLException {
        if (type == null) {
            throw new IllegalArgumentException("Game type filter cannot be null.");
        }
        return cardDAO.getCardsByGameType(type);
    }

    // ====================================================================================
    // 6. UPDATE CARD NAME
    // ====================================================================================
    public void updateCardName(User caller, int cardId, String newName) throws SQLException {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Card name cannot be empty.");
        }
        if (isDuplicate(newName)) {
            throw new IllegalArgumentException("A card with this name already exists.");
        }

        Card target = cardDAO.getCardById(cardId);
        if (target == null) {
            throw new IllegalArgumentException("Card with ID " + cardId + " does not exist.");
        }

        target.setCardName(newName);
        cardDAO.updateCard(target);
    }

    // ====================================================================================
    // 7. UPDATE CARD TYPE
    // ====================================================================================
    public void updateCardType(User caller, int cardId, GameType newType) throws SQLException {
        if (newType == null) {
            throw new IllegalArgumentException("Card type cannot be null.");
        }

        Card target = cardDAO.getCardById(cardId);
        if (target == null) {
            throw new IllegalArgumentException("Card with ID " + cardId + " does not exist.");
        }

        target.setCardType(newType);
        cardDAO.updateCard(target);
    }

    // ====================================================================================
    // 8. DELETE CARD
    // ====================================================================================
    public void deleteCard(User caller, int cardId) throws SQLException {
        cardDAO.deleteCard(cardId);
    }

    // ====================================================================================
    // 9. UTILITIES
    // ====================================================================================
    private void validateCard(Card card) {
        if (card.getName() == null || card.getName().isBlank()) {
            throw new IllegalArgumentException("Card name cannot be empty.");
        }
        if (card.getType() == null) {
            throw new IllegalArgumentException("Card type must be provided.");
        }
    }

    public boolean isDuplicate(String name) throws SQLException {
        Card existing = cardDAO.getCardByName(name);
        return existing != null;
    }
}
