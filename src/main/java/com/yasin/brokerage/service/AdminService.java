package com.yasin.brokerage.service;

import com.yasin.brokerage.model.*;
import com.yasin.brokerage.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private OrderRepository orderRepository;

    public void matchOrder(Long orderId) {
        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalStateException("Order not found.");
        }

        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be matched.");
        }

        Customer customer = order.getCustomer();
        Optional<Asset> tryAssetOpt = assetService.findAssetByCustomerAndName(customer, "TRY");
        Optional<Asset> stockAssetOpt = assetService.findAssetByCustomerAndName(customer, order.getAssetName());

        if (order.getOrderSide() == OrderSide.BUY) {
            // Reduce TRY
            if (tryAssetOpt.isEmpty() || tryAssetOpt.get().getUsableSize() < order.getSize() * order.getPrice()) {
                throw new IllegalStateException("Not enough TRY balance to match this BUY order.");
            }

            Asset tryAsset = tryAssetOpt.get();
            tryAsset.setUsableSize(tryAsset.getUsableSize() - (order.getSize() * order.getPrice()));
            assetService.updateAsset(tryAsset);

            // Add stock
            assetService.addOrUpdateAsset(customer, order.getAssetName(), order.getSize(), order.getSize());
        }

        if (order.getOrderSide() == OrderSide.SELL) {
            // Reduce stock
            if (stockAssetOpt.isEmpty() || stockAssetOpt.get().getUsableSize() < order.getSize()) {
                throw new IllegalStateException("Not enough stock to match this SELL order.");
            }

            Asset stockAsset = stockAssetOpt.get();
            stockAsset.setUsableSize(stockAsset.getUsableSize() - order.getSize());
            assetService.updateAsset(stockAsset);

            // Add TRY
            assetService.addOrUpdateAsset(customer, "TRY", order.getSize() * order.getPrice(), order.getSize() * order.getPrice());
        }

        // Mark order as MATCHED
        order.setStatus(OrderStatus.MATCHED);
        orderRepository.save(order);
    }
}