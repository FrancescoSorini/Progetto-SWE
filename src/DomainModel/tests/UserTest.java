package DomainModel.tests;

import DomainModel.user.Role;
import DomainModel.user.User;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void gettersAndSettersWork() {
        User user = new User("alice", "a@a.it", "pwd", true, Role.PLAYER);
        user.setUserId(12);
        user.setUsername("alice2");
        user.setEmail("a2@a.it");
        user.setPassword("pwd2");
        user.setEnabled(false);
        user.setRole(Role.ADMIN);

        assertEquals(12, user.getUserId());
        assertEquals("alice2", user.getUsername());
        assertEquals("a2@a.it", user.getEmail());
        assertEquals("pwd2", user.getPassword());
        assertEquals(Role.ADMIN, user.getRole());
        assertEquals(1, user.getRoleId());
        assertEquals(false, user.isEnabled());
    }

    @Test
    void printUsersHandlesEmptyList() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(output));
            User.printUsers(List.of());
        } finally {
            System.setOut(original);
        }
        assertTrue(output.toString().contains("Nessun utente trovato."));
    }
}
