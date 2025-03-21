package com.yasin.brokerage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@SuppressWarnings("JpaDataSourceORMInspection")
@Table(name = "assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;

    @Column(nullable = false)
    private String assetName;

    @Column(nullable = false)
    private double size;

    @Column(nullable = false)
    private double usableSize;

    @Override
    public String toString() {
        return "Asset{" +
                "id=" + id +
                ", assetName='" + assetName + '\'' +
                ", size=" + size +
                ", usableSize=" + usableSize +
                ", customer=" + customer.getUsername() +
                '}';
    }
}