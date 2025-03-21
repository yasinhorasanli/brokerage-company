package com.yasin.brokerage.controller;

import com.yasin.brokerage.model.Customer;
import com.yasin.brokerage.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;


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

    @GetMapping({"/{username}"})
    public ResponseEntity<Customer> getCustomerByUsername(
            @PathVariable(required = false) String username,
            @AuthenticationPrincipal User user) {

        String authenticatedUsername = user.getUsername();

        // USERNAME == AUTH_USER
        if (username == null || username.isEmpty() || username.equals(authenticatedUsername)) {
            return customerService.findCustomerByUsername(authenticatedUsername)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }

        // ROLE_ADMIN == AUTH_USER
        if (user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return customerService.findCustomerByUsername(username)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    // Redirect /customers/ to /customers/{authenticated_username}
    @GetMapping("/")
    public ResponseEntity<Void> getAuthenticatedUserRedirect(@AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                .header(HttpHeaders.LOCATION, "/customers/" + user.getUsername())
                .build();
    }
}