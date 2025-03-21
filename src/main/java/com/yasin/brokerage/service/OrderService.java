package com.yasin.brokerage.service;

import com.yasin.brokerage.model.*;
import com.yasin.brokerage.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AssetService assetService;

    public List<Order> getOrdersByCustomer(Customer customer) {
        return orderRepository.findByCustomer(customer);
    }

    public List<Order> getOrdersByDateRange(Customer customer, LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByCustomerAndCreateDateBetween(customer, start, end);
    }

    public List<Order> listOrders(Customer customer, LocalDate startDate, LocalDate endDate,
                                  OrderStatus status, OrderSide side) {
        List<Order> orders;

        if (customer == null) {
            orders = orderRepository.findAll(); // Admin request for all orders
        } else {
            orders = orderRepository.findByCustomer(customer);
        }

        // Apply optional filters
        return orders.stream()
                .filter(order -> startDate == null || !order.getCreateDate().toLocalDate().isBefore(startDate))
                .filter(order -> endDate == null || !order.getCreateDate().toLocalDate().isAfter(endDate))
                .filter(order -> status == null || order.getStatus() == status)
                .filter(order -> side == null || order.getOrderSide() == side)
                .toList();
    }


    public Order createOrder(Customer customer, String assetName, OrderSide orderSide, int size, double price, LocalDateTime createDate) {
        Optional<Asset> tryAssetOpt = assetService.findAssetByCustomerAndName(customer, "TRY");
        Optional<Asset> assetOpt = assetService.findAssetByCustomerAndName(customer, assetName);

        if (orderSide == OrderSide.SELL) {
            if (assetOpt.isEmpty() || assetOpt.get().getUsableSize() < size) {
                throw new IllegalStateException("Not enough asset to SELL.");
            }

            Asset asset = assetOpt.get();
            asset.setUsableSize(asset.getUsableSize() - size); // Reduce usable only
            assetService.updateAsset(asset);
        }

        if (orderSide == OrderSide.BUY) {
            double totalCost = size * price;
            if (tryAssetOpt.isEmpty() || tryAssetOpt.get().getUsableSize() < totalCost) {
                throw new IllegalStateException("Not enough TRY to BUY.");
            }

            Asset tryAsset = tryAssetOpt.get();
            tryAsset.setUsableSize(tryAsset.getUsableSize() - totalCost); // Reduce usable TRY
            assetService.updateAsset(tryAsset);
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setAssetName(assetName);
        order.setOrderSide(orderSide);
        order.setSize(size);
        order.setPrice(price);
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(createDate);

        return orderRepository.save(order);
    }

    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public void deleteOrder(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot delete non-pending orders.");
        }

        Customer customer = order.getCustomer();

        if (order.getOrderSide() == OrderSide.SELL) {
            Asset asset = assetService.findAssetByCustomerAndName(customer, order.getAssetName())
                    .orElseThrow(() -> new IllegalStateException("Asset not found."));
            asset.setUsableSize(asset.getUsableSize() + order.getSize()); // Restore usable asset
            assetService.updateAsset(asset);
        }

        if (order.getOrderSide() == OrderSide.BUY) {
            Asset tryAsset = assetService.findAssetByCustomerAndName(customer, "TRY")
                    .orElseThrow(() -> new IllegalStateException("TRY asset not found."));
            double refund = order.getSize() * order.getPrice();
            tryAsset.setUsableSize(tryAsset.getUsableSize() + refund); // Restore usable TRY
            assetService.updateAsset(tryAsset);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    // ADMIN only method
    public void matchOrder(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be matched.");
        }

        if (order.getCreateDate().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot match an order before its createDate.");
        }

        Customer customer = order.getCustomer();
        int size = order.getSize();
        double value = order.getSize() * order.getPrice();

        if (order.getOrderSide() == OrderSide.SELL) {
            Asset asset = assetService.findAssetByCustomerAndName(customer, order.getAssetName())
                    .orElseThrow(() -> new IllegalStateException("Asset not found."));
            asset.setSize(asset.getSize() - size); // Reduce total size (usable already reduced)
            assetService.updateAsset(asset);

            assetService.addOrUpdateAsset(customer, "TRY", value, value); // Add to TRY balance
        }

        if (order.getOrderSide() == OrderSide.BUY) {
            Asset asset = assetService.findAssetByCustomerAndName(customer, order.getAssetName())
                    .orElse(null);
            if (asset == null) {
                assetService.addOrUpdateAsset(customer, order.getAssetName(), size, size); // First match creates asset
            } else {
                asset.setSize(asset.getSize() + size); // Increase total size
                assetService.updateAsset(asset);
            }

            Asset tryAsset = assetService.findAssetByCustomerAndName(customer, "TRY")
                    .orElseThrow(() -> new IllegalStateException("TRY asset not found."));
            tryAsset.setSize(tryAsset.getSize() - value); // Reduce total TRY
            assetService.updateAsset(tryAsset);
        }

        order.setStatus(OrderStatus.MATCHED);
        orderRepository.save(order);
    }
}