# model 文件夹说明

本文件夹包含所有实体模型（Entity），对应数据库表结构，用于 JPA/Hibernate ORM 映射和业务对象传递。

---

## 1. User.java
- **作用**：用户实体，对应 users 表。
- **关键实现**：
  - 包含用户基本信息：id、username、password、phone、email 等。
  - 关联角色：`@ManyToMany` 多对多关系，一个用户可有多个角色。
  - 实现 UserDetails 接口（Spring Security），用于认证和权限管理。

## 2. Role.java
- **作用**：角色实体，对应 roles 表。
- **关键实现**：
  - 包含角色信息：id、roleName（如 ADMIN、USER）。
  - 关联权限：`@ManyToMany` 多对多关系，一个角色可有多个权限。
  - 关联用户：反向关联用户。

## 3. Permission.java
- **作用**：权限实体，对应 permissions 表。
- **关键实现**：
  - 包含权限信息：id、permissionName（如 ORDER_CREATE、ORDER_READ）。
  - 关联角色：`@ManyToMany` 多对多关系。

## 4. OrderEntity.java
- **作用**：订单实体，对应 orders 表。
- **关键实现**：
  - 包含订单信息：id、orderNumber、status、totalAmount、createTime 等。
  - 关联用户：`@ManyToOne` 多对一，订单属于某个用户。
  - 关联订单明细：`@OneToMany` 一对多，一个订单包含多个明细项。
  - 关联操作日志：`@OneToMany` 一对多。

## 5. OrderItem.java
- **作用**：订单明细实体，对应 order_items 表。
- **关键实现**：
  - 包含明细信息：id、productId、productName、quantity、price、subtotal。
  - 关联订单：`@ManyToOne` 多对一，属于某个订单。

## 6. OrderOperationLog.java
- **作用**：订单操作日志实体，对应 order_operation_logs 表。
- **关键实现**：
  - 包含日志信息：id、orderId、operator、operation、operationTime。
  - 用于审计追踪订单的状态变更、删除、修改等操作。

## 7. Product.java
- **作用**：商品实体，对应 products 表。
- **关键实现**：
  - 包含商品信息：id、name、price、stock、imageUrl、description。
  - 支持库存管理和商品展示。

---

所有实体类均使用 JPA 注解（如 `@Entity`、`@Table`、`@Id`、`@Column`），结合 Lombok（`@Data`、`@NoArgsConstructor`）简化代码。