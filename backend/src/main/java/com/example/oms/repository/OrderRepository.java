package com.example.oms.repository;

import com.example.oms.model.OrderEntity;
import com.example.oms.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Page<OrderEntity> findByUser(User user, Pageable pageable);
}
