package com.furkan.digitalWallet.service;

import com.furkan.digitalWallet.entity.Customer;
import com.furkan.digitalWallet.enums.Role;
import com.furkan.digitalWallet.exception.NotFoundException;
import com.furkan.digitalWallet.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setTckn("12345678901");
        customer.setUsername("johndoe");
        customer.setPassword("password");
        customer.setRole(Role.CUSTOMER);
    }

    @Test
    void getByUsername_ShouldReturnCustomer_WhenUsernameExists() {
        
        String username = "johndoe";
        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));

        
        Customer result = customerService.getByUsername(username);

        
        assertNotNull(result);
        assertEquals(customer.getId(), result.getId());
        assertEquals(customer.getUsername(), result.getUsername());
        assertEquals(customer.getName(), result.getName());
        verify(customerRepository).findByUsername(username);
    }

    @Test
    void getByUsername_ShouldThrowNotFoundException_WhenUsernameDoesNotExist() {
        
        String username = "nonexistent";
        when(customerRepository.findByUsername(username)).thenReturn(Optional.empty());

        
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> customerService.getByUsername(username));

        assertEquals("Kullanıcı bulunamadı: " + username, exception.getMessage());
        verify(customerRepository).findByUsername(username);
    }
}
