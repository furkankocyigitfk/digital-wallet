package com.furkan.digitalWallet.controller;

import com.furkan.digitalWallet.entity.Customer;
import com.furkan.digitalWallet.enums.Role;
import com.furkan.digitalWallet.repository.CustomerRepository;
import com.furkan.digitalWallet.request.AuthRequest;
import com.furkan.digitalWallet.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AuthController authController;

    private AuthRequest authRequest;
    private Customer customer;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setUsername("customer");
        authRequest.setPassword("password");

        customer = new Customer();
        customer.setUsername("customer");
        customer.setRole(Role.CUSTOMER);
    }

    @Test
    void login_SuccessfulAuthentication_ReturnsTokenAndUserDetails() {
        // Arrange
        String token = "jwt-token";
        when(customerRepository.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(jwtService.generateToken("customer", "CUSTOMER")).thenReturn(token);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        // Act
        ResponseEntity<?> response = authController.login(authRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(token, responseBody.get("token"));
        assertEquals("CUSTOMER", responseBody.get("role"));
        assertEquals("customer", responseBody.get("username"));

        verify(authenticationManager).authenticate(argThat(auth ->
                auth instanceof UsernamePasswordAuthenticationToken &&
                        auth.getPrincipal().equals("customer") &&
                        auth.getCredentials().equals("password")));
        verify(customerRepository).findByUsername("customer");
        verify(jwtService).generateToken("customer", "CUSTOMER");
        verifyNoMoreInteractions(authenticationManager, customerRepository, jwtService);
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        // Arrange
        when(customerRepository.findByUsername("customer")).thenReturn(Optional.empty());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authController.login(authRequest));

        verify(authenticationManager).authenticate(argThat(auth ->
                auth instanceof UsernamePasswordAuthenticationToken &&
                        auth.getPrincipal().equals("customer") &&
                        auth.getCredentials().equals("password")));
        verify(customerRepository).findByUsername("customer");
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_AuthenticationFailure_ThrowsAuthenticationException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Invalid credentials") {
                });

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authController.login(authRequest));

        verify(authenticationManager).authenticate(argThat(auth ->
                auth instanceof UsernamePasswordAuthenticationToken &&
                        auth.getPrincipal().equals("customer") &&
                        auth.getCredentials().equals("password")));
        verifyNoInteractions(customerRepository, jwtService);
    }

    @Test
    void login_NullAuthRequest_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> authController.login(null));

        verifyNoInteractions(authenticationManager, customerRepository, jwtService);
    }
}