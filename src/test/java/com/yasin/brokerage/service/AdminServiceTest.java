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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private OrderService orderService;
    @Mock
    private AssetService assetService;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AdminService adminService;

    private Customer customer;
    private Order buyOrder;
    private Order sellOrder;
    private Asset tryAsset;
    private Asset stockAsset;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setUsername("testUser");
        buyOrder = new Order(1L, customer, "AAPL", OrderSide.BUY, 5, 150.0, OrderStatus.PENDING, LocalDateTime.now());
        sellOrder = new Order(2L, customer, "NVDA", OrderSide.SELL, 10, 200.0, OrderStatus.PENDING, LocalDateTime.now());
        tryAsset = new Asset(null, customer, "TRY", 10000, 10000);
        stockAsset = new Asset(null, customer, "AAPL", 10, 10);
    }

    @Test
    void testMatchBuyOrder() {
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(buyOrder));
        when(assetService.findAssetByCustomerAndName(customer, "TRY")).thenReturn(Optional.of(tryAsset));
        when(assetService.findAssetByCustomerAndName(customer, "AAPL")).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminService.matchOrder(1L);

        // Reduce TRY
        assertThat(tryAsset.getUsableSize()).isEqualTo(10000 - (5 * 150.0));

        // Add Stock
        verify(assetService, times(1)).addOrUpdateAsset(customer, "AAPL", 5, 5);

        // Mark order as MATCHED
        assertThat(buyOrder.getStatus()).isEqualTo(OrderStatus.MATCHED);
        verify(orderRepository, times(1)).save(buyOrder);
    }

    @Test
    void testMatchSellOrder() {
        when(orderService.getOrderById(2L)).thenReturn(Optional.of(sellOrder));
        when(assetService.findAssetByCustomerAndName(customer, "TRY")).thenReturn(Optional.of(tryAsset));
        when(assetService.findAssetByCustomerAndName(customer, "NVDA")).thenReturn(Optional.of(stockAsset));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminService.matchOrder(2L);

        assertThat(stockAsset.getUsableSize()).isEqualTo(10 - 10);

        verify(assetService, times(1)).addOrUpdateAsset(customer, "TRY", 10 * 200.0, 10 * 200.0);

        assertThat(sellOrder.getStatus()).isEqualTo(OrderStatus.MATCHED);
        verify(orderRepository, times(1)).save(sellOrder);
    }

    @Test
    void testCannotMatchAlreadyMatchedOrder() {
        buyOrder.setStatus(OrderStatus.MATCHED);

        when(orderService.getOrderById(1L)).thenReturn(Optional.of(buyOrder));

        IllegalStateException exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            adminService.matchOrder(1L);
        });

        assertThat(exception.getMessage()).isEqualTo("Only PENDING orders can be matched.");
        verify(orderRepository, times(0)).save(any(Order.class));
    }

    @Test
    void testCannotMatchNonExistentOrder() {
        when(orderService.getOrderById(99L)).thenReturn(Optional.empty());

        IllegalStateException exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            adminService.matchOrder(99L);
        });

        assertThat(exception.getMessage()).isEqualTo("Order not found.");
    }
}
