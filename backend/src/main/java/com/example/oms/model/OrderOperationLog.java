package com.example.oms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "order_operation_logs")
@Getter
@Setter
public class OrderOperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String operator;

    @Column
    private String remark;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
