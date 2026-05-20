# 电商库存系统 - 逻辑文档

## 1. 业务对象

### 1.1 商品 Goods

商品是库存和销售的基础档案，主要字段包括：

| 字段 | 含义 |
| --- | --- |
| `name` | 商品名称 |
| `type` | 商品类型 |
| `producer` | 厂家/品牌 |
| `model` | 型号 |
| `imgUrl` | 图片地址 |
| `keywords` | 搜索关键词 |
| `state` | 状态，`0` 启用，`1` 停用 |
| `sortKey` | 排序值，越大越靠前 |
| `deleted` | 删除标记，`0` 正常，`1` 已删除 |

商品唯一约束为 `name + type + model`。

### 1.2 库存 Stock

库存以批次形式管理，一条库存记录代表一次进货批次。

| 字段 | 含义 |
| --- | --- |
| `goodsId` | 关联商品 |
| `ctns` | 箱数 |
| `number` | 盒数/入库数量 |
| `unitPrice` | 进货单价 |
| `cost` | 发出成本 |
| `sellingPrice` | 建议销售价 |
| `stock` | 当前库存 |
| `totalAmount` | 入库总金额 |
| `stockTotalAmount` | 当前库存金额 |

保存库存时自动计算：

```text
totalAmount = number * unitPrice
stock = 表单库存值；如果未填，则等于 number
stockTotalAmount = stock * unitPrice
```

### 1.3 销售 Sale

销售流水记录每一次售出。

| 字段 | 含义 |
| --- | --- |
| `goodsId` | 售出商品 |
| `stockId` | 页面选择的库存批次，非数据库字段 |
| `platform` | 销售平台 |
| `shop` | 店铺 |
| `costPrice` | 成本价，由库存批次带出 |
| `sellingPrice` | 销售价 |
| `charge` | 手续费 |
| `profit` | 利润 |
| `goodsSource` | 商品来源，例如进货、倒卖、搬砖 |
| `remark` | 备注 |

销售保存时由 `SaleService.create` 统一处理库存匹配、成本计算、手续费计算、利润计算和扣库存。

### 1.4 额外成本 ExtraCost

用于记录非商品成本类支出，如运营支出、杂费等。

```text
category  支出类目
amount    支出金额
remark    备注
```

保存时 `createdTime` 使用当前时间。

### 1.5 资产快照 AssetSnapshot

资产快照按月份保存，一个月份最多一条记录。

主要包括：

- 进货金额 `purchaseAmount`
- 现金存款 `cashDeposit`
- 贷款类：招商银行信用卡、广州银行信用卡、花呗、其他贷款
- 资产类：借出去、保证金、公积金
- 店铺待确认货款：模型小屋、米乐模型、星域模型、幻影模玩、完美动漫、维利动漫
- 去年存款 `lastYearDeposit`
- 备注

## 2. 核心业务流程

### 2.1 登录流程

```text
用户访问 /login
  |
提交 username/password
  |
AuthController 校验固定账号
  |
成功：Session 写入 loginUser，跳转 /dashboard
失败：返回 login 页面并显示错误
```

后续页面由 `AuthInterceptor` 判断 Session 中是否存在 `loginUser`。

### 2.2 商品管理流程

```text
进入 /goods
  |
按商品、关键词分页查询
  |
新增或编辑商品
  |
GoodsRepository.save
  |
新增：写入 t_shop_goods，deleted=0，createdTime=当前时间
编辑：更新商品基础字段、状态、排序和备注
```

删除商品时不物理删除，而是：

```sql
UPDATE t_shop_goods SET deleted='1' WHERE id=?
```

### 2.3 库存入库与维护流程

```text
进入 /stock
  |
默认只看 stock > 0 的有效库存
  |
新增/编辑库存批次
  |
StockRepository.save
  |
计算入库总金额和库存金额
  |
写入或更新 t_shop_stock
```

库存列表支持按商品筛选，并可切换有效库存和全部库存。

### 2.4 销售录入流程

销售可以从首页看板或销售管理页录入。

```text
用户选择商品
  |
可选择具体库存批次 stockId
  |
填写店铺、销售价、商品来源、备注
  |
提交 /sales/save 或 /dashboard/sales/save
  |
SaleService.create
  |
自动解析平台
  |
选择库存批次或自动取最早可用库存
  |
计算成本、手续费、利润
  |
插入销售流水
  |
库存扣减 1
```

如果表单传入 `stockId`，系统优先使用该库存批次。若没有传入 `stockId`，系统按 `createdTime ASC, id ASC` 自动取该商品最早的可用库存批次。

### 2.5 销售计算规则

平台由店铺推导：

| 店铺 | 平台 |
| --- | --- |
| 模型小屋、星域模型、米乐模型 | 拼多多 |
| 幻影模玩、完美动漫 | 淘宝 |
| 维利动漫 | 抖店 |

成本价：

```text
costPrice = 选中库存批次.cost
```

手续费：

```text
charge = round(sellingPrice * 0.006, 2)
```

利润：

```text
profit = sellingPrice - costPrice - charge
```

库存扣减：

```sql
UPDATE t_shop_stock
SET stock = stock - 1,
    stockTotalAmount = (stock - 1) * unitPrice
WHERE id = ? AND stock >= 1
```

