package DomainModel.tests;

import DomainModel.GameType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameTypeTest {

    @Test
    void fromIdReturnsExpectedEnum() {
        assertEquals(GameType.MAGIC, GameType.fromId(1));
        assertEquals(GameType.POKEMON, GameType.fromId(2));
        assertEquals(GameType.YUGIOH, GameType.fromId(3));
    }

    @Test
    void fromIdThrowsForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> GameType.fromId(99));
    }

    @Test
    void getGameIdReturnsExpectedValue() {
        assertEquals(1, GameType.MAGIC.getGameId());
        assertEquals(2, GameType.POKEMON.getGameId());
        assertEquals(3, GameType.YUGIOH.getGameId());
    }
}
