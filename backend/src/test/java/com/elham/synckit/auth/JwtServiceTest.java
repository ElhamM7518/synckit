package com.elham.synckit.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "synckit-dev-secret-change-me-please-32bytes-min",
            3_600_000
    );

    @Test
    void generateTokenRoundTripsUserId() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        String token = jwtService.generateToken(userId, "elham@example.com");

        assertTrue(jwtService.isValid(token));
        assertEquals(userId, jwtService.extractUserId(token));
    }
}
