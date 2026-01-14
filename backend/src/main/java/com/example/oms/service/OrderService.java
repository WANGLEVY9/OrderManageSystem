package com.example.oms.service;

import com.example.oms.dto.OrderCreateRequest;
import com.example.oms.dto.OrderItemRequest;
import com.example.oms.model.OrderEntity;
import com.example.oms.model.OrderItem;
import com.example.oms.model.OrderOperationLog;
import com.example.oms.model.Product;
import com.example.oms.model.User;
import com.example.oms.repository.OrderOperationLogRepository;
import com.example.oms.repository.OrderRepository;
import com.example.oms.repository.ProductRepository;
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
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderService(OrderRepository orderRepository,
            OrderOperationLogRepository logRepository,
            ProductRepository productRepository,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.logRepository = logRepository;
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public OrderEntity createOrder(User operator, User owner, OrderCreateRequest request) {
        OrderEntity order = new OrderEntity();
        order.setUser(owner);
        order.setStatus("CREATED");
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = new OrderItem();
            if (itemRequest.getProductId() != null) {
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found"));
                item.setProduct(product);
                item.setProductName(product.getName());
                item.setPrice(product.getPrice());
                item.setImageUrl(product.getImageUrl());
            } else {
                item.setProductName(itemRequest.getProductName());
                item.setPrice(itemRequest.getPrice());
                item.setImageUrl(itemRequest.getImageUrl());
            }
            item.setQuantity(itemRequest.getQuantity());
            item.setOrder(order);
            BigDecimal price = item.getPrice();
            total = total.add(price.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            order.getItems().add(item);
        }
        order.setTotalAmount(total);
        OrderEntity saved = orderRepository.save(order);
        logAction(saved, operator.getUsername(), "CREATE", "Order created for " + owner.getUsername());
        kafkaTemplate.send("order-topic", "OrderCreated", "Order " + saved.getId() + " created");
        return saved;
    }

    @Transactional
    public OrderEntity createOrder(User user, OrderCreateRequest request) {
        return createOrder(user, user, request);
    }

    @Transactional
    public OrderEntity replaceItems(Long orderId, List<OrderItemRequest> items, String operator) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.getItems().clear();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : items) {
            OrderItem item = new OrderItem();
            if (itemRequest.getProductId() != null) {
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found"));
                item.setProduct(product);
                item.setProductName(product.getName());
                item.setPrice(product.getPrice());
                item.setImageUrl(product.getImageUrl());
            } else {
                item.setProductName(itemRequest.getProductName());
                item.setPrice(itemRequest.getPrice());
                item.setImageUrl(itemRequest.getImageUrl());
            }
            item.setQuantity(itemRequest.getQuantity());
            item.setOrder(order);
            BigDecimal price = item.getPrice();
            total = total.add(price.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            order.getItems().add(item);
        }
        order.setTotalAmount(total);
        OrderEntity saved = orderRepository.save(order);
        logAction(saved, operator, "ITEMS", "Items replaced");
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

    @Transactional
    public void deleteOrder(Long id, String operator) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        logAction(order, operator, "DELETE", "Order deleted");
        orderRepository.delete(order);
        kafkaTemplate.send("order-topic", "OrderDeleted", "Order " + id + " deleted");
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
