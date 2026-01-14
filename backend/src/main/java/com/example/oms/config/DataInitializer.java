package com.example.oms.config;

import com.example.oms.model.OrderEntity;
import com.example.oms.model.OrderItem;
import com.example.oms.model.Permission;
import com.example.oms.model.Product;
import com.example.oms.model.Role;
import com.example.oms.model.User;
import com.example.oms.repository.OrderRepository;
import com.example.oms.repository.PermissionRepository;
import com.example.oms.repository.ProductRepository;
import com.example.oms.repository.RoleRepository;
import com.example.oms.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            Permission orderRead = ensurePermission(permissionRepository, "ORDER_READ", "Read orders");
            Permission orderCreate = ensurePermission(permissionRepository, "ORDER_CREATE", "Create orders");
            Permission orderWrite = ensurePermission(permissionRepository, "ORDER_WRITE", "Write orders");

            Role adminRole = roleRepository.findByCode("ROLE_ADMIN").orElseGet(() -> {
                Role r = new Role();
                r.setCode("ROLE_ADMIN");
                r.setName("Admin");
                r.setPermissions(new HashSet<>(List.of(orderRead, orderCreate, orderWrite)));
                return roleRepository.save(r);
            });

            roleRepository.findByCode("ROLE_USER").orElseGet(() -> {
                Role r = new Role();
                r.setCode("ROLE_USER");
                r.setName("User");
                r.setPermissions(new HashSet<>(List.of(orderRead, orderCreate)));
                return roleRepository.save(r);
            });

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = buildUser("admin", "admin123", Set.of(adminRole), passwordEncoder);
                userRepository.save(admin);
            }

            seedUsers(userRepository, passwordEncoder, adminRole, roleRepository.findByCode("ROLE_USER").orElseThrow());
            seedProducts(productRepository);
            seedOrders(orderRepository, userRepository, productRepository);
        };
    }

    private Permission ensurePermission(PermissionRepository repository, String code, String name) {
        return repository.findAll().stream()
                .filter(p -> code.equals(p.getCode()))
                .findFirst()
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode(code);
                    p.setName(name);
                    return repository.save(p);
                });
    }

    private void seedUsers(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Role adminRole,
            Role userRole) {
        List<String> admins = List.of("admin1", "admin2", "admin3", "admin4", "admin5");
        admins.forEach(name -> {
            if (userRepository.findByUsername(name).isEmpty()) {
                userRepository.save(buildUser(name, "Admin123!", Set.of(adminRole), passwordEncoder));
            }
        });

        for (int i = 1; i <= 10; i++) {
            String username = String.format("user%02d", i);
            if (userRepository.findByUsername(username).isEmpty()) {
                userRepository.save(buildUser(username, "User123!", Set.of(userRole), passwordEncoder));
            }
        }
    }

    private User buildUser(String username, String rawPassword, Set<Role> roles, PasswordEncoder encoder) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(rawPassword));
        user.setRoles(roles);
        user.setEnabled(true);
        return user;
    }

    private void seedProducts(ProductRepository productRepository) {
        if (productRepository.count() > 0) {
            return;
        }
        List<Product> products = List.of(
                buildProduct("iPhone 15", new BigDecimal("6999")),
                buildProduct("MacBook Air", new BigDecimal("8999")),
                buildProduct("iPad Pro", new BigDecimal("7699")),
                buildProduct("Apple Watch", new BigDecimal("2999")),
                buildProduct("AirPods Pro", new BigDecimal("1999")),
                buildProduct("ThinkPad X1", new BigDecimal("10999")),
                buildProduct("Dell XPS 13", new BigDecimal("9999")),
                buildProduct("Surface Pro", new BigDecimal("7999")),
                buildProduct("Nintendo Switch", new BigDecimal("2499")),
                buildProduct("PS5", new BigDecimal("3899")),
                buildProduct("Kindle Scribe", new BigDecimal("2099")),
                buildProduct("Sony WH-1000XM5", new BigDecimal("2899")),
                buildProduct("Logitech MX Master 3S", new BigDecimal("799")),
                buildProduct("Keychron K2", new BigDecimal("599")),
                buildProduct("Raspberry Pi 5", new BigDecimal("499")),
                buildProduct("DJI Mini 4", new BigDecimal("4999")),
                buildProduct("GoPro Hero 12", new BigDecimal("4299")),
                buildProduct("Canon EOS R7", new BigDecimal("9999")),
                buildProduct("Sony A7 IV", new BigDecimal("16999")),
                buildProduct("UltraWide Monitor", new BigDecimal("2999"))
        );
        productRepository.saveAll(products);
    }

    private Product buildProduct(String name, BigDecimal price) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(price);
        return p;
    }

    private void seedOrders(OrderRepository orderRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {
        if (orderRepository.count() > 0) {
            return;
        }
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            return;
        }
        Random random = new Random(1L);
        userRepository.findAll().forEach(user -> {
            for (int i = 0; i < 2; i++) {
                OrderEntity order = new OrderEntity();
                order.setUser(user);
                order.setStatus(i % 2 == 0 ? "CREATED" : "PAID");
                BigDecimal total = BigDecimal.ZERO;
                for (int j = 0; j < 2; j++) {
                    Product product = products.get(random.nextInt(products.size()));
                    OrderItem item = new OrderItem();
                    item.setProduct(product);
                    item.setProductName(product.getName());
                    item.setPrice(product.getPrice());
                    int qty = random.nextInt(3) + 1;
                    item.setQuantity(qty);
                    item.setOrder(order);
                    total = total.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
                    order.getItems().add(item);
                }
                order.setTotalAmount(total);
                orderRepository.save(order);
            }
        });
    }
}
