# repository 文件夹说明

本文件夹包含所有数据访问层接口（Repository），继承 Spring Data JPA 接口，提供对数据库的 CRUD 操作和自定义查询。

---

## 1. UserRepository.java
- **作用**：用户数据访问接口。
- **关键实现**：
  - 继承 `JpaRepository<User, Long>`，自动提供基础 CRUD。
  - 自定义查询方法：
    - `Optional<User> findByUsername(String username)`：根据用户名查询。
    - `Optional<User> findByPhone(String phone)`：根据手机号查询。
    - `boolean existsByUsername(String username)`：检查用户名是否存在。

## 2. RoleRepository.java
- **作用**：角色数据访问接口。
- **关键实现**：
  - 继承 `JpaRepository<Role, Long>`。
  - 自定义查询方法：
    - `Optional<Role> findByRoleName(String roleName)`：根据角色名查询。

## 3. PermissionRepository.java
- **作用**：权限数据访问接口。
- **关键实现**：
  - 继承 `JpaRepository<Permission, Long>`。
  - 自定义查询方法：
    - `Optional<Permission> findByPermissionName(String permissionName)`：根据权限名查询。

## 4. OrderRepository.java
- **作用**：订单数据访问接口。
- **关键实现**：
  - 继承 `JpaRepository<OrderEntity, Long>`。
  - 自定义查询方法：
    - `Page<OrderEntity> findByUser(User user, Pageable pageable)`：分页查询用户订单。
    - `Optional<OrderEntity> findByOrderNumber(String orderNumber)`：根据订单号查询。
    - `List<OrderEntity> findByStatus(String status)`：根据状态查询。

## 5. OrderOperationLogRepository.java
- **作用**：订单操作日志数据访问接口。
- **关键实现**：
  - 继承 `JpaRepository<OrderOperationLog, Long>`。
  - 自定义查询方法：
    - `List<OrderOperationLog> findByOrderIdOrderByOperationTimeDesc(Long orderId)`：查询订单日志，按时间倒序。

## 6. ProductRepository.java
- **作用**：商品数据访问接口。
- **关键实现**：
  - 继承 `JpaRepository<Product, Long>`。
  - 自定义查询方法：
    - `List<Product> findByNameContaining(String name)`：模糊查询商品名。
    - `List<Product> findByStockGreaterThan(Integer stock)`：查询库存充足的商品。

---

所有 Repository 接口利用 Spring Data JPA 的方法命名规则，无需编写实现类即可自动生成 SQL，大幅简化数据访问层开发。