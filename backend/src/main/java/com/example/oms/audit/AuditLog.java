package com.example.oms.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String httpMethod;

    @Column(nullable = false)
    private String action;

    private String ip;

    @Column(nullable = false)
    private Boolean success;

    private Long durationMs;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
