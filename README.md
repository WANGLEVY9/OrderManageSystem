## 一、项目选型与总体目标

### 1. 项目类型选择（最高复杂度）

**选择：订单管理系统（Order Management System）**

理由：

* 业务流程天然复杂（下单 → 支付 → 发货 → 完成 / 取消）
* 非常适合：

  * Kafka 异步消息
  * Redis 多数据结构
  * 权限控制（用户 / 管理员）
  * 接口访问统计
* 比学生管理系统更“贴近真实生产系统”，更容易写出高质量总结文档

---

## 快速开始

```bash
# 后端
cd backend
mvn spring-boot:run

# 前端
cd ../frontend
npm install
npm run dev

# 编排（MySQL/Redis/Kafka/后端/前端）
docker-compose up -d
```

默认账号：admin / admin123（管理员），注册接口可创建普通用户。

---

## 二、总体技术架构（最高级别）

### 1. 架构风格

**前后端完全分离 + 微服务化设计思想（单体实现）**

```
[ Vue3 前端 ]
        |
        |  HTTPS + JWT
        v
[ Spring Boot 后端 ]
  ├── API Gateway（Spring Security）
  ├── Auth 模块
  ├── Order 模块
  ├── User 模块
  ├── Statistics 模块（AOP）
  ├── Kafka Producer / Consumer
  ├── Redis Cache / Session / RBAC
  └── MySQL / PostgreSQL
```

实现要点：
- Spring Boot 3.2 + Java 17，JWT + Spring Security + RBAC（角色/方法级 `@PreAuthorize`）。
- 数据持久化：Spring Data JPA（默认），同时提供 MyBatis 示例（`OrderMapper#countOrders`）便于切换。
- Redis：登录态缓存、接口调用计数（String）、排行榜（ZSet），可扩展 List 队列等。
- Kafka：下单/状态变更生产消息，消费者记录日志。
- AOP：统一拦截 RestController，记录接口调用次数并写入 Redis 排行榜。
- OpenAPI/Swagger UI：`/swagger-ui.html`。
- 部署：Dockerfile（前后端）、docker-compose（mysql/redis/kafka/backend/frontend），可扩展 K8s。

---

## 三、前端实现方案（20 分 —— 满分）

### 1. 技术选型（最高级）

| 要求    | 方案                      |
| ----- | ----------------------- |
| 前端框架  | **Vue 3 + Vite**        |
| UI 框架 | **Element Plus**        |
| 状态管理  | **Pinia**               |
| 路由    | **Vue Router（多级嵌套路由）**  |
| 请求    | Axios                   |
| 鉴权    | JWT + Axios Interceptor |

### 2. 前端评分点逐条对齐

#### 2.1 前后端分离（10 分）

* Vue 单独工程
* 通过 RESTful API 与后端通信
* Token 存储在 localStorage

#### 2.2 页面跳转逻辑（满分）

* 单层跳转：登录 → 首页
* 多层跳转：

  ```
  首页
   └── 订单管理
        ├── 订单列表
        │    └── 订单详情
        │         └── 操作记录
  ```

#### 2.3 图片与样式（满分）

* 商品图片（URL 存储在数据库）
* CSS：

  * Element Plus 主题定制
  * Flex / Grid 布局
* 响应式页面

---

## 四、安全管理（20 分 —— 全部最高级）

### 1. 用户登录（最高级方案）

**Redis + 数据库 + JWT**

#### 登录流程

```
用户登录
 → 校验数据库用户
 → BCrypt 校验密码
 → 生成 JWT
 → 登录状态写入 Redis
```

| 子项    | 实现     |
| ----- | ------ |
| 密码加密  | BCrypt |
| 用户数据  | MySQL  |
| 会话    | Redis  |
| Token | JWT    |

---

### 2. 权限控制（RBAC + 方法级）

#### 权限模型（最高级）

```
User —— Role —— Permission
```

* 表设计：

  * user
  * role
  * permission
  * user_role
  * role_permission

#### 权限控制方式（全部实现）

| 要求    | 实现               |
| ----- | ---------------- |
| 权限控制  | Spring Security  |
| 基于角色  | `hasRole()`      |
| 基于方法  | `@PreAuthorize`  |
| 权限持久化 | 数据库存储 + Redis 缓存 |

---

## 五、基本功能（30 分 —— 全满）

### 1. 接口调用统计（10 分）

**AOP + Redis + 前端可视化**

#### 实现方式

* 使用 `@Around` AOP 拦截 Controller
* Redis：

  * `String`：接口调用次数
  * `ZSet`：接口访问排行榜
* 前端：

  * 管理员页面展示统计图表（ECharts）

后端已通过 AOP 自动埋点；前端 `/stats` 与 Dashboard 展示调用总数及排行榜。

---

### 2. 数据持久化（10 分）

#### 当前实现

* **Spring Data JPA**

  * 自动建表
  * 分页
  * 复杂查询

#### 框架可切换设计（10 分关键点）

* Repository 层与 Service 解耦
* 提供 MyBatis Mapper 示例
* 文档中说明“如何从 JPA 切换至 MyBatis”

切换步骤：
- 在 `application.yml` 关闭 `spring.jpa` 或改为 `none`，开启 `mybatis` 对应数据源配置。
- 引入自定义 Mapper（示例：`OrderMapper`），在 Service 中替换 JPA Repository 调用。
- 如需 XML 映射，可在 `resources/mybatis` 添加并在配置中指明 `mapper-locations`。

---

### 3. Kafka（10 分）

#### 使用场景（真实业务）

* 下单成功 → 发送 Kafka 消息
* 消费者：

  * 订单日志
  * 异步通知
  * 数据统计

```text
Order Service → Kafka Producer → order-topic
Kafka Consumer → Log / Async Task
```

---

### 4. Redis 高级使用（10 分）

| 数据结构   | 场景       |
| ------ | -------- |
| String | 登录状态     |
| Hash   | 用户信息缓存   |
| ZSet   | 接口访问排行榜  |
| List   | 异步任务队列   |
| TTL    | Token 过期 |

---

## 六、部署方案（15 分 + 附加分）

### 1. Dockerfile（12 分）

* 后端 Spring Boot 镜像
* 前端 Vue Nginx 镜像

### 2. docker-compose（15 分）

```yaml
services:
  mysql
  redis
  kafka
  backend
  frontend
```

一键启动整个系统

---

### 3. Kubernetes（附加 +5）

* Deployment
* Service
* ConfigMap
* 可选：Ingress

---

## 七、文档要求（15 分）

### 文档结构建议（2000 字内）

1. Spring Boot 核心机制理解
2. Security 与 RBAC 实践
3. Redis 在系统中的角色
4. Kafka 解耦与异步思想
5. Docker / Compose 部署经验总结
6. 项目踩坑与优化点

---

## 八、实现与落地说明

### 1. 关键技术栈落地
- **JDK 版本**：17（已在 `backend/pom.xml` 和 Dockerfile 指定）
- **后端**：Spring Boot 3.2 + Spring Security + JWT + RBAC（数据库持久化）+ Redis 会话/缓存 + Kafka 生产/消费 + Spring Data JPA（附 MyBatis 示例接口）+ AOP 接口统计（Redis String + ZSet）+ OpenAPI
- **数据库**：MySQL 8（DDL 自动建表，可选 Flyway 扩展）
- **前端**：Vue 3 + Vite + Element Plus + Pinia + Vue Router + Axios 拦截器 + ECharts 可视化
- **部署**：Dockerfile（前后端）+ docker-compose（mysql/redis/kafka/zookeeper/backend/frontend 一键启动）
 - **增强**：Flyway 版本化脚本、Redis 令牌桶限流过滤器、接口审计日志（AOP 持久化）

### 2. 运行方式
- 本地启动：
  1) `mysql` 启动并创建库 `oms`，账号 `root/123456`
  2) `redis`、`kafka`、`zookeeper` 本地启动（或使用 compose）
  3) 后端：在 `backend` 目录执行 `mvn spring-boot:run`（默认端口 8080）
  4) 前端：在 `frontend` 目录执行 `npm install && npm run dev`（默认端口 5173，已代理 `/api` 到 8080）
- 一键启动（推荐）：在仓库根目录执行 `docker-compose up -d --build`
 - 一键启动（推荐）：在仓库根目录执行 `docker-compose up -d --build`，或使用脚本：
   - Windows：`powershell -ExecutionPolicy Bypass -File scripts/deploy.ps1 -Rebuild`
   - Bash：`bash scripts/deploy.sh --rebuild`

### 3. 默认账户
- 管理员：`admin` / `admin123`
- 新用户：通过 `/api/auth/register` 注册即分配 `ROLE_USER`（含下单权限）

### 4. 接口与安全
- 认证：`/api/auth/login` 返回 JWT，前端 Axios 拦截器自动附加 `Authorization: Bearer <token>`
- 权限：
  - 角色：`ROLE_ADMIN`、`ROLE_USER`
  - 细粒度权限：`ORDER_READ`、`ORDER_CREATE`、`ORDER_WRITE`，持久化到数据库
- AOP 统计：拦截 `@RestController`，将接口名计入 Redis String 及 ZSet（排行榜），前端 ECharts 展示

### 5. 订单与 Kafka 流程
- 下单：`/api/orders`（用户权限），创建订单、写入操作日志、发送 Kafka 消息 `order-topic`
- 状态流转：`/api/orders/{id}/status`（管理员），同时记录日志并发送 Kafka 消息
- 消费者：`OrderEventConsumer` 监听 `order-topic` 记录日志，示范异步解耦

### 6. Redis 使用场景
- 登录态与缓存（可扩展）：
  - String：接口计数器 `api:counter:*`
  - ZSet：接口访问排行榜 `api:rank`
- 其他数据结构（示例场景设计）：
  - Hash：用户资料缓存
  - List：异步任务队列（可扩展下发通知）

### 7. 持久层切换指引（JPA ⇄ MyBatis）
- JPA 默认实现，Repository 与 Service 解耦
- MyBatis 示例：`OrderMapper#countOrders`（`src/main/resources/mybatis` 预留目录），可按需补充 XML/注解 Mapper；Service 层通过接口注入即可替换

### 8. 前端路由与页面
- 登录页：JWT 获取与存储（localStorage）
- 仪表盘：接口计数与排行榜可视化（ECharts）
- 订单管理：创建订单（多层商品行）、我的订单列表、订单详情与操作记录
- 管理员：接口统计页、用户列表页，受角色控制

### 9. 部署与环境变量
- docker-compose 已暴露：MySQL 3306、Redis 6379、Kafka 9092、后端 8080、前端 5173
- 后端关键变量：`SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`、`SPRING_DATA_REDIS_HOST`、`SPRING_KAFKA_BOOTSTRAP_SERVERS`、`JWT_SECRET`
- 限流配置：`ratelimit.enabled`、`ratelimit.limit`、`ratelimit.window-seconds`（默认 100 次 / 60s / 主体+URI 维度）

### 10. 后续可优化项
- 引入 Flyway 管理 DDL、分环境配置（dev/prod）
- 接入接口限流（Bucket4j/Redis）、审计日志落库
- Kafka 消费者增加重试与死信队列

---


