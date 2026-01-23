# security 文件夹说明

本文件夹包含 Spring Security 安全相关组件，负责 JWT 认证、过滤器链和权限校验。

---

## JwtAuthenticationFilter.java
- **作用**：JWT 认证过滤器，拦截所有请求，验证 Token 并设置认证上下文。
- **关键实现**：
  - 继承 `OncePerRequestFilter`，确保每次请求只过滤一次。
  - 从请求头 `Authorization: Bearer <token>` 提取 Token。
  - 调用 `JwtUtil` 解析 Token，提取用户名。
  - 从数据库加载用户信息，验证 Token 有效性。
  - 验证成功后，将认证信息设置到 `SecurityContextHolder`：
    ```java
    UsernamePasswordAuthenticationToken authentication = 
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
    ```
  - 放行白名单路径（如登录、注册、文档等）。

---

此过滤器是 JWT 认证流程的核心，确保受保护接口只能由持有有效 Token 的用户访问。