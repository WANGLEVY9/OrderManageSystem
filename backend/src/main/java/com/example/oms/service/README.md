# service 文件夹说明

本文件夹包含所有业务逻辑层服务类，负责核心业务处理、事务管理、数据校验和业务编排。

---

## 1. AuthService.java
- **作用**：认证服务，处理用户登录、注册和 Token 生成。
- **关键实现**：
  - `login(LoginRequest request)`：验证用户名/手机号和密码，生成 JWT Token。
  - `register(RegisterRequest request)`：新用户注册，密码加密，分配默认角色。
  - 使用 `PasswordEncoder` 加密密码，调用 `JwtUtil` 生成 Token。

## 2. UserService.java
- **作用**：用户管理服务，处理用户 CRUD、角色分配等。
- **关键实现**：
  - `findAll()`：查询所有用户。
  - `updateUser(Long id, UserUpdateRequest request)`：更新用户信息。
  - `findRoles()`：查询所有角色。
  - `findById(Long id)`：根据 ID 查询用户。
  - `findByUsername(String username)`：根据用户名查询。
  - 实现 `UserDetailsService` 接口，供 Spring Security 使用。

## 3. OrderService.java
- **作用**：订单管理服务，处理订单创建、状态更新、查询、删除等核心业务。
- **关键实现**：
  - `createOrder(User operator, User owner, OrderCreateRequest request)`：创建订单，计算总价，保存明细，发送 Kafka 消息。
  - `updateStatus(Long id, String status, String operator)`：更新订单状态，记录操作日志。
  - `listOrders(User user, int page, int size)`：分页查询用户订单。
  - `listAll(int page, int size)`：管理员分页查询所有订单。
  - `replaceItems(Long id, List<OrderItemRequest> items, String operator)`：替换订单明细，重新计算总价。
  - `deleteOrder(Long id, String operator)`：删除订单，记录日志。
  - `getLogs(Long id)`：查询订单操作日志。
  - 使用 `@Transactional` 保证事务一致性。

## 4. ProductService.java
- **作用**：商品管理服务，处理商品创建和查询。
- **关键实现**：
  - `listAll()`：查询所有商品。
  - `create(ProductRequest request)`：创建新商品。
  - 校验商品信息，管理库存。

## 5. StatsService.java
- **作用**：统计服务，提供业务数据汇总和排名。
- **关键实现**：
  - `allCounters()`：统计关键指标（如订单总数、销售额、用户数等）。
  - `topRank(int limit)`：查询销售排名前 N 的商品。
  - 使用 Redis 缓存统计结果，提高性能。

## 6. OrderEventConsumer.java
- **作用**：Kafka 消息消费者，异步处理订单事件。
- **关键实现**：
  - 使用 `@KafkaListener` 监听订单主题。
  - 处理订单创建、状态变更等事件，实现业务解耦。
  - 示例：发送通知、更新缓存、同步数据等。

---

所有服务类均使用 `@Service` 注解标记，通过依赖注入调用 Repository 和其他服务，遵循单一职责原则和分层架构。