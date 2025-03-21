package com.yasin.brokerage.controller;

import com.yasin.brokerage.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/match/{orderId}")
    public ResponseEntity<String> matchOrder(@PathVariable Long orderId) {
        try {
            adminService.matchOrder(orderId);
            return ResponseEntity.ok("Order matched successfully!");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

