package DomainModel.tests;

import DomainModel.user.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleTest {

    @Test
    void roleIdsMatchExpectedValues() {
        assertEquals(1, Role.ADMIN.getRoleId());
        assertEquals(2, Role.ORGANIZER.getRoleId());
        assertEquals(3, Role.PLAYER.getRoleId());
    }
}
