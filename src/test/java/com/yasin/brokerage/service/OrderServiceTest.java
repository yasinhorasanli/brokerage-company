package com.yasin.brokerage.service;

import com.yasin.brokerage.model.*;
import com.yasin.brokerage.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Order buyOrder;
    private Order sellOrder;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setUsername("testUser");

        buyOrder = new Order();
        buyOrder.setId(1L);
        buyOrder.setCustomer(customer);
        buyOrder.setAssetName("AAPL");
        buyOrder.setOrderSide(OrderSide.BUY);
        buyOrder.setSize(5);
        buyOrder.setPrice(150.0);
        buyOrder.setStatus(OrderStatus.PENDING);
        buyOrder.setCreateDate(LocalDateTime.now());

        sellOrder = new Order();
        sellOrder.setId(2L);
        sellOrder.setCustomer(customer);
        sellOrder.setAssetName("NVDA");
        sellOrder.setOrderSide(OrderSide.SELL);
        sellOrder.setSize(10);
        sellOrder.setPrice(200.0);
        sellOrder.setStatus(OrderStatus.MATCHED);
        sellOrder.setCreateDate(LocalDateTime.now());
    }

    @Test
    void testGetOrdersByCustomer() {
        when(orderRepository.findByCustomer(customer)).thenReturn(List.of(buyOrder, sellOrder));

        List<Order> orders = orderService.getOrdersByCustomer(customer);

        assertThat(orders).hasSize(2);
        assertThat(orders).extracting(Order::getAssetName).containsExactlyInAnyOrder("AAPL", "NVDA");
    }

    @Test
    void testCreateOrder() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order newOrder = orderService.createOrder(customer, "TSLA", OrderSide.BUY, 3, 650.0);

        assertThat(newOrder.getAssetName()).isEqualTo("TSLA");
        assertThat(newOrder.getSize()).isEqualTo(3);
        assertThat(newOrder.getOrderSide()).isEqualTo(OrderSide.BUY);
        assertThat(newOrder.getStatus()).isEqualTo(OrderStatus.PENDING);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testFindOrderById() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(buyOrder));

        Optional<Order> foundOrder = orderService.getOrderById(1L);

        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getAssetName()).isEqualTo("AAPL");
    }

    @Test
    void testDeletePendingOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(buyOrder));
        doNothing().when(orderRepository).delete(buyOrder);

        orderService.deleteOrder(buyOrder);

        verify(orderRepository, times(1)).delete(buyOrder);
    }

    @Test
    void testCannotDeleteMatchedOrder() {

        IllegalStateException exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            orderService.deleteOrder(sellOrder);
        });

        assertThat(exception.getMessage()).isEqualTo("Cannot delete non-pending orders.");

        verify(orderRepository, times(0)).delete(sellOrder);
    }

    @Test
    void testGetOrdersByDateRange() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        when(orderRepository.findByCustomerAndCreateDateBetween(customer, start, end)).thenReturn(List.of(buyOrder));

        List<Order> orders = orderService.getOrdersByDateRange(customer, start, end);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getAssetName()).isEqualTo("AAPL");
    }
}
