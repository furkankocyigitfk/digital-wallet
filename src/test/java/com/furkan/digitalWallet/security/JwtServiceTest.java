package com.furkan.digitalWallet.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secret", "mySecretKeyForJWTTokenGenerationThatIsLongEnoughToMeetRequirements");
        ReflectionTestUtils.setField(jwtService, "expirationSeconds", 18000L);
    }

    @Test
    void generateToken_ShouldReturnValidToken_WhenValidUsernameAndRole() {
        // Given
        String username = "testuser";
        String role = "CUSTOMER";

        // When
        String token = jwtService.generateToken(username, role);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void extractUsername_ShouldReturnUsername_WhenValidToken() {
        // Given
        String username = "testuser";
        String role = "CUSTOMER";
        String token = jwtService.generateToken(username, role);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void extractRole_ShouldReturnRole_WhenValidToken() {
        // Given
        String username = "testuser";
        String role = "CUSTOMER";
        String token = jwtService.generateToken(username, role);

        // When
        String extractedRole = jwtService.extractRole(token);

        // Then
        assertEquals(role, extractedRole);
    }

    @Test
    void extractRole_ShouldReturnNull_WhenTokenHasNoRole() {
        // Given
        String username = "testuser";
        String token = jwtService.generateToken(username, null);

        // When
        String extractedRole = jwtService.extractRole(token);

        // Then
        assertNull(extractedRole);
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenValidToken() {
        // Given
        String username = "testuser";
        String role = "CUSTOMER";
        String token = jwtService.generateToken(username, role);

        // When
        boolean isValid = jwtService.isTokenValid(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtService.isTokenValid(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenNullToken() {
        // When
        boolean isValid = jwtService.isTokenValid(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenEmptyToken() {
        // When
        boolean isValid = jwtService.isTokenValid("");

        // Then
        assertFalse(isValid);
    }

    @Test
    void generateToken_ShouldGenerateDifferentTokens_WhenCalledMultipleTimes() {
        // Given
        String username = "testuser";
        String role = "CUSTOMER";

        // When
        String token1 = jwtService.generateToken(username, role);
        // Add small delay to ensure different issuedAt times
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtService.generateToken(username, role);

        // Then
        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_ShouldHandleSpecialCharacters_InUsernameAndRole() {
        // Given
        String username = "test.user@example.com";
        String role = "SPECIAL_ROLE";

        // When
        String token = jwtService.generateToken(username, role);

        // Then
        assertNotNull(token);
        assertEquals(username, jwtService.extractUsername(token));
        assertEquals(role, jwtService.extractRole(token));
    }

    @Test
    void extractUsername_ShouldThrowException_WhenInvalidToken() {
        // Given
        String invalidToken = "invalid.token.signature";

        // When & Then
        assertThrows(Exception.class, () -> jwtService.extractUsername(invalidToken));
    }

    @Test
    void extractRole_ShouldThrowException_WhenInvalidToken() {
        // Given
        String invalidToken = "invalid.token.signature";

        // When & Then
        assertThrows(Exception.class, () -> jwtService.extractRole(invalidToken));
    }
}
