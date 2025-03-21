package com.yasin.brokerage.repository;

import com.yasin.brokerage.model.Asset;
import com.yasin.brokerage.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AssetRepositoryTest {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = customerRepository.save(Customer.builder()
                .username("testUser")
                .password("password123")
                .role("CUSTOMER")
                .build());
        assetRepository.save(Asset.builder()
                .customer(customer)
                .assetName("AAPL")
                .size(10)
                .usableSize(10)
                .build());
    }

    @Test
    void testFindByCustomer() {
        List<Asset> assets = assetRepository.findByCustomer(customer);
        assertThat(assets).hasSize(1);
        assertThat(assets.get(0).getAssetName()).isEqualTo("AAPL");
    }

    @Test
    void testFindByCustomerAndAssetName() {
        Optional<Asset> asset = assetRepository.findByCustomerAndAssetName(customer, "AAPL");
        assertThat(asset).isPresent();
        assertThat(asset.get().getUsableSize()).isEqualTo(10);
    }
}