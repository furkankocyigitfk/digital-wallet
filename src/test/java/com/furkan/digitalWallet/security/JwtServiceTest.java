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
        
        String username = "testuser";
        String role = "CUSTOMER";

        
        String token = jwtService.generateToken(username, role);

        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void extractUsername_ShouldReturnUsername_WhenValidToken() {
        
        String username = "testuser";
        String role = "CUSTOMER";
        String token = jwtService.generateToken(username, role);

        
        String extractedUsername = jwtService.extractUsername(token);

        
        assertEquals(username, extractedUsername);
    }

    @Test
    void extractRole_ShouldReturnRole_WhenValidToken() {
        
        String username = "testuser";
        String role = "CUSTOMER";
        String token = jwtService.generateToken(username, role);

        
        String extractedRole = jwtService.extractRole(token);

        
        assertEquals(role, extractedRole);
    }

    @Test
    void extractRole_ShouldReturnNull_WhenTokenHasNoRole() {
        
        String username = "testuser";
        String token = jwtService.generateToken(username, null);

        
        String extractedRole = jwtService.extractRole(token);

        
        assertNull(extractedRole);
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenValidToken() {
        
        String username = "testuser";
        String role = "CUSTOMER";
        String token = jwtService.generateToken(username, role);

        
        boolean isValid = jwtService.isTokenValid(token);

        
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenInvalidToken() {
        
        String invalidToken = "invalid.token.here";

        
        boolean isValid = jwtService.isTokenValid(invalidToken);

        
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenNullToken() {
        
        boolean isValid = jwtService.isTokenValid(null);

        
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenEmptyToken() {
        
        boolean isValid = jwtService.isTokenValid("");

        
        assertFalse(isValid);
    }

    @Test
    void generateToken_ShouldGenerateDifferentTokens_WhenCalledMultipleTimes() {
        
        String username = "testuser";
        String role = "CUSTOMER";

        
        String token1 = jwtService.generateToken(username, role);
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtService.generateToken(username, role);

        
        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_ShouldHandleSpecialCharacters_InUsernameAndRole() {
        
        String username = "test.user@example.com";
        String role = "SPECIAL_ROLE";

        
        String token = jwtService.generateToken(username, role);

        
        assertNotNull(token);
        assertEquals(username, jwtService.extractUsername(token));
        assertEquals(role, jwtService.extractRole(token));
    }

    @Test
    void extractUsername_ShouldThrowException_WhenInvalidToken() {
        
        String invalidToken = "invalid.token.signature";

        
        assertThrows(Exception.class, () -> jwtService.extractUsername(invalidToken));
    }

    @Test
    void extractRole_ShouldThrowException_WhenInvalidToken() {
        
        String invalidToken = "invalid.token.signature";

        
        assertThrows(Exception.class, () -> jwtService.extractRole(invalidToken));
    }
}
