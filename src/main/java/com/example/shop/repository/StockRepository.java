package com.example.shop.repository;

import com.example.shop.model.PageResult;
import com.example.shop.model.Stock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StockRepository {
    private final JdbcTemplate jdbcTemplate;

    public StockRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Stock> findAll() {
        return findPage(1, Integer.MAX_VALUE).getRecords();
    }

    public PageResult<Stock> findPage(int page, int size) {
        return findPage(null, true, page, size);
    }

    public PageResult<Stock> findPage(Integer goodsId, boolean validOnly, int page, int size) {
        int currentPage = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        List<Object> args = new ArrayList<>();
        String where = " WHERE 1=1";
        if (goodsId != null) {
            where += " AND s.goodsId = ?";
            args.add(goodsId);
        }
        if (validOnly) {
            where += " AND s.stock > 0";
        }
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM t_shop_stock s" + where, Integer.class, args.toArray());
        List<Object> queryArgs = new ArrayList<>(args);
        queryArgs.add(pageSize);
        queryArgs.add((currentPage - 1) * pageSize);
        List<Stock> records = jdbcTemplate.query("SELECT s.*, " + goodsNameSql() + " goodsName " +
                "FROM t_shop_stock s " +
                "LEFT JOIN t_shop_goods g ON g.id = s.goodsId " + where + " " + "ORDER BY s.createdTime DESC, s.id DESC " +
                "LIMIT ? OFFSET ? ", RowMappers.STOCK, queryArgs.toArray());
        return new PageResult<>(records, currentPage, pageSize, total == null ? 0 : total);
    }

    public List<Stock> findAvailableByGoodsId(int goodsId) {
        return jdbcTemplate.query("SELECT s.*, " + goodsNameSql() + " goodsName " +
                "FROM t_shop_stock s " +
                "LEFT JOIN t_shop_goods g ON g.id = s.goodsId " +
                "WHERE s.goodsId=? AND s.stock > 0 " +
                "ORDER BY s.createdTime ASC, s.id ASC ", RowMappers.STOCK, goodsId);
    }

    public List<Stock> findAvailableForSale() {
        return jdbcTemplate.query("SELECT s.*, " + goodsNameSql() + " goodsName " +
                "FROM t_shop_stock s " +
                "INNER JOIN t_shop_goods g ON g.id = s.goodsId " +
                "WHERE s.stock > 0 AND g.deleted='0' AND g.state='0' " +
                "ORDER BY g.sortKey DESC, g.name, s.createdTime DESC, s.id DESC ", RowMappers.STOCK);
    }

    public Stock findById(int id) {
        List<Stock> stocks = jdbcTemplate.query("SELECT s.*, " + goodsNameSql() + " goodsName " +
                "FROM t_shop_stock s " +
                "LEFT JOIN t_shop_goods g ON g.id = s.goodsId " +
                "WHERE s.id=? ", RowMappers.STOCK, id);
        return stocks.isEmpty() ? null : stocks.get(0);
    }

    public void save(Stock stock) {
        int number = stock.getNumber() == null ? 0 : stock.getNumber();
        double unitPrice = stock.getUnitPrice() == null ? 0 : stock.getUnitPrice();
        double totalAmount = number * unitPrice;
        int currentStock = stock.getStock() == null ? number : stock.getStock();
        double stockTotalAmount = currentStock * unitPrice;
        if (stock.getId() == null) {
            jdbcTemplate.update("INSERT INTO t_shop_stock(goodsId,ctns,number,unitPrice,cost,sellingPrice,stock,totalAmount,stockTotalAmount,createdTime,state) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?) ", stock.getGoodsId(), n(stock.getCtns()), number, unitPrice, n(stock.getCost()), n(stock.getSellingPrice()),
                    currentStock, totalAmount, stockTotalAmount, LocalDateTime.now(), "0");
        } else {
            jdbcTemplate.update("UPDATE t_shop_stock " +
                "SET goodsId=?, ctns=?, number=?, unitPrice=?, cost=?, sellingPrice=?, stock=?, totalAmount=?, stockTotalAmount=? " +
                "WHERE id=? ", stock.getGoodsId(), n(stock.getCtns()), number, unitPrice, n(stock.getCost()), n(stock.getSellingPrice()),
                    currentStock, totalAmount, stockTotalAmount, stock.getId());
        }
    }

    public void decreaseStock(int stockId, int quantity, double unitPrice) {
        jdbcTemplate.update("UPDATE t_shop_stock " +
                "SET stock = stock - ?, " +
                "    stockTotalAmount = (stock - ?) * ? " +
                "WHERE id=? AND stock >= ? ", quantity, quantity, unitPrice, stockId, quantity);
    }

    public int countStock(int goodsId) {
        Integer value = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(stock),0) FROM t_shop_stock WHERE goodsId=?", Integer.class, goodsId);
        return value == null ? 0 : value;
    }

    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM t_shop_stock WHERE id=?", id);
    }

    private static double n(Double value) {
        return value == null ? 0D : value;
    }

    private static String goodsNameSql() {
        return "CONCAT_WS(':', NULLIF(g.name, ''), NULLIF(g.type, ''), NULLIF(g.model, ''))";
    }
}
