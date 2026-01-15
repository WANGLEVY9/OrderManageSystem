# controller 文件夹说明

本文件夹包含所有后端接口控制器，负责处理 HTTP 请求、参数校验、权限控制、业务分发和响应封装，是系统 API 的入口。

---

## 1. AuthController.java
- **作用**：认证相关接口，包括用户登录和注册。
- **关键实现**：
  - `/api/auth/login`：校验用户身份，返回 JWT Token。
  - `/api/auth/register`：新用户注册。
  - 用 `@RestController`、`@PostMapping`、`@Valid` 参数校验。

## 2. UserController.java
- **作用**：用户管理接口，包括用户列表、更新、角色查询。
- **关键实现**：
  - `/api/users`：分页/列表查询所有用户（需管理员权限）。
  - `/api/users/{id}`：更新指定用户信息。
  - `/api/users/roles`：查询所有角色。
  - 用 `@PreAuthorize` 控制权限。

## 3. OrderController.java
- **作用**：订单管理接口，包括订单创建、查询、状态更新、明细替换、删除、审计日志。
- **关键实现**：
  - `/api/orders`：创建订单。
  - `/api/orders/mine`：当前用户查询自己的订单。
  - `/api/orders/by-user/{userId}`：管理员查询指定用户订单。
  - `/api/orders/{id}/status`：管理员更新订单状态。
  - `/api/orders/{id}/items`：管理员替换订单明细。
  - `/api/orders/{id}/logs`：查询订单操作日志。
  - `/api/orders/{id}`：删除订单。

## 4. ProductController.java
- **作用**：商品管理接口，包括商品列表和创建。
- **关键实现**：
  - `/api/products`：查询所有商品。
  - `/api/products`（POST）：管理员创建商品。

## 5. StatsController.java
- **作用**：统计相关接口，包括汇总指标和产品销售排名。
- **关键实现**：
  - `/api/stats/counters`：管理员查询关键指标（如订单数、销售额等）。
  - `/api/stats/rank`：管理员查询产品销售排名。

## 6. HealthController.java
- **作用**：健康检查接口。
- **关键实现**：
  - `/health`：返回服务存活状态（"ok"），用于监控和探针。

---

所有控制器均采用 RESTful 风格，结合注解实现参数校验、权限控制和分组文档说明。