package com.example.oms.repository;

import com.example.oms.model.OrderEntity;
import com.example.oms.model.OrderOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderOperationLogRepository extends JpaRepository<OrderOperationLog, Long> {
    List<OrderOperationLog> findByOrder(OrderEntity order);
}
