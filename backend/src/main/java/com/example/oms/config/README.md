# config 文件夹说明

本文件夹包含系统的核心配置类，负责安全、限流、消息队列、缓存、异常处理、文档分组和初始化数据等，保证系统的健壮性、可维护性和易用性。

---

## 1. DataInitializer.java
- **作用**：系统数据初始化器，项目启动时自动插入基础数据（如默认管理员、角色、权限等）。
- **关键实现**：通常用 `@PostConstruct` 或实现 `ApplicationRunner`，在 Spring Boot 启动后执行。

## 2. GlobalExceptionHandler.java
- **作用**：全局异常处理器，统一捕获接口异常，返回标准化错误响应。
- **关键实现**：用 `@RestControllerAdvice` + `@ExceptionHandler`，如：
  ```java
  @ExceptionHandler(Exception.class)
  public ApiResponse<?> handle(Exception ex) { ... }
  ```

## 3. KafkaConfig.java
- **作用**：Kafka 消息队列配置，支持异步消息处理。
- **关键实现**：用 `@Configuration`，定义 KafkaTemplate、ConsumerFactory 等 Bean。

## 4. OpenApiConfig.java
- **作用**：OpenAPI/Swagger/Knife4j 文档配置，负责分组、全局信息、安全方案等。
- **关键实现**：定义多个 `GroupedOpenApi` Bean，配置 JWT 安全、API 元数据。

## 5. RateLimitFilter.java
- **作用**：接口限流过滤器，防止接口被频繁访问，支持基于 Redis 的分布式限流。
- **关键实现**：继承 `OncePerRequestFilter`，统计请求次数，超限时返回 429 错误。
  ```java
  if (count > limit) { response.setStatus(429); ... }
  ```
  并通过 `shouldNotFilter` 方法豁免文档、健康检查等路径。

## 6. RedisConfig.java
- **作用**：Redis 连接配置，支持缓存、分布式锁、限流等功能。
- **关键实现**：用 `@Configuration`，定义 RedisTemplate Bean，设置序列化方式、连接参数。

## 7. SecurityConfig.java
- **作用**：Spring Security 安全配置，负责接口权限控制、JWT 认证、过滤器链、密码加密等。
- **关键实现**：
  - 配置无需认证的接口（如 `/api/auth/**`、文档相关路径）。
  - 配置 JWT 认证过滤器和限流过滤器。
  - 配置密码加密方式（如 BCrypt）。
  - 配置无状态会话。

---

这些配置类通过 Spring 注解和 Bean 管理机制实现自动化和模块化，是系统基础设施的核心组成部分。