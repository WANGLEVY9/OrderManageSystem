## 一、项目说明（做什么 + 目录与模块）

订单管理系统（Order Management System），覆盖下单 / 支付流转 / 审计 / 统计，按课程要求实现最高等级技术方案。

- 后端：`backend/`
  - 入口与配置：应用启动 [backend/src/main/java/com/example/oms/OmsApplication.java](backend/src/main/java/com/example/oms/OmsApplication.java)，安全/限流/异常/数据初始化等配置位于 `config`（如 [backend/src/main/java/com/example/oms/config/SecurityConfig.java](backend/src/main/java/com/example/oms/config/SecurityConfig.java)、[backend/src/main/java/com/example/oms/config/RateLimitFilter.java](backend/src/main/java/com/example/oms/config/RateLimitFilter.java)）。
  - 业务模块：`controller`（REST API）、`service`（业务）、`repository`（JPA）、`mapper`（MyBatis 示例）、`model`（实体）、`dto`（请求/响应）。
  - 基础设施：`audit`（审计切面与日志实体）、`aspect/ApiMetricsAspect.java`（API 计数 AOP）、`config/KafkaConfig.java`、`config/RedisConfig.java`。
- 前端：`frontend/`
  - Vue3 + Vite，页面在 `src/views`（登录、Dashboard、订单列表、订单详情、统计、用户管理），路由在 `src/router/index.js`，状态在 `src/store/auth.js`，API 调用在 `src/api/*`。
- 部署与脚本：根目录 `docker-compose.yml`（MySQL/Redis/Kafka/ZK/Backend/Frontend 一键启动），`backend/Dockerfile` 与 `frontend/Dockerfile` 多阶段构建，`scripts/deploy.ps1|sh` 编排辅助。

## 二、技术架构（用什么 + 用在哪）

- 后端框架：Spring Boot 3.2（Java 17），REST + Spring MVC；Bean Validation 校验 DTO。
- 安全与鉴权：Spring Security + JWT（[backend/src/main/java/com/example/oms/config/SecurityConfig.java](backend/src/main/java/com/example/oms/config/SecurityConfig.java)），方法级 `@PreAuthorize`，角色/权限持久化并缓存。
- 数据持久化：Spring Data JPA（默认）+ Flyway 迁移脚本；提供 MyBatis Mapper 示例 [backend/src/main/java/com/example/oms/mapper/OrderMapper.java](backend/src/main/java/com/example/oms/mapper/OrderMapper.java)。
- 消息：Kafka 生产/消费（下单、状态变更事件）[backend/src/main/java/com/example/oms/service/OrderService.java](backend/src/main/java/com/example/oms/service/OrderService.java) + [backend/src/main/java/com/example/oms/service/OrderEventConsumer.java](backend/src/main/java/com/example/oms/service/OrderEventConsumer.java)。
- 缓存与限流：Redis 作为会话/计数/排行榜存储，[backend/src/main/java/com/example/oms/service/StatsService.java](backend/src/main/java/com/example/oms/service/StatsService.java) 读写 String/ZSet；[backend/src/main/java/com/example/oms/config/RateLimitFilter.java](backend/src/main/java/com/example/oms/config/RateLimitFilter.java) 令牌桶限流。
- AOP 统计：接口调用计数切面 [backend/src/main/java/com/example/oms/aspect/ApiMetricsAspect.java](backend/src/main/java/com/example/oms/aspect/ApiMetricsAspect.java) → Redis；前端 ECharts 展示。
- 审计日志：订单操作写入 [backend/src/main/java/com/example/oms/model/OrderOperationLog.java](backend/src/main/java/com/example/oms/model/OrderOperationLog.java) 与 [backend/src/main/java/com/example/oms/repository/OrderOperationLogRepository.java](backend/src/main/java/com/example/oms/repository/OrderOperationLogRepository.java)。
- 前端栈：Vue 3 + Vite + Element Plus + Pinia + Vue Router + Axios（拦截器自动加 JWT），ECharts 展示统计。
- 部署：后端多阶段镜像（maven:3.9 + temurin:21），前端 Nginx 静态服务；docker-compose 健康检查与依赖顺序；默认端口 8080/5173。

## 三、按课程评分点逐条对照与示例

### 1. 前端开发与展示（20 分）
- 前后端分离（10 分）：独立 `frontend/` 工程，Axios 调用后端；路由与守卫 [frontend/src/router/index.js](frontend/src/router/index.js)。
- 多层页面逻辑跳转（7 分）：登录 → Dashboard → 订单列表 → 订单详情 → 操作记录（`views/Orders.vue`、`views/OrderDetail.vue`）。
- 图片展示（10 分）：管理员创建商品可填图片 URL，列表与订单明细中以 `el-image` 预览商品图（见 [frontend/src/views/Orders.vue](frontend/src/views/Orders.vue)、[frontend/src/views/OrderDetail.vue](frontend/src/views/OrderDetail.vue)）。
- CSS 样式（10 分）：Element Plus 主题化 + 自定义样式 [frontend/src/assets/global.css](frontend/src/assets/global.css)；表单/表格/状态胶囊统一风格。

