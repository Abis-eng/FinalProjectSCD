package com.elcinic.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void hash_isDeterministic() {
        String h1 = PasswordHasher.hash("password123");
        String h2 = PasswordHasher.hash("password123");
        assertEquals(h1, h2);
        assertEquals(64, h1.length());
    }

    @Test
    void verify_matchesKnownAdminHash() {
        String hash = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";
        assertTrue(PasswordHasher.verify("admin123", hash));
        assertFalse(PasswordHasher.verify("wrong", hash));
    }

    @Test
    void hash_rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(null));
    }
}
