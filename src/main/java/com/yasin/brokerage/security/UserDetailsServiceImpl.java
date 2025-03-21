package com.yasin.brokerage.security;

import com.yasin.brokerage.model.Customer;
import com.yasin.brokerage.repository.CustomerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public UserDetailsServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.withUsername(customer.getUsername())
                .password(customer.getPassword()) // Hashed password
                .roles(customer.getRole().toUpperCase()) // CUSTOMER or ADMIN
                .build();
    }
}