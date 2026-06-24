CREATE TABLE IF NOT EXISTS `t_shop_extra_cost` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `category` varchar(15) DEFAULT NULL COMMENT '支出类目',
  `amount` double(8,2) DEFAULT NULL COMMENT '支出金额',
  `createdTime` datetime DEFAULT NULL COMMENT '支出时间',
  `remark` varchar(50) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='额外成本';

CREATE TABLE IF NOT EXISTS `t_shop_goods` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(60) DEFAULT NULL,
  `type` varchar(10) DEFAULT NULL,
  `producer` varchar(50) DEFAULT NULL,
  `model` varchar(10) DEFAULT NULL,
  `imgUrl` varchar(100) DEFAULT NULL,
  `keywords` varchar(60) DEFAULT NULL,
  `createdTime` datetime DEFAULT NULL,
  `state` char(1) DEFAULT '0',
  `sortKey` int(11) DEFAULT '0',
  `remark` varchar(100) DEFAULT NULL,
  `deleted` char(1) DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name` (`name`,`type`,`model`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='商品表';

CREATE TABLE IF NOT EXISTS `t_shop_sale` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `goodsId` int(11) DEFAULT NULL COMMENT '商品ID',
  `platform` varchar(20) DEFAULT NULL COMMENT '销售平台',
  `shop` varchar(50) DEFAULT NULL COMMENT '店铺',
  `costPrice` double(8,2) DEFAULT NULL COMMENT '成本价',
  `sellingPrice` double(8,2) DEFAULT NULL COMMENT '销售价',
  `charge` double(5,2) DEFAULT NULL COMMENT '手续费',
  `profit` double(8,2) DEFAULT NULL COMMENT '利润',
  `goodsSource` varchar(10) DEFAULT NULL COMMENT '商品来源',
  `createdTime` datetime DEFAULT NULL COMMENT '售出时间',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='销售数据';

CREATE TABLE IF NOT EXISTS `t_shop_stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goodsId` int(11) DEFAULT NULL COMMENT '商品ID',
  `ctns` double(5,2) DEFAULT NULL COMMENT '箱数',
  `number` int(11) DEFAULT NULL COMMENT '盒数',
  `unitPrice` double(10,2) DEFAULT NULL COMMENT '进货单价',
  `cost` double(10,2) DEFAULT NULL COMMENT '发出成本',
  `sellingPrice` double(10,2) DEFAULT NULL COMMENT '销售价格',
  `stock` int(11) DEFAULT NULL COMMENT '库存',
  `totalAmount` double(10,2) DEFAULT NULL COMMENT '总金额',
  `stockTotalAmount` double(10,2) DEFAULT NULL COMMENT '库存总金额',
  `createdTime` datetime DEFAULT NULL COMMENT '添加时间',
  `state` char(1) DEFAULT '0' COMMENT '状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='库存';

CREATE TABLE IF NOT EXISTS `t_shop_asset_snapshot` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `month` varchar(7) NOT NULL COMMENT '统计月份 yyyy-MM',
  `purchaseAmount` double(12,2) DEFAULT '0.00' COMMENT '进货金额快照',
  `cashDeposit` double(12,2) DEFAULT '0.00' COMMENT '现金存款',
  `cmbCreditLoan` double(12,2) DEFAULT '0.00' COMMENT '招商银行信用卡',
  `gzCreditLoan` double(12,2) DEFAULT '0.00' COMMENT '广州银行信用卡',
  `huabeiLoan` double(12,2) DEFAULT '0.00' COMMENT '支付宝花呗',
  `otherLoan` double(12,2) DEFAULT '0.00' COMMENT '其他贷款',
  `lentOut` double(12,2) DEFAULT '0.00' COMMENT '借出去',
  `guaranteeDeposit` double(12,2) DEFAULT '0.00' COMMENT '保证金',
  `housingFund` double(12,2) DEFAULT '0.00' COMMENT '公积金',
  `lastYearDeposit` double(12,2) DEFAULT '0.00' COMMENT '去年存款',
  `modelHouseReceivable` double(12,2) DEFAULT '0.00' COMMENT '模型小屋待确认货款',
  `mileReceivable` double(12,2) DEFAULT '0.00' COMMENT '米乐模型待确认货款',
  `xingyuReceivable` double(12,2) DEFAULT '0.00' COMMENT '星域模型待确认货款',
  `huanyingReceivable` double(12,2) DEFAULT '0.00' COMMENT '幻影模玩待确认货款',
  `wanmeiReceivable` double(12,2) DEFAULT '0.00' COMMENT '完美动漫待确认货款',
  `weiliReceivable` double(12,2) DEFAULT '0.00' COMMENT '维利动漫待确认货款',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  `updatedTime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_asset_month` (`month`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='资产月度快照';

CREATE TABLE IF NOT EXISTS `t_shop_asset_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `configKey` varchar(50) NOT NULL COMMENT '配置键',
  `configValue` double(10,4) DEFAULT '0.0000' COMMENT '配置值',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `updatedTime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_asset_config_key` (`configKey`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='资产配置';

CREATE TABLE IF NOT EXISTS `t_shop_asset_snapshot_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sourceAssetId` int(11) DEFAULT NULL,
  `month` varchar(7) NOT NULL,
  `purchaseAmount` double(12,2) DEFAULT '0.00',
  `cashDeposit` double(12,2) DEFAULT '0.00',
  `cmbCreditLoan` double(12,2) DEFAULT '0.00',
  `gzCreditLoan` double(12,2) DEFAULT '0.00',
  `huabeiLoan` double(12,2) DEFAULT '0.00',
  `otherLoan` double(12,2) DEFAULT '0.00',
  `lentOut` double(12,2) DEFAULT '0.00',
  `guaranteeDeposit` double(12,2) DEFAULT '0.00',
  `housingFund` double(12,2) DEFAULT '0.00',
  `lastYearDeposit` double(12,2) DEFAULT '0.00',
  `modelHouseReceivable` double(12,2) DEFAULT '0.00',
  `mileReceivable` double(12,2) DEFAULT '0.00',
  `xingyuReceivable` double(12,2) DEFAULT '0.00',
  `huanyingReceivable` double(12,2) DEFAULT '0.00',
  `wanmeiReceivable` double(12,2) DEFAULT '0.00',
  `weiliReceivable` double(12,2) DEFAULT '0.00',
  `remark` varchar(200) DEFAULT NULL,
  `snapshotTime` datetime DEFAULT NULL,
  `createdTime` datetime DEFAULT NULL,
  `updatedTime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_asset_history_month` (`month`) USING BTREE,
  KEY `idx_asset_history_source` (`sourceAssetId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='asset snapshot history';

INSERT IGNORE INTO `t_shop_asset_config` (`configKey`, `configValue`, `remark`, `updatedTime`)
VALUES ('assetEstimateRate', 0.4, '预估存款系数', NOW());
