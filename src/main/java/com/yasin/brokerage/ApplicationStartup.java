package com.yasin.brokerage;

import com.yasin.brokerage.model.Customer;
import com.yasin.brokerage.service.AssetService;
import com.yasin.brokerage.service.CustomerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements CommandLineRunner {

    private final CustomerService customerService;
    private final AssetService assetService;

    public ApplicationStartup(CustomerService customerService, AssetService assetService) {
        this.customerService = customerService;
        this.assetService = assetService;
    }

    @Override
    public void run(String... args) {
        Customer yasin = customerService.createCustomer("yasin", "1234", "CUSTOMER");
        Customer alice = customerService.createCustomer("alice", "pass123", "CUSTOMER");
        Customer admin = customerService.createCustomer("admin", "adminpass", "ADMIN");

        assetService.addOrUpdateAsset(yasin, "TRY", 10000, 10000);
        assetService.addOrUpdateAsset(alice, "TRY", 5000, 5000);

        assetService.addOrUpdateAsset(yasin, "NVDA", 20, 20);
        assetService.addOrUpdateAsset(alice, "AAPL", 15, 15);

        System.out.println("Example customers and assets initialized!");
    }
}