### 2.6 销售删除规则

销售删除为物理删除：

```sql
DELETE FROM t_shop_sale WHERE id=?
```

删除销售记录不会回滚库存。因此如果销售误删，需要手动调整库存或重新录入对应数据。

### 2.7 额外成本流程

```text
进入 /costs
  |
选择月份，默认当前月
  |
按 createdTime 查询该月成本
  |
新增成本记录
  |
删除成本记录
```

额外成本参与首页净利润、本月利润和年度利润计算。

### 2.8 库存统计流程

库存统计页 `/inventory-stats` 查询仍有库存的批次。

筛选和排序：

- 可按商品过滤。
- 可按库存数量或库存金额排序。
- 支持升序和降序。

合计口径：

```text
totalStock = 所有查询结果 stock 求和
totalAmount = 所有查询结果 stockAmount 求和
```

### 2.9 资产管理流程

资产管理按月份维护资产快照。

```text
进入 /assets
  |
选择月份，默认当前月
  |
查询 t_shop_asset_snapshot
  |
如果没有该月记录，创建空对象用于展示
  |
进货金额默认取当前库存金额
  |
计算实际存款、预估存款、今年利润、本月存款
  |
用户可保存月度快照或维护预估系数
```

如果资产快照没有保存 `purchaseAmount`，或保存值小于等于 0，页面展示和保存时使用当前库存金额。

## 3. 统计口径

### 3.1 首页看板

首页主要由 `StatsRepository.dashboard()` 统计。

| 指标 | 口径 |
| --- | --- |
| 商品数 | 未删除商品数 |
| 库存商品数 | `SUM(t_shop_stock.stock)` |
| 库存总金额 | `SUM(t_shop_stock.stockTotalAmount)` |
| 销售总数 | 销售流水总条数 |
| 总销售额 | `SUM(t_shop_sale.sellingPrice)` |
| 销售利润 | `SUM(t_shop_sale.profit)` |
| 额外成本 | `SUM(t_shop_extra_cost.amount)` |
| 净利润 | 销售利润 - 额外成本 |
| 今日销售额 | 今日销售流水销售价合计 |
| 今日利润 | 今日销售额 - 今日成本价合计 |
| 本月收入 | 本月销售利润合计 |
| 本月利润 | 本月销售利润 - 本月额外成本 |
| 年度利润 | 年度销售利润 - 年度额外成本 |

注意：今日利润使用 `sellingPrice - costPrice`，没有扣除手续费；而销售流水中的 `profit` 已扣除手续费。

### 3.2 月度销售趋势

月度销售趋势从全部销售流水按 `createdTime` 格式化为 `yyyy-MM` 分组，最多返回最近 12 个月。

统计项：

- 订单数
- 销售额
- 利润

### 3.3 销售商品统计

销售管理页的商品统计按以下维度分组：

```text
goodsId + 商品名称/类型/型号 + goodsSource
```

统计项：

- 销售数量
- 成本价合计
- 销售价合计
- 手续费合计
- 利润合计

### 3.4 库存金额排名

首页库存排名按商品聚合：

```text
stock = SUM(t_shop_stock.stock)
amount = SUM(t_shop_stock.stockTotalAmount)
```

只显示库存数量大于 0 的商品。

### 3.5 资产统计公式

店铺待确认货款：

```text
receivableTotal =
  modelHouseReceivable
+ mileReceivable
+ xingyuReceivable
+ huanyingReceivable
+ wanmeiReceivable
+ weiliReceivable
```

资产项目合计：

```text
assetItemTotal = lentOut + guaranteeDeposit + housingFund
```

贷款合计：

```text
loanTotal = cmbCreditLoan + gzCreditLoan + huabeiLoan + otherLoan
```

实际存款：

```text
actualDeposit =
  cashDeposit
+ receivableTotal
+ purchaseAmount
+ assetItemTotal
- loanTotal
```

预估存款：

```text
estimatedDeposit = actualDeposit + purchaseAmount * estimateRate
```

今年利润：

```text
yearlyProfit = actualDeposit - lastYearDeposit
```

去年存款优先读取上一年 12 月资产快照的实际存款；如果没有记录，则使用当前资产快照中手动录入的 `lastYearDeposit`。

本月存款：

```text
monthDeposit = 当前月 actualDeposit - 上月 actualDeposit
```

## 4. 分页逻辑

分页模型由 `PageResult<T>` 提供：

```text
page       当前页，最小 1
size       每页条数，最小 1
total      总记录数，最小 0
totalPages 总页数，最小 1
offset     (page - 1) * size
```

列表页均通过 `LIMIT ? OFFSET ?` 实现分页。

## 5. 重要业务约束

- 商品删除不影响历史库存和销售记录，但商品列表默认不展示已删除商品。
- 停用商品不会出现在可选商品列表中。
- 销售保存会扣减库存；销售删除不会恢复库存。
- 销售可以选择具体库存批次；未选择时系统自动取最早可用库存。
- 额外成本只影响利润统计，不影响销售流水利润字段。
- 月度资产快照以月份唯一，重复保存同一月份会更新原记录。
