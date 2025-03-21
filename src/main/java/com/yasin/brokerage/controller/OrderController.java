package com.yasin.brokerage.controller;

import com.yasin.brokerage.model.Customer;
import com.yasin.brokerage.model.Order;
import com.yasin.brokerage.model.OrderSide;
import com.yasin.brokerage.model.OrderStatus;
import com.yasin.brokerage.service.CustomerService;
import com.yasin.brokerage.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Order> createOrder(@RequestParam String username,
                                             @RequestParam String assetName,
                                             @RequestParam OrderSide orderSide,
                                             @RequestParam int size,
                                             @RequestParam double price) {
        Optional<Customer> customer = customerService.findCustomerByUsername(username);
        if (customer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Order order = orderService.createOrder(customer.get(), assetName, orderSide, size, price);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable String username) {
        Optional<Customer> customer = customerService.findCustomerByUsername(username);
        if (customer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Order> orders = orderService.getOrdersByCustomer(customer.get());
        return ResponseEntity.ok(orders);
    }

    // Redirect /orders/ to /orders/{authenticated_username}
    @GetMapping("/")
    public ResponseEntity<Void> getAuthenticatedUserRedirect(@AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                .header(HttpHeaders.LOCATION, "/orders/" + user.getUsername())
                .build();
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long orderId,
                                              @AuthenticationPrincipal User user) {
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
}