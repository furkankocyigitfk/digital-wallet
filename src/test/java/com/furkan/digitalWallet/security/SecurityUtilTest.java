package com.furkan.digitalWallet.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityUtilTest {

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private MockedStatic<SecurityContextHolder> securityContextHolderMockedStatic;

    @BeforeEach
    void setUp() {
        // Initialize static mock for SecurityContextHolder
        securityContextHolderMockedStatic = mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        // Close the static mock
        securityContextHolderMockedStatic.close();
    }

    @Test
    void currentUsername_AuthenticationPresent_ReturnsUsername() {
        // Arrange
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        // Act
        String username = SecurityUtil.currentUsername();

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void currentUsername_AuthenticationNull_ReturnsNull() {
        // Arrange
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        String username = SecurityUtil.currentUsername();

        // Assert
        assertNull(username);
    }

    @Test
    void hasRole_AuthenticationPresentAndEmptyAuthorities_ReturnsFalse() {
        // Arrange
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn(List.of());

        // Act
        boolean hasRole = SecurityUtil.hasRole("USER");

        // Assert
        assertFalse(hasRole);
    }

    @Test
    void hasRole_AuthenticationNull_ReturnsFalse() {
        // Arrange
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        boolean hasRole = SecurityUtil.hasRole("USER");

        // Assert
        assertFalse(hasRole);
    }
}