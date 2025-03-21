package com.yasin.brokerage.service;

import com.yasin.brokerage.model.Customer;
import com.yasin.brokerage.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setUsername("testUser");
        customer.setPassword("$2a$10$7hVq8uRQK3N7U.9VOUjbQeYfU/SOIk8CDHGnp7dYoybi1wDkHqHSO"); // BCrypt hashed --> "password123"
        customer.setRole("CUSTOMER");
    }

    @Test
    void testFindCustomerByUsername() {
        when(customerRepository.findByUsername("testUser")).thenReturn(Optional.of(customer));

        Optional<Customer> foundCustomer = customerService.findCustomerByUsername("testUser");

        assertThat(foundCustomer).isPresent();
        assertThat(foundCustomer.get().getUsername()).isEqualTo("testUser");
    }

    @Test
    void testCreateCustomer() {
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedpassword");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Customer newCustomer = customerService.createCustomer("newUser", "password123", "CUSTOMER");

        assertThat(newCustomer.getUsername()).isEqualTo("newUser");
        assertThat(newCustomer.getPassword()).isEqualTo("$2a$10$hashedpassword");
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testAuthenticateSuccess() {
        when(customerRepository.findByUsername("testUser")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("password123", customer.getPassword())).thenReturn(true);

        boolean isAuthenticated = customerService.authenticate("testUser", "password123");

        assertThat(isAuthenticated).isTrue();
    }

    @Test
    void testAuthenticateFailure() {
        when(customerRepository.findByUsername("testUser")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("wrongPassword", customer.getPassword())).thenReturn(false);

        boolean isAuthenticated = customerService.authenticate("testUser", "wrongPassword");

        assertThat(isAuthenticated).isFalse();
    }
}