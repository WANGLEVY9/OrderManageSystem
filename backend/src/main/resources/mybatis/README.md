# MyBatis 映射文件说明

本目录用于存放 MyBatis 的 XML 映射文件（如 OrderMapper.xml），用于自定义 SQL 查询、结果映射等。每个 Mapper 接口应有对应的 XML 文件。

- OrderMapper.xml：订单相关自定义 SQL 查询（如订单总数统计等）。

配置方式：
- application.yml 中无需特殊配置，Spring Boot Starter 会自动扫描 resources/mybatis/ 下的 XML 文件。
- 若需自定义路径，可添加：
  ```yaml
  mybatis:
    mapper-locations: classpath:mybatis/*.xml
  ```

如需扩展更多 Mapper，按需添加对应 XML 文件即可。
