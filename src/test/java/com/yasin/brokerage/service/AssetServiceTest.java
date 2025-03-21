package com.yasin.brokerage.service;

import com.yasin.brokerage.model.Asset;
import com.yasin.brokerage.model.Customer;
import com.yasin.brokerage.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    private Customer customer;
    private Asset tryAsset;
    private Asset stockAsset;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setUsername("testUser");

        tryAsset = new Asset(null, customer, "TRY", 10000, 10000);
        stockAsset = new Asset(null, customer, "AAPL", 10, 5);
    }

    @Test
    void testGetAssetsByCustomer() {
        when(assetRepository.findByCustomer(customer)).thenReturn(List.of(tryAsset, stockAsset));

        List<Asset> assets = assetService.getAssetsByCustomer(customer);

        assertThat(assets).hasSize(2);
        assertThat(assets).extracting(Asset::getAssetName).containsExactlyInAnyOrder("TRY", "AAPL");
    }

    @Test
    void testFindAssetByCustomerAndName() {
        when(assetRepository.findByCustomerAndAssetName(customer, "AAPL")).thenReturn(Optional.of(stockAsset));

        Optional<Asset> foundAsset = assetService.findAssetByCustomerAndName(customer, "AAPL");

        assertThat(foundAsset).isPresent();
        assertThat(foundAsset.get().getUsableSize()).isEqualTo(5);
    }

    @Test
    void testUpdateAsset() {
        when(assetRepository.save(any(Asset.class))).thenReturn(stockAsset);

        Asset updatedAsset = assetService.updateAsset(stockAsset);

        assertThat(updatedAsset.getAssetName()).isEqualTo("AAPL");
        verify(assetRepository, times(1)).save(any(Asset.class));
    }

    @Test
    void testAddNewAsset() {
        when(assetRepository.findByCustomerAndAssetName(customer, "NVDA")).thenReturn(Optional.empty());
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Asset newAsset = assetService.addOrUpdateAsset(customer, "NVDA", 15, 15);

        assertThat(newAsset.getAssetName()).isEqualTo("NVDA");
        assertThat(newAsset.getSize()).isEqualTo(15);
        assertThat(newAsset.getUsableSize()).isEqualTo(15);
    }

    @Test
    void testUpdateExistingAsset() {
        when(assetRepository.findByCustomerAndAssetName(customer, "AAPL")).thenReturn(Optional.of(stockAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Asset updatedAsset = assetService.addOrUpdateAsset(customer, "AAPL", 10, 10);

        assertThat(updatedAsset.getSize()).isEqualTo(20);
        assertThat(updatedAsset.getUsableSize()).isEqualTo(15);
    }

    @Test
    void testUpdateAssetBalance() {
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tryAsset.setUsableSize(8000);
        Asset updatedTryAsset = assetService.updateAsset(tryAsset);

        assertThat(updatedTryAsset.getUsableSize()).isEqualTo(8000);
    }
}