package com.yasin.brokerage.repository;

import com.yasin.brokerage.model.Asset;
import com.yasin.brokerage.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByCustomer(Customer customer);
    Optional<Asset> findByCustomerAndAssetName(Customer customer, String assetName);
}