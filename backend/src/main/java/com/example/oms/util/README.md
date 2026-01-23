# util 文件夹说明

本文件夹包含工具类，提供通用的功能模块，如 JWT Token 生成与解析、加密解密、日期处理等。

---

## JwtUtil.java
- **作用**：JWT Token 工具类，负责 Token 的生成、解析和验证。
- **关键实现**：
  - `generateToken(String username)`：根据用户名生成 JWT Token，包含过期时间。
  - `extractUsername(String token)`：从 Token 中提取用户名（Subject）。
  - `validateToken(String token, UserDetails userDetails)`：验证 Token 是否有效（未过期且用户匹配）。
  - `isTokenExpired(String token)`：检查 Token 是否过期。
  - 使用 `io.jsonwebtoken` 库（jjwt）实现：
    ```java
    Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(SignatureAlgorithm.HS256, secret)
        .compact();
    ```
  - 从配置文件读取密钥（`jwt.secret`）和过期时间（`jwt.expiration-minutes`）。

---

此工具类是 JWT 认证的核心组件，配合 `JwtAuthenticationFilter` 实现无状态认证机制。