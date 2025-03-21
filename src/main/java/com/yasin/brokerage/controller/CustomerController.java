package com.yasin.brokerage.controller;

import com.yasin.brokerage.model.Customer;
import com.yasin.brokerage.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/create")
    public ResponseEntity<Customer> createCustomer(@RequestParam String username,
                                                   @RequestParam String password,
                                                   @RequestParam String role) {
        Customer customer = customerService.createCustomer(username, password, role);
        return ResponseEntity.ok(customer);
    }

    @GetMapping
    public ResponseEntity<?> listCustomers(@AuthenticationPrincipal User user,
                                           @RequestParam(required = false) String username) {

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        String authenticatedUsername = user.getUsername();

        // If admin and no filter: return all customers
        if (isAdmin && (username == null || username.isBlank())) {
            List<Customer> all = customerService.findAllCustomers();
            return ResponseEntity.ok(all);
        }

        String targetUsername = (username == null || username.isBlank())
                ? authenticatedUsername
                : username;

        // If not admin and trying to see someone else
        if (!targetUsername.equals(authenticatedUsername) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        // Return single customer (self or by admin)
        Optional<Customer> customer = customerService.findCustomerByUsername(targetUsername);
        return customer.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}