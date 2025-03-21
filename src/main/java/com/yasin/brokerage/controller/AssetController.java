package com.yasin.brokerage.controller;

import com.yasin.brokerage.model.Asset;
import com.yasin.brokerage.model.Customer;
import com.yasin.brokerage.service.AssetService;
import com.yasin.brokerage.service.CustomerService;
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
@RequestMapping("/assets")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/{username}")
    public ResponseEntity<List<Asset>> getAssetsByCustomer(@PathVariable String username) {
        Optional<Customer> customer = customerService.findCustomerByUsername(username);
        if (customer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Asset> assets = assetService.getAssetsByCustomer(customer.get());
        return ResponseEntity.ok(assets);
    }

    // Redirect /assets to /assets/{authenticated_username}
    @GetMapping()
    public ResponseEntity<Void> getAuthenticatedUserRedirect(@AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                .header(HttpHeaders.LOCATION, "/assets/" + user.getUsername())
                .build();
    }

    @PostMapping("/add")
    public ResponseEntity<Asset> addAsset(@RequestParam String username,
                                          @RequestParam String assetName,
                                          @RequestParam double size,
                                          @RequestParam double usableSize) {
        Optional<Customer> customer = customerService.findCustomerByUsername(username);
        if (customer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Asset asset = assetService.addOrUpdateAsset(customer.get(), assetName, size, usableSize);
        return ResponseEntity.ok(asset);
    }
}