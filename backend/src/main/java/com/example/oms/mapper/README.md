# mapper 文件夹说明

本文件夹包含 MyBatis 映射器接口，负责实现复杂的数据库查询和数据转换，与 XML 映射文件或注解配合使用。

---

## OrderMapper.java
- **作用**：订单相关的自定义查询映射器，用于执行复杂的订单统计、关联查询等 SQL 操作。
- **关键实现**：
  - 使用 `@Mapper` 注解标记为 MyBatis 接口。
  - 定义复杂查询方法，如订单聚合统计、多表关联查询等。
  - 配合 XML 映射文件或 `@Select`、`@Update` 等注解实现 SQL。
  - 示例方法：
    - `List<OrderStatistics> getOrderStats()`：订单统计查询。
    - `List<OrderDetail> findOrdersWithDetails()`：关联查询订单及明细。

---

MyBatis Mapper 通过接口与 SQL 解耦，提供灵活的数据库操作能力，适合处理复杂业务查询。