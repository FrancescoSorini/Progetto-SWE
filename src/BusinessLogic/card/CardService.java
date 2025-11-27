package BusinessLogic.card;

import ORM.dao.CardDAO;
import DomainModel.card.Card;
import DomainModel.card.CardType;
import DomainModel.user.Role;
import DomainModel.user.User;
import BusinessLogic.session.UserSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CardService {

    private final CardDAO cardDAO;

    public CardService(Connection connection) {
        this.cardDAO = new CardDAO(connection);
    }

    // ====================================================================================
    // SECURITY CHECK: solo admin può modificare il catalogo
    // ====================================================================================
    private void checkAdminPermission() {
        User current = UserSession.getInstance().getCurrentUser();

        if (current == null)
            throw new SecurityException("You must be logged in to perform this action.");

        if (current.getRole() != Role.ADMIN)
            throw new SecurityException("Only administrators can modify the card catalog.");
    }

    // ====================================================================================
    // 1. CREATE CARD (Admin only)
    // ====================================================================================
    public void createCard(Card card) throws SQLException {
        checkAdminPermission();
        validateCard(card);

        if (isDuplicate(card.getName())) {
            throw new IllegalArgumentException("A card with this name already exists.");
        }

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
    public Card searchCardsByName(String name) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name filter cannot be empty.");
        }
        return cardDAO.getCardByName(name);
    }

    // ====================================================================================
    // 6. UPDATE CARD NAME (Admin)
    // ====================================================================================
    public void updateCardName(int cardId, String newName) throws SQLException {
        checkAdminPermission();

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
    // 7. UPDATE CARD TYPE (Admin)
    // ====================================================================================
    public void updateCardType(int cardId, CardType newType) throws SQLException {
        checkAdminPermission();

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
    // 8. DELETE CARD (Admin)
    // ====================================================================================
    public void deleteCard(int cardId) throws SQLException {
        checkAdminPermission();
        cardDAO.deleteCard(cardId);
    }

    // ====================================================================================
    // 9. BUSINESS LOGIC: VALIDAZIONE
    // ====================================================================================
    private void validateCard(Card card) {
        if (card.getName() == null || card.getName().isBlank()) {
            throw new IllegalArgumentException("Card name cannot be empty.");
        }
        if (card.getType() == null) {
            throw new IllegalArgumentException("Card type must be provided.");
        }
    }

    // ====================================================================================
    // 10. BUSINESS LOGIC: DUPLICATI
    // ====================================================================================
    public boolean isDuplicate(String name) throws SQLException {
        Card existing = cardDAO.getCardByName(name);
        return existing != null;
    }
}
