package com.example.oms.service;

import com.example.oms.dto.ProductRequest;
import com.example.oms.model.Product;
import com.example.oms.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> listAll() {
        return productRepository.findAll();
    }

    @Transactional
    public Product create(ProductRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Product name already exists");
        }
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        return productRepository.save(product);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }
}
