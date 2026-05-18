# 电商库存系统

基于 Spring Boot 2.7 + Thymeleaf HTML + JdbcTemplate 的电商库存系统。

## 启动

```bash
mvn spring-boot:run
```

或先打包再启动：

```bash
mvn package
java -jar target/shop.jar
```

访问地址：

```text
http://localhost:8080/shop/login
```

登录账号：

```text
admin / 123456
```

## 数据库

默认连接 MySQL 的 `shop` 数据库：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/shop?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=123456
```

如果账号密码不同，修改：

```text
src/main/resources/application.properties
```

系统启动时会执行 `CREATE TABLE IF NOT EXISTS`，不会删除已有数据。

## 页面

前端页面已改为 HTML：

```text
src/main/resources/templates
```

静态资源：

```text
src/main/resources/static/assets
```

## 功能

- 库存统计
- 商品列表
- 库存管理
- 额外成本
- 销售管理
