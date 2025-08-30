package com.furkan.digitalWallet.security;

import com.furkan.digitalWallet.entity.Customer;
import com.furkan.digitalWallet.enums.Role;
import com.furkan.digitalWallet.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    private Customer customer;
    private Customer employee;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setUsername("testuser");
        customer.setPassword("hashedPassword");
        customer.setRole(Role.CUSTOMER);

        employee = new Customer();
        employee.setId(2L);
        employee.setUsername("testemployee");
        employee.setPassword("hashedEmployeePassword");
        employee.setRole(Role.EMPLOYEE);
    }

    @Test
    void loadUserByUsername_CustomerExists_ReturnsUserDetails() {
        // Given
        String username = "testuser";
        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));

        // When
        UserDetails userDetails = appUserDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("hashedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());

        // Verify authorities
        assertEquals(1, userDetails.getAuthorities().size());
        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_CUSTOMER", authority.getAuthority());

        verify(customerRepository).findByUsername(username);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void loadUserByUsername_EmployeeExists_ReturnsUserDetailsWithEmployeeRole() {
        // Given
        String username = "testemployee";
        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(employee));

        // When
        UserDetails userDetails = appUserDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        assertEquals("testemployee", userDetails.getUsername());
        assertEquals("hashedEmployeePassword", userDetails.getPassword());

        // Verify authorities
        assertEquals(1, userDetails.getAuthorities().size());
        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_EMPLOYEE", authority.getAuthority());

        verify(customerRepository).findByUsername(username);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        // Given
        String username = "nonexistentuser";
        when(customerRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> appUserDetailsService.loadUserByUsername(username)
        );

        assertEquals("User not found", exception.getMessage());

        verify(customerRepository).findByUsername(username);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void loadUserByUsername_NullUsername_ThrowsUsernameNotFoundException() {
        // Given
        String username = null;
        when(customerRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                UsernameNotFoundException.class,
                () -> appUserDetailsService.loadUserByUsername(username)
        );

        verify(customerRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_EmptyUsername_ThrowsUsernameNotFoundException() {
        // Given
        String username = "";
        when(customerRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                UsernameNotFoundException.class,
                () -> appUserDetailsService.loadUserByUsername(username)
        );

        verify(customerRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_CustomerWithNullRole_ReturnsUserDetailsWithRoleNull() {
        // Given
        String username = "testuser";
        customer.setRole(null);
        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));

        // When
        UserDetails userDetails = appUserDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());

        // Verify authorities - should have ROLE_null
        assertEquals(1, userDetails.getAuthorities().size());
        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_null", authority.getAuthority());

        verify(customerRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_RepositoryThrowsException_PropagatesException() {
        // Given
        String username = "testuser";
        when(customerRepository.findByUsername(username))
                .thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> appUserDetailsService.loadUserByUsername(username)
        );

        assertEquals("Database connection error", exception.getMessage());

        verify(customerRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_CustomerHasSpecialCharactersInCredentials_ReturnsCorrectUserDetails() {
        // Given
        String username = "user@domain.com";
        customer.setUsername(username);
        customer.setPassword("password!@#$%^&*()");
        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));

        // When
        UserDetails userDetails = appUserDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        assertEquals("user@domain.com", userDetails.getUsername());
        assertEquals("password!@#$%^&*()", userDetails.getPassword());

        verify(customerRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_AuthorityCreation_VerifyCorrectFormat() {
        // Given
        String username = "testuser";
        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));

        // When
        UserDetails userDetails = appUserDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails.getAuthorities());
        assertFalse(userDetails.getAuthorities().isEmpty());

        // Check that authority is of correct type
        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertInstanceOf(SimpleGrantedAuthority.class, authority);
        assertTrue(authority.getAuthority().startsWith("ROLE_"));

        verify(customerRepository).findByUsername(username);
    }
}
