# 电商库存系统 - 开发文档

## 1. 开发环境

建议环境：

| 项目 | 版本 |
| --- | --- |
| JDK | 1.8 |
| Maven | 3.6+ |
| MySQL | 5.7+/8.x |
| IDE | IntelliJ IDEA 或其他 Java IDE |

项目基于 Spring Boot 2.7.18，依赖在 `pom.xml` 中维护。

## 2. 本地配置

配置文件：

```text
src/main/resources/application.properties
```

当前主要配置：

```properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/shop?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=123456

server.port=80
server.servlet.context-path=/
server.servlet.session.timeout=24h
```

如果本机 MySQL 账号密码不同，需要修改 `spring.datasource.username` 和 `spring.datasource.password`。

如果 80 端口被占用，建议改为：

```properties
server.port=8080
```

## 3. 启动项目

开发模式：

```bash
mvn spring-boot:run
```

打包：

```bash
mvn package
```

运行 Jar：

```bash
java -jar target/shop.jar
```

当前配置下访问：

```text
http://localhost/login
```

若端口改为 8080：

```text
http://localhost:8080/login
```

登录账号：

```text
admin / admin@2026.
```

## 4. 数据库初始化

系统启动时 `DatabaseInitializer` 会自动创建核心表：

- `t_shop_extra_cost`
- `t_shop_goods`
- `t_shop_sale`
- `t_shop_stock`
- `t_shop_asset_snapshot`
- `t_shop_asset_config`

同时会初始化资产估算系数：

```text
assetEstimateRate = 0.4
```

也可以使用脚本初始化：

```text
src/main/resources/schema-mysql.sql
```

启动初始化和 SQL 脚本都使用保守方式建表，不会删除已有数据。

## 5. 代码结构与职责

### 5.1 Controller

路径：

```text
src/main/java/com/example/shop/controller
```

职责：

- 接收页面请求和表单提交。
- 解析筛选参数、分页参数、月份/日期参数。
- 调用 Repository 或 Service 获取数据。
- 向 Thymeleaf 模板填充 `Model`。
- 返回模板名或重定向。

现有 Controller：

| 类 | 职责 |
| --- | --- |
| `AuthController` | 登录、登出 |
| `DashboardController` | 首页看板和首页销售录入 |
| `GoodsController` | 商品列表、保存、删除 |
| `StockController` | 库存列表、保存、删除 |
| `SaleController` | 销售流水、统计、保存、删除 |
| `ExtraCostController` | 额外成本列表、保存、删除 |
| `InventoryStatsController` | 库存统计 |
| `AssetController` | 资产管理、快照保存、估算系数维护 |

### 5.2 Service

路径：

```text
src/main/java/com/example/shop/service
```

当前主要服务：

| 类 | 职责 |
| --- | --- |
| `SaleService` | 销售创建事务、平台解析、库存匹配、利润计算、库存扣减 |

涉及多个表写入或需要事务一致性的业务，应优先放在 Service 层。

### 5.3 Repository

路径：

```text
src/main/java/com/example/shop/repository
```

职责：

- 使用 `JdbcTemplate` 执行 SQL。
- 将查询结果映射为 Model 或 `Map`。
- 封装分页查询。

新增查询时建议在 Repository 中封装，Controller 不直接写 SQL。

### 5.4 Model

路径：

```text
src/main/java/com/example/shop/model
```

Model 既承担数据库记录映射，也承担页面展示模型。当前没有使用 JPA 注解。

### 5.5 Web 辅助组件

路径：

```text
src/main/java/com/example/shop/web
```

| 类 | 职责 |
| --- | --- |
| `AuthInterceptor` | 登录拦截 |
| `MoneyFormatter` | Thymeleaf 金额格式化 Bean |

## 6. 前端模板开发

模板目录：

```text
src/main/resources/templates
```

静态资源目录：

```text
src/main/resources/static/assets
```

布局模板为：

```text
layout.html
```

业务页面通过：

```html
th:replace="layout :: layout(...)"
```

复用统一布局。

新增页面时建议：

1. 新增 Controller 方法并设置 `active` 菜单标识。
2. 在 `layout.html` 添加导航入口。
3. 在 `templates` 下新增页面模板。
4. 复用 `panel`、`toolbar`、`grid-form`、`table-wrap` 等现有样式类。
5. 表单提交后使用 `redirect:` 避免刷新重复提交。

## 7. 常见开发任务

### 7.1 新增业务字段

推荐步骤：

1. 修改数据库初始化逻辑或 SQL 脚本。
2. 修改对应 Model 字段和 getter/setter。
3. 修改 `RowMappers` 映射。
4. 修改 Repository 的 insert/update/select SQL。
5. 修改模板表单和列表展示。
6. 如字段参与统计，更新 `StatsRepository` 或对应 Repository。

注意：已有数据库升级需要使用 `ALTER TABLE`，可以参考 `DatabaseInitializer.addColumnIfMissing`。

### 7.2 新增列表筛选条件

推荐位置：

- Controller 接收 `@RequestParam`
- Repository 动态拼接 `WHERE`
- 模板中添加筛选表单字段
- 分页链接保留筛选参数

当前 Repository 动态查询使用参数数组，新增条件时应继续使用占位符，避免字符串拼接用户输入。

### 7.3 新增统计指标

推荐步骤：

1. 明确指标口径。
2. 在 `DashboardStats` 或页面模型中增加字段。
3. 在 `StatsRepository` 中实现 SQL。
4. 在 Controller 填充模型。
5. 在模板展示。

统计涉及金额时，应明确是否扣除额外成本、手续费、退款和删除记录。

### 7.4 修改销售逻辑

销售核心入口为：

```text
SaleService.create(Sale sale)
```

该方法带 `@Transactional`，当前事务中完成：

- 平台解析
- 库存批次解析
- 成本价、手续费、利润计算
- 保存销售流水
- 扣减库存

涉及销售与库存一致性的改动应集中修改该方法及其私有辅助方法。

## 8. 编码规范建议

- Controller 只做参数解析和页面模型组织，复杂业务放到 Service。
- Repository 只做数据访问，不放页面跳转逻辑。
- SQL 查询使用 `?` 参数占位，不拼接用户输入。
- 新增金额计算建议使用 `BigDecimal`，避免 `Double` 精度问题。
- 删除类操作需要明确是软删除还是物理删除。
- 表单提交成功后使用重定向。
- 时间范围查询统一使用左闭右开：`createdTime >= start AND createdTime < end`。

## 9. 测试建议

当前项目未包含自动化测试。建议逐步补充：

| 测试类型 | 建议覆盖 |
| --- | --- |
| Service 单元测试 | 销售保存、手续费计算、利润计算、库存扣减 |
| Repository 集成测试 | 分页查询、统计 SQL、资产公式依赖数据 |
| Controller 测试 | 登录拦截、表单提交和重定向 |
| 页面回归测试 | 商品、库存、销售、资产核心操作路径 |

销售相关建议优先测试，因为它同时写销售表和库存表。

## 10. 部署建议

简单部署：

```bash
mvn package
java -jar target/shop.jar
```

生产环境建议：

- 使用独立 MySQL 账号，不使用 root。
- 将数据库密码改为环境变量或外部配置。
- 将端口改为非特权端口，例如 8080，再通过 Nginx 代理到 80。
- 定期备份 MySQL 数据库。
- 对登录密码进行配置化和加密存储。
- 如部署到公网，建议接入 Spring Security 或反向代理认证。

## 11. 已知技术债

- 认证账号硬编码在 `AuthController`。
- 金额计算大量使用 `Double`。
- 数据库没有外键约束。
- 销售删除不回滚库存，需要业务上谨慎操作。
- 部分页面和 Java 字符串在当前工作区显示有编码异常，建议统一文件编码为 UTF-8 后检查。
- `README.md` 中账号、端口和当前源码配置不一致，建议后续同步更新。
