package com.yasin.brokerage.security;

import com.yasin.brokerage.model.Customer;
import com.yasin.brokerage.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setUsername("testUser");
        customer.setPassword("$2a$10$hashedpassword"); // Mocked BCrypt hash
        customer.setRole("CUSTOMER");
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(customerRepository.findByUsername("testUser")).thenReturn(Optional.of(customer));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testUser");

        assertThat(userDetails.getUsername()).isEqualTo("testUser");
        assertThat(userDetails.getPassword()).isEqualTo(customer.getPassword());
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_CUSTOMER");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(customerRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("unknownUser"));

        assertThat(exception.getMessage()).isEqualTo("User not found: unknownUser");
    }
}