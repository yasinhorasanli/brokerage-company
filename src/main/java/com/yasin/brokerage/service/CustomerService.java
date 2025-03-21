package com.yasin.brokerage.service;

import com.yasin.brokerage.model.Customer;
import com.yasin.brokerage.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<Customer> findCustomerByUsername(String username) {
        return customerRepository.findByUsername(username);
    }

    public Customer createCustomer(String username, String password, String role) {
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setPassword(passwordEncoder.encode(password));
        customer.setRole(role);
        return customerRepository.save(customer);
    }

    public boolean authenticate(String username, String password) {
        return customerRepository.findByUsername(username)
                .map(customer -> passwordEncoder.matches(password, customer.getPassword()))
                .orElse(false);
    }
}