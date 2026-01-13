package com.example.oms.service;

import com.example.oms.dto.OrderCreateRequest;
import com.example.oms.dto.OrderItemRequest;
import com.example.oms.model.OrderEntity;
import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderOperationLog;
import com.example.oms.model.User;
import com.example.oms.repository.OrderOperationLogRepository;
import com.example.oms.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderOperationLogRepository logRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderService(OrderRepository orderRepository,
                        OrderOperationLogRepository logRepository,
                        KafkaTemplate<String, String> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.logRepository = logRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public OrderEntity createOrder(User user, OrderCreateRequest request) {
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setStatus("CREATED");
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setProductName(itemRequest.getProductName());
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(itemRequest.getPrice());
            item.setOrder(order);
            total = total.add(itemRequest.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            order.getItems().add(item);
        }
        order.setTotalAmount(total);
        OrderEntity saved = orderRepository.save(order);
        logAction(saved, user.getUsername(), "CREATE", "Order created");
        kafkaTemplate.send("order-topic", "OrderCreated", "Order " + saved.getId() + " created");
        return saved;
    }

    @Transactional
    public OrderEntity updateStatus(Long orderId, String status, String operator) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(status);
        OrderEntity saved = orderRepository.save(order);
        logAction(saved, operator, "STATUS", "Status -> " + status);
        kafkaTemplate.send("order-topic", "OrderStatus", "Order " + saved.getId() + " -> " + status);
        return saved;
    }

    public Page<OrderEntity> listOrders(User user, int page, int size) {
        return orderRepository.findByUser(user, PageRequest.of(page, size));
    }

    public Page<OrderEntity> listAll(int page, int size) {
        return orderRepository.findAll(PageRequest.of(page, size));
    }

    public List<OrderOperationLog> getLogs(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return logRepository.findByOrder(order);
    }

    private void logAction(OrderEntity order, String operator, String action, String remark) {
        OrderOperationLog log = new OrderOperationLog();
        log.setOrder(order);
        log.setOperator(operator);
        log.setAction(action);
        log.setRemark(remark);
        logRepository.save(log);
    }
}
