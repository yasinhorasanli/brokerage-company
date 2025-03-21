package com.yasin.brokerage.repository;

import com.yasin.brokerage.model.Customer;
import com.yasin.brokerage.model.Order;
import com.yasin.brokerage.model.OrderSide;
import com.yasin.brokerage.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = customerRepository.save(new Customer(null, "testUser", "password123", "CUSTOMER", null, null));
        orderRepository.save(new Order(null, customer, "AAPL", OrderSide.BUY, 5, 150.0, OrderStatus.PENDING, LocalDateTime.now()));
    }

    @Test
    void testFindByCustomer() {
        List<Order> orders = orderRepository.findByCustomer(customer);
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getAssetName()).isEqualTo("AAPL");
    }

    @Test
    void testFindByStatus() {
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        assertThat(pendingOrders).isNotEmpty();
    }
}