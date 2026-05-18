package com.example.shop.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DatabaseInitializer {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS t_shop_extra_cost ( " +
                "  id int AUTO_INCREMENT PRIMARY KEY, " +
                "  category varchar(15), " +
                "  amount decimal(8,2), " +
                "  createdTime datetime, " +
                "  remark varchar(50) " +
                ") ");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS t_shop_goods ( " +
                "  id int AUTO_INCREMENT PRIMARY KEY, " +
                "  name varchar(60), " +
                "  type varchar(10), " +
                "  producer varchar(50), " +
                "  model varchar(10), " +
                "  imgUrl varchar(100), " +
                "  keywords varchar(60), " +
                "  createdTime datetime, " +
                "  state char(1) DEFAULT '0', " +
                "  sortKey int DEFAULT 0, " +
                "  remark varchar(100), " +
                "  deleted char(1) DEFAULT '0', " +
                "  UNIQUE(name, type, model) " +
                ") ");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS t_shop_sale ( " +
                "  id int AUTO_INCREMENT PRIMARY KEY, " +
                "  goodsId int, " +
                "  platform varchar(20), " +
                "  shop varchar(50), " +
                "  costPrice decimal(8,2), " +
                "  sellingPrice decimal(8,2), " +
                "  charge decimal(5,2), " +
                "  profit decimal(8,2), " +
                "  goodsSource varchar(10), " +
                "  createdTime datetime, " +
                "  remark varchar(100) " +
                ") ");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS t_shop_stock ( " +
                "  id int AUTO_INCREMENT PRIMARY KEY, " +
                "  goodsId int, " +
                "  ctns decimal(5,2), " +
                "  number int, " +
                "  unitPrice decimal(10,2), " +
                "  cost decimal(10,2), " +
                "  sellingPrice decimal(10,2), " +
                "  stock int, " +
                "  totalAmount decimal(10,2), " +
                "  stockTotalAmount decimal(10,2), " +
                "  createdTime datetime, " +
                "  state char(1) DEFAULT '0' " +
                ") ");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS t_shop_asset_snapshot ( " +
                "  id int AUTO_INCREMENT PRIMARY KEY, " +
                "  month varchar(7) NOT NULL, " +
                "  purchaseAmount decimal(12,2) DEFAULT 0, " +
                "  cashDeposit decimal(12,2) DEFAULT 0, " +
                "  cmbCreditLoan decimal(12,2) DEFAULT 0, " +
                "  gzCreditLoan decimal(12,2) DEFAULT 0, " +
                "  huabeiLoan decimal(12,2) DEFAULT 0, " +
                "  otherLoan decimal(12,2) DEFAULT 0, " +
                "  lentOut decimal(12,2) DEFAULT 0, " +
                "  guaranteeDeposit decimal(12,2) DEFAULT 0, " +
                "  housingFund decimal(12,2) DEFAULT 0, " +
                "  lastYearDeposit decimal(12,2) DEFAULT 0, " +
                "  modelHouseReceivable decimal(12,2) DEFAULT 0, " +
                "  mileReceivable decimal(12,2) DEFAULT 0, " +
                "  xingyuReceivable decimal(12,2) DEFAULT 0, " +
                "  huanyingReceivable decimal(12,2) DEFAULT 0, " +
                "  wanmeiReceivable decimal(12,2) DEFAULT 0, " +
                "  weiliReceivable decimal(12,2) DEFAULT 0, " +
                "  remark varchar(200), " +
                "  createdTime datetime, " +
                "  updatedTime datetime, " +
                "  UNIQUE KEY uk_asset_month(month) " +
                ") ");
        addColumnIfMissing("t_shop_asset_snapshot", "purchaseAmount", "decimal(12,2) DEFAULT 0");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS t_shop_asset_config ( " +
                "  id int AUTO_INCREMENT PRIMARY KEY, " +
                "  configKey varchar(50) NOT NULL, " +
                "  configValue decimal(10,4) DEFAULT 0, " +
                "  remark varchar(100), " +
                "  updatedTime datetime, " +
                "  UNIQUE KEY uk_asset_config_key(configKey) " +
                ") ");
        jdbcTemplate.update("INSERT IGNORE INTO t_shop_asset_config(configKey, configValue, remark, updatedTime) " +
                "VALUES('assetEstimateRate', 0.4, '预估存款系数', NOW()) ");
    }

    private void addColumnIfMissing(String table, String column, String definition) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) " +
                "FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? ", Integer.class, table, column);
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }
}
