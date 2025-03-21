package com.yasin.brokerage.repository;

import com.yasin.brokerage.model.Customer;
import com.yasin.brokerage.model.Order;
import com.yasin.brokerage.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer(Customer customer);
    List<Order> findByCustomerAndCreateDateBetween(Customer customer, LocalDateTime start, LocalDateTime end);
    List<Order> findByStatus(OrderStatus status);
}