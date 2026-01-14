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
            // ensure permissions synced even if role already exists
            adminRole.setPermissions(new HashSet<>(List.of(orderRead, orderCreate, orderWrite)));
            roleRepository.save(adminRole);

            roleRepository.findByCode("ROLE_USER").orElseGet(() -> {
                Role r = new Role();
                r.setCode("ROLE_USER");
                r.setName("User");
                r.setPermissions(new HashSet<>(List.of(orderRead, orderCreate)));
                return roleRepository.save(r);
            });
            // refresh ROLE_USER permissions to avoid stale mapping
            roleRepository.findByCode("ROLE_USER").ifPresent(r -> {
                r.setPermissions(new HashSet<>(List.of(orderRead, orderCreate)));
                roleRepository.save(r);
            });

            // Ensure admin账号存在且密码/角色正确（防止历史数据缺失导致无法登录）
            User admin = userRepository.findByUsername("admin").orElseGet(User::new);
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEnabled(true);
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);

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
                buildProduct("iPhone 15", new BigDecimal("6999"), "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600"),
                buildProduct("MacBook Air", new BigDecimal("8999"), "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600"),
                buildProduct("iPad Pro", new BigDecimal("7699"), "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=600"),
                buildProduct("Apple Watch", new BigDecimal("2999"), "https://images.unsplash.com/photo-1519741497674-611481863552?w=600"),
                buildProduct("AirPods Pro", new BigDecimal("1999"), "https://images.unsplash.com/photo-1588423771073-b8903fbb85b5?w=600"),
                buildProduct("ThinkPad X1", new BigDecimal("10999"), "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600"),
                buildProduct("Dell XPS 13", new BigDecimal("9999"), "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600"),
                buildProduct("Surface Pro", new BigDecimal("7999"), "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600"),
                buildProduct("Nintendo Switch", new BigDecimal("2499"), "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=600"),
                buildProduct("PS5", new BigDecimal("3899"), "https://images.unsplash.com/photo-1593642634315-48f5414c3ad9?w=600"),
                buildProduct("Kindle Scribe", new BigDecimal("2099"), "https://images.unsplash.com/photo-1513475382585-d06e58bcb0e0?w=600"),
                buildProduct("Sony WH-1000XM5", new BigDecimal("2899"), "https://images.unsplash.com/photo-1518443952241-2b51c0c0c31c?w=600"),
                buildProduct("Logitech MX Master 3S", new BigDecimal("799"), "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600"),
                buildProduct("Keychron K2", new BigDecimal("599"), "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=600"),
                buildProduct("Raspberry Pi 5", new BigDecimal("499"), "https://images.unsplash.com/photo-1518770660439-4636190af475?w=600"),
                buildProduct("DJI Mini 4", new BigDecimal("4999"), "https://images.unsplash.com/photo-1508615039623-a25605d2b022?w=600"),
                buildProduct("GoPro Hero 12", new BigDecimal("4299"), "https://images.unsplash.com/photo-1489515217757-5fd1be406fef?w=600"),
                buildProduct("Canon EOS R7", new BigDecimal("9999"), "https://images.unsplash.com/photo-1416339306562-f3d12fefd36f?w=600"),
                buildProduct("Sony A7 IV", new BigDecimal("16999"), "https://images.unsplash.com/photo-1416339306562-f3d12fefd36f?w=600"),
                buildProduct("UltraWide Monitor", new BigDecimal("2999"), "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600")
        );
        productRepository.saveAll(products);
    }

    private Product buildProduct(String name, BigDecimal price, String imageUrl) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(price);
        p.setImageUrl(imageUrl);
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
