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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private AssetService assetService;

    @InjectMocks private OrderService orderService;

    private Customer customer;
    private Order buyOrder, sellOrder, futureOrder;
    private Asset tryAsset, stockAsset;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setUsername("testUser");

        tryAsset = new Asset(null, customer, "TRY", 10000, 10000);
        stockAsset = new Asset(null, customer, "AAPL", 10, 10);

        buyOrder = new Order(1L, customer, "AAPL", OrderSide.BUY, 2, 100.0, OrderStatus.PENDING, LocalDateTime.now());
        sellOrder = new Order(2L, customer, "AAPL", OrderSide.SELL, 2, 100.0, OrderStatus.PENDING, LocalDateTime.now());
        futureOrder = new Order(99L, customer, "AAPL", OrderSide.BUY, 5, 100.0, OrderStatus.PENDING, LocalDateTime.now().plusDays(2));
    }

    // -----------------------
    // OrderService methods
    // -----------------------

    @Test
    void testCreateBuyOrder() {
        when(assetService.findAssetByCustomerAndName(customer, "TRY")).thenReturn(Optional.of(tryAsset));
        when(assetService.findAssetByCustomerAndName(customer, "AAPL")).thenReturn(Optional.of(stockAsset));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order createdOrder = orderService.createOrder(customer, "AAPL", OrderSide.BUY, 2, 100.0, null);

        assertThat(createdOrder.getAssetName()).isEqualTo("AAPL");
        verify(assetService).updateAsset(any(Asset.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testDeleteBuyOrder() {
        when(assetService.findAssetByCustomerAndName(customer, "TRY"))
                .thenReturn(Optional.of(tryAsset));

        orderService.deleteOrder(buyOrder);

        verify(assetService).updateAsset(any(Asset.class));
        verify(orderRepository).save(buyOrder);
        assertThat(buyOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }


    @Test
    void testCreateSellOrder() {
        when(assetService.findAssetByCustomerAndName(customer, "TRY")).thenReturn(Optional.of(tryAsset));
        when(assetService.findAssetByCustomerAndName(customer, "AAPL")).thenReturn(Optional.of(stockAsset));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order createdOrder = orderService.createOrder(customer, "AAPL", OrderSide.SELL, 2, 100.0, null );

        assertThat(createdOrder.getOrderSide()).isEqualTo(OrderSide.SELL);
        verify(assetService).updateAsset(any(Asset.class));
    }

    @Test
    void testDeleteSellOrder() {
        when(assetService.findAssetByCustomerAndName(customer, "AAPL"))
                .thenReturn(Optional.of(stockAsset));

        orderService.deleteOrder(sellOrder);

        verify(assetService).updateAsset(any(Asset.class));
        verify(orderRepository).save(sellOrder);
        assertThat(sellOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }


    @Test
    void testGetOrdersByCustomer() {
        when(orderRepository.findByCustomer(customer)).thenReturn(List.of(buyOrder, sellOrder));

        List<Order> orders = orderService.getOrdersByCustomer(customer);

        assertThat(orders).hasSize(2);
        assertThat(orders).extracting(Order::getAssetName).containsExactlyInAnyOrder("AAPL", "AAPL");
    }

    @Test
    void testGetOrderById() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(buyOrder));

        Optional<Order> foundOrder = orderService.getOrderById(1L);

        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getAssetName()).isEqualTo("AAPL");
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

    // -----------------------
    // Admin-only: matchOrder
    // -----------------------

    @Test
    void testMatchBuyOrder() {
        when(assetService.findAssetByCustomerAndName(customer, "AAPL")).thenReturn(Optional.of(stockAsset));
        when(assetService.findAssetByCustomerAndName(customer, "TRY")).thenReturn(Optional.of(tryAsset));

        orderService.matchOrder(buyOrder);

        verify(assetService, atLeastOnce()).updateAsset(any(Asset.class));
        verify(orderRepository).save(buyOrder);
    }

    @Test
    void testMatchSellOrder() {
        when(assetService.findAssetByCustomerAndName(customer, "AAPL")).thenReturn(Optional.of(stockAsset));

        orderService.matchOrder(sellOrder);

        verify(assetService, atLeastOnce()).updateAsset(any(Asset.class));
        verify(orderRepository).save(sellOrder);
    }

    @Test
    void testMatchOrder_AlreadyMatched() {
        buyOrder.setStatus(OrderStatus.MATCHED);

        assertThrows(IllegalStateException.class, () -> orderService.matchOrder(buyOrder));
    }

    @Test
    void testMatchOrder_AssetNotFound() {
        when(assetService.findAssetByCustomerAndName(customer, "AAPL")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> orderService.matchOrder(sellOrder));
    }

    @Test
    void testMatchOrderBeforeCreateDate_fails() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> orderService.matchOrder(futureOrder));

        assertThat(exception.getMessage()).isEqualTo("Cannot match an order before its createDate.");

        verify(orderRepository, never()).save(any());
        verify(assetService, never()).updateAsset(any());
    }

}


