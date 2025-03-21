package com.yasin.brokerage.service;

import com.yasin.brokerage.model.*;
import com.yasin.brokerage.repository.AssetRepository;
import com.yasin.brokerage.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus(OrderStatus.PENDING);
    }

    public Order createOrder(Customer customer, String assetName, OrderSide orderSide, int size, double price) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setAssetName(assetName);
        order.setOrderSide(orderSide);
        order.setSize(size);
        order.setPrice(price);
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());

        return orderRepository.save(order);
    }

    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public void deleteOrder(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot delete non-pending orders.");
        }

        Optional<Order> existingOrder = orderRepository.findById(order.getId());
        if (existingOrder.isEmpty()) {
            throw new IllegalStateException("Order not found.");
        }

        orderRepository.delete(order);
    }
}