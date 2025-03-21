package com.yasin.brokerage.repository;

import com.yasin.brokerage.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        Customer customer = Customer.builder()
                .username("testUser")
                .password("password123")
                .role("CUSTOMER")
                .build();

        customerRepository.save(customer);
    }

    @Test
    void testFindByUsername() {
        Optional<Customer> foundCustomer = customerRepository.findByUsername("testUser");
        assertThat(foundCustomer).isPresent();
        assertThat(foundCustomer.get().getUsername()).isEqualTo("testUser");
    }
}