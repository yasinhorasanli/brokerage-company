package com.yasin.brokerage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;

    @Column(nullable = false)
    private String assetName; // should not be TRY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide orderSide; // BUY or SELL

    @Column(nullable = false)
    private int size;

    @Column(nullable = false)
    private double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customer=" + customer.getUsername() +
                ", assetName='" + assetName + '\'' +
                ", orderSide=" + orderSide +
                ", size=" + size +
                ", price=" + price +
                ", status=" + status +
                ", createDate=" + createDate +
                '}';
    }
}

