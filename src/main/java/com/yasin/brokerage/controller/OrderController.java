package com.yasin.brokerage.controller;

import com.yasin.brokerage.model.Customer;
import com.yasin.brokerage.model.Order;
import com.yasin.brokerage.model.OrderSide;
import com.yasin.brokerage.model.OrderStatus;
import com.yasin.brokerage.service.CustomerService;
import com.yasin.brokerage.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@AuthenticationPrincipal User user,
                                             @RequestParam(required = false) String username,
                                             @RequestParam String assetName,
                                             @RequestParam OrderSide orderSide,
                                             @RequestParam int size,
                                             @RequestParam double price,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createDate) {

        String authenticatedUsername = user.getUsername();
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        String targetUsername = (username != null && !username.isBlank()) ? username : authenticatedUsername;

        if (!targetUsername.equals(authenticatedUsername) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to create orders for other users.");
        }

        // Reject past createDate
        if (createDate != null && createDate.isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("createDate cannot be in the past.");
        }
        LocalDateTime finalCreateDate = (createDate != null) ? createDate : LocalDateTime.now();

        Optional<Customer> customer = customerService.findCustomerByUsername(targetUsername);
        if (customer.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found: " + targetUsername);
        }

        Order order = orderService.createOrder(customer.get(), assetName, orderSide, size, price, finalCreateDate);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<Order>> listOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) OrderSide side) {

        String authenticatedUsername = user.getUsername();
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Admin can list all orders
        if (isAdmin && (username == null || username.isBlank())) {
            List<Order> orders = orderService.listOrders(null, startDate, endDate, status, side); // pass null customer
            return ResponseEntity.ok(orders);
        }

        // Determine target user (self or specified)
        String targetUsername = (username == null || username.isBlank()) ? authenticatedUsername : username;

        // If not admin and trying to view others' data
        if (!targetUsername.equals(authenticatedUsername) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Load the customer and filter
        Customer customer = customerService.findCustomerByUsername(targetUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found"));

        List<Order> orders = orderService.listOrders(customer, startDate, endDate, status, side);
        return ResponseEntity.ok(orders);
    }


    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteOrder(@AuthenticationPrincipal User user,
                                              @RequestParam Long orderId) {
        Optional<Order> orderOpt = orderService.getOrderById(orderId);

        if (orderOpt.isEmpty() || orderOpt.get().getStatus() != OrderStatus.PENDING) {
            return ResponseEntity.badRequest().body("Order not found or cannot be deleted.");
        }

        Order order = orderOpt.get();
        String authenticatedUsername = user.getUsername();
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Order can be deleted by its OWNER or an ADMIN
        if (isAdmin || order.getCustomer().getUsername().equals(authenticatedUsername)) {
            orderService.deleteOrder(order);
            return ResponseEntity.ok("Order successfully deleted: " + order);
        }

        // Deny access for others
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete this order.");
    }

    @PostMapping("/match")
    public ResponseEntity<String> matchOrder(@RequestParam Long orderId) {
        try {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();
            orderService.matchOrder(order);
            return ResponseEntity.ok("Order matched successfully!");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}