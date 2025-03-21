package com.yasin.brokerage.service;

import com.yasin.brokerage.model.Asset;
import com.yasin.brokerage.model.Customer;
import com.yasin.brokerage.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    public List<Asset> getAssetsByCustomer(Customer customer) {
        return assetRepository.findByCustomer(customer);
    }

    public Optional<Asset> findAssetByCustomerAndName(Customer customer, String assetName) {
        return assetRepository.findByCustomerAndAssetName(customer, assetName);
    }

    public Asset addOrUpdateAsset(Customer customer, String assetName, double size, double usableSize) {
        Optional<Asset> existingAsset = assetRepository.findByCustomerAndAssetName(customer, assetName);

        if (existingAsset.isPresent()) {
            Asset asset = existingAsset.get();
            asset.setSize(asset.getSize() + size);
            asset.setUsableSize(asset.getUsableSize() + usableSize);
            return assetRepository.save(asset);
        } else {
            Asset newAsset = new Asset(null, customer, assetName, size, usableSize);
            return assetRepository.save(newAsset);
        }
    }

    public Asset updateAsset(Asset asset) {
        return assetRepository.save(asset);
    }
}