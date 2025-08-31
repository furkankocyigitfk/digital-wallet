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
        
        securityContextHolderMockedStatic = mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        
        securityContextHolderMockedStatic.close();
    }

    @Test
    void currentUsername_AuthenticationPresent_ReturnsUsername() {
        
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        
        String username = SecurityUtil.currentUsername();

        
        assertEquals("testuser", username);
    }

    @Test
    void currentUsername_AuthenticationNull_ReturnsNull() {
        
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        
        String username = SecurityUtil.currentUsername();

        
        assertNull(username);
    }

    @Test
    void hasRole_AuthenticationPresentAndEmptyAuthorities_ReturnsFalse() {
        
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn(List.of());

        
        boolean hasRole = SecurityUtil.hasRole("USER");

        
        assertFalse(hasRole);
    }

    @Test
    void hasRole_AuthenticationNull_ReturnsFalse() {
        
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        
        boolean hasRole = SecurityUtil.hasRole("USER");

        
        assertFalse(hasRole);
    }
}