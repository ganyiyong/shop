# 电商库存系统 - 系统文档

## 1. 系统概述

本项目是一个面向小型电商经营场景的库存、销售、成本与资产管理系统。系统以商品档案和库存批次为基础，记录销售流水，自动扣减库存并计算利润，同时提供首页经营看板、库存统计、额外成本和月度资产快照。

系统采用单体 Web 应用架构，后端由 Spring Boot 提供 MVC、会话鉴权和数据库访问能力，前端使用 Thymeleaf 模板渲染页面，数据持久化在 MySQL 中。

## 2. 技术栈

| 类型 | 技术 |
| --- | --- |
| 后端框架 | Spring Boot 2.7.18 |
| Web | Spring MVC |
| 模板引擎 | Thymeleaf |
| 数据访问 | Spring JDBC / JdbcTemplate |
| 数据库 | MySQL 8.x 驱动 |
| 构建工具 | Maven |
| Java 版本 | Java 8 |
| 打包产物 | `target/shop.jar` |

## 3. 系统架构

```text
浏览器
  |
  | HTTP 表单 / 页面访问
  v
Spring MVC Controller
  |
  | 组织页面模型、接收表单
  v
Service
  |
  | 核心业务事务，例如销售扣库存
  v
Repository
  |
  | JdbcTemplate 执行 SQL
  v
MySQL
```

项目目前没有独立 REST API 层，主要通过 Thymeleaf 页面和表单提交完成业务操作。

## 4. 目录结构

```text
src/main/java/com/example/shop
  ShopInventoryApplication.java        应用启动入口
  config/
    WebConfig.java                     MVC 拦截器与静态资源配置
    DatabaseInitializer.java           启动时建表和基础配置初始化
  controller/                          页面控制器
  service/                             业务服务
  repository/                          数据访问层
  model/                               页面和数据库模型
  web/                                 Web 辅助组件

src/main/resources
  application.properties               端口、数据库、Thymeleaf 配置
  schema-mysql.sql                     MySQL 建表脚本
  templates/                           Thymeleaf 页面模板
  static/assets/                       CSS 与 JavaScript 静态资源
```

## 5. 模块划分

| 模块 | 路由 | 主要类 | 说明 |
| --- | --- | --- | --- |
| 登录认证 | `/login`, `/logout` | `AuthController`, `AuthInterceptor` | 固定账号登录，基于 Session 控制访问 |
| 首页看板 | `/`, `/dashboard`, `/stats` | `DashboardController`, `StatsRepository` | 今日、本月、年度、库存、销售趋势等经营概览 |
| 商品管理 | `/goods` | `GoodsController`, `GoodsRepository` | 商品档案增删改查、搜索、启停用和软删除 |
| 库存管理 | `/stock` | `StockController`, `StockRepository` | 库存批次录入、编辑、删除和筛选 |
| 库存统计 | `/inventory-stats` | `InventoryStatsController`, `StatsRepository` | 按商品和库存批次统计库存数量、金额 |
| 销售管理 | `/sales` | `SaleController`, `SaleService`, `SaleRepository` | 销售录入、销售流水、商品维度统计 |
| 额外成本 | `/costs` | `ExtraCostController`, `ExtraCostRepository` | 按月记录额外支出 |
| 资产管理 | `/assets` | `AssetController`, `AssetRepository` | 月度资产快照、贷款、货款、预估系数 |

## 6. 数据库设计

### 6.1 核心表

| 表名 | 说明 |
| --- | --- |
| `t_shop_goods` | 商品档案 |
| `t_shop_stock` | 库存批次 |
| `t_shop_sale` | 销售流水 |
| `t_shop_extra_cost` | 额外成本 |
| `t_shop_asset_snapshot` | 月度资产快照 |
| `t_shop_asset_config` | 资产模块配置 |

### 6.2 主要关系

```text
t_shop_goods.id
  ├─ t_shop_stock.goodsId
  └─ t_shop_sale.goodsId

t_shop_asset_snapshot.month 以 yyyy-MM 唯一标识一个月度快照
t_shop_asset_config.configKey 存储资产估算系数等配置
```

当前数据库层没有声明外键约束，关联关系由业务代码和 SQL 查询维护。

## 7. 认证与权限

系统使用固定账号密码登录：

```text
username: admin
password: admin@2026.
```

登录成功后，Session 写入 `loginUser`。`AuthInterceptor` 拦截所有请求，放行以下路径：

- `/login`
- `/logout`
- `/error`
- `/assets/**`

其他页面必须登录后访问。静态资源通过 `/assets/**` 访问。

## 8. 页面结构

所有业务页面通过 `layout.html` 共享基础布局，包括侧边栏、顶部用户信息、移动端菜单和静态资源加载。

主要页面如下：

| 页面 | 模板 |
| --- | --- |
| 登录页 | `login.html` |
| 首页看板 | `dashboard.html` |
| 商品列表 | `goods.html` |
| 库存管理 | `stock.html` |
| 库存统计 | `inventory-stats.html` |
| 销售管理 | `sales.html` |
| 额外成本 | `costs.html` |
| 资产管理 | `assets.html` |

## 9. 配置说明

当前 `application.properties` 主要配置：

```properties
server.port=80
server.servlet.context-path=/
server.servlet.session.timeout=24h

spring.datasource.url=jdbc:mysql://localhost:3306/shop?...
spring.datasource.username=root
spring.datasource.password=123456
spring.thymeleaf.cache=false
```

注意：README 中的访问地址可能与当前配置不一致。源码当前端口为 `80`，上下文路径为 `/`，登录地址为：

```text
http://localhost/login
```

如果将端口改为 `8080`，则访问：

```text
http://localhost:8080/login
```

## 10. 初始化机制

系统启动时 `DatabaseInitializer` 会执行 `CREATE TABLE IF NOT EXISTS`，用于保证核心表存在，并通过 `INSERT IGNORE` 初始化资产估算系数 `assetEstimateRate=0.4`。

`schema-mysql.sql` 也提供了独立建表脚本。两者都不会删除已有数据。

## 11. 运行方式

开发运行：

```bash
mvn spring-boot:run
```

打包运行：

```bash
mvn package
java -jar target/shop.jar
```

## 12. 已知注意事项

- 项目没有 Spring Security，认证逻辑较轻量，适合内网或个人使用场景。
- 销售删除不会回滚库存，页面也提示了这一点。
- 库存删除为物理删除，商品删除为软删除。
- 部分源码和模板在当前终端显示存在编码异常，但模板原意和业务字段可以从上下文识别。
- 金额字段在 Java 模型中多使用 `Double`，数据库初始化类使用 `decimal`，SQL 脚本部分字段为 `double`，后续可统一为 `decimal`/`BigDecimal` 提升财务精度。