### 2. 安全管理（20 分）
- 基于 Redis 的登录（10 分）：登录生成 JWT，状态写 Redis（`StatsService` 计数、RedisConfig 连接）。
- 权限持久化与授权（满分项）：角色/权限模型落库（`permissions`、`roles`、`user_roles`、`role_permissions` 表，见 [backend/src/main/resources/db/migration/V1__init.sql](backend/src/main/resources/db/migration/V1__init.sql)），DataInitializer 预置管理员/权限；方法级 `@PreAuthorize` 结合持久化权限控制接口访问。
- 密码加密：BCrypt（UserService 注册与 DataInitializer 创建默认用户 [backend/src/main/java/com/example/oms/config/DataInitializer.java](backend/src/main/java/com/example/oms/config/DataInitializer.java)）。

### 3. 基本功能（30 分）
- 重要接口统计（10 分）：AOP 计数切面 [backend/src/main/java/com/example/oms/aspect/ApiMetricsAspect.java](backend/src/main/java/com/example/oms/aspect/ApiMetricsAspect.java) 将接口调用写入 Redis（String/ZSet），前端 `/stats` 页面 ECharts 展示排行与趋势。
- 数据持久化（10 分）：JPA Repository（如 [backend/src/main/java/com/example/oms/repository/OrderRepository.java](backend/src/main/java/com/example/oms/repository/OrderRepository.java)），提供 MyBatis 示例 `OrderMapper` 便于切换。
- Kafka 消息（10 分）：下单/状态变更在 [backend/src/main/java/com/example/oms/service/OrderService.java](backend/src/main/java/com/example/oms/service/OrderService.java) 中发送，消费者 [backend/src/main/java/com/example/oms/service/OrderEventConsumer.java](backend/src/main/java/com/example/oms/service/OrderEventConsumer.java) 记录日志。
- Redis 多数据结构（10 分）：String/ZSet 用于计数与排行（`StatsService`），可扩展 Hash/List/TTL；登录态与限流依赖 Redis 连接。

### 4. 部署（15 分 + 附加分）
- Dockerfile（12 分）：后端多阶段镜像 [backend/Dockerfile](backend/Dockerfile)，前端镜像 [frontend/Dockerfile](frontend/Dockerfile)。
- docker-compose（15 分）：一键启动 [docker-compose.yml](docker-compose.yml)，含 mysql/redis/kafka/zookeeper/backend/frontend 健康检查与依赖顺序。
- Kubernetes（附加）：可依据现有镜像与环境变量直接编写 Deployment/Service/Ingress（未附清单，可按需要补充）。

### 5. 文档（15 分）
- 本 README 覆盖架构、功能、运行方式，对照评分要求，满足 2000 字内说明需求。

## 四、运行与运维指令

### 1. 本地与容器运行
- 本地后端：`cd backend && mvn spring-boot:run`（需要 JDK 17+）。
- 本地前端：`cd frontend && npm install && npm run dev`。
- 跳过测试打包：`mvn --% clean package -Dmaven.test.skip=true`（PowerShell 需 `--%`）。
- 一键编排：在仓库根目录 `docker-compose up -d --build`。

### 2. 变更后重新编排/部署
- 仅重建后端：`docker-compose build backend && docker-compose up -d backend`。
- 仅重建前端：`docker-compose build frontend && docker-compose up -d frontend`。
- 全量重建：`docker-compose up -d --build`（会重新构建前后端镜像）。

### 3. 持久层 JPA ⇄ MyBatis 切换提示
- JPA 默认开启：使用 `repository` 层。
- 切到 MyBatis：在配置中启用 MyBatis 数据源/mapper 扫描，使用 [backend/src/main/java/com/example/oms/mapper/OrderMapper.java](backend/src/main/java/com/example/oms/mapper/OrderMapper.java) 及 `resources/mybatis` 里的 SQL；在 Service 中切换注入 Mapper 实现。

### 4. 数据初始化与账号
- 默认管理员：admin / admin123（`DataInitializer` 写入）。
- 通过 `/api/auth/register` 注册创建普通用户（自动授予下单权限）。

### 5. Git 提交与推送
- 查看状态：`git status`。
- 提交：`git add . && git commit -m "feat: ..."`。
- 首次推送主分支：`git push --set-upstream origin main`（后续直接 `git push`）。

### 6. 常用环境变量（compose 已内置，可覆盖）
- 数据源：`SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`。
- Redis：`SPRING_DATA_REDIS_HOST`。
- Kafka：`SPRING_KAFKA_BOOTSTRAP_SERVERS`。
- JWT：`JWT_SECRET`。
- 限流：`ratelimit.enabled`、`ratelimit.limit`、`ratelimit.window-seconds`。

### 7. 端口与访问
- 后端 8080，前端 5173，MySQL 3306，Redis 6379，Kafka 9092（均在 docker-compose 暴露）。

---



