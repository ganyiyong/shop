package com.example.shop.repository;

import com.example.shop.model.PageResult;
import com.example.shop.model.Sale;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class SaleRepository {
    private final JdbcTemplate jdbcTemplate;

    public SaleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Sale> findAll() {
        return page(null, null, 1, Integer.MAX_VALUE).getRecords();
    }

    public PageResult<Sale> page(LocalDateTime startTime, LocalDateTime endTime, int page, int size) {
        return page(startTime, endTime, null, page, size);
    }

    public PageResult<Sale> page(LocalDateTime startTime, LocalDateTime endTime, Integer goodsId, int page, int size) {
        List<Object> args = new ArrayList<>();
        String where = " WHERE 1=1";
        if (goodsId != null) {
            where += " AND s.goodsId = ?";
            args.add(goodsId);
        }
        if (startTime != null) {
            where += " AND s.createdTime >= ?";
            args.add(startTime);
        }
        if (endTime != null) {
            where += " AND s.createdTime < ?";
            args.add(endTime);
        }
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM t_shop_sale s" + where, Integer.class, args.toArray());
        List<Object> queryArgs = new ArrayList<>(args);
        int currentPage = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        queryArgs.add(pageSize);
        queryArgs.add((currentPage - 1) * pageSize);
        List<Sale> records = jdbcTemplate.query("SELECT s.*, g.name goodsName " +
                "FROM t_shop_sale s " +
                "LEFT JOIN t_shop_goods g ON g.id = s.goodsId " + where + " " + "ORDER BY s.createdTime DESC, s.id DESC " +
                "LIMIT ? OFFSET ? ", RowMappers.SALE, queryArgs.toArray());
        return new PageResult<>(records, currentPage, pageSize, total == null ? 0 : total);
    }

    public List<Map<String, Object>> goodsStats(LocalDateTime startTime, LocalDateTime endTime, Integer goodsId) {
        List<Object> args = new ArrayList<>();
        String where = " WHERE 1=1";
        if (goodsId != null) {
            where += " AND s.goodsId = ?";
            args.add(goodsId);
        }
        if (startTime != null) {
            where += " AND s.createdTime >= ?";
            args.add(startTime);
        }
        if (endTime != null) {
            where += " AND s.createdTime < ?";
            args.add(endTime);
        }
        return jdbcTemplate.queryForList("SELECT CONCAT(COALESCE(g.name, '未知商品'), IF(g.model IS NULL OR g.model = '', '', CONCAT('(', g.model, ')'))) goodsName, " +
                "       COALESCE(NULLIF(s.goodsSource, ''), '进货') goodsSource, " +
                "       COUNT(*) saleCount, " +
                "       COALESCE(SUM(s.costPrice), 0) costPrice, " +
                "       COALESCE(SUM(s.sellingPrice), 0) sellingPrice, " +
                "       COALESCE(SUM(s.charge), 0) charge, " +
                "       COALESCE(SUM(s.profit), 0) profit " +
                "FROM t_shop_sale s " +
                "LEFT JOIN t_shop_goods g ON g.id = s.goodsId " + where + " " + "GROUP BY s.goodsId, g.name, g.model, COALESCE(NULLIF(s.goodsSource, ''), '进货') " +
                "ORDER BY saleCount DESC, profit DESC, goodsName ", args.toArray());
    }

    public void save(Sale sale) {
        jdbcTemplate.update("INSERT INTO t_shop_sale(goodsId,platform,shop,costPrice,sellingPrice,charge,profit,goodsSource,createdTime,remark) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?) ", sale.getGoodsId(), sale.getPlatform(), sale.getShop(), n(sale.getCostPrice()), n(sale.getSellingPrice()),
                n(sale.getCharge()), n(sale.getProfit()), sale.getGoodsSource(), LocalDateTime.now(), sale.getRemark());
    }

    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM t_shop_sale WHERE id=?", id);
    }

    private static double n(Double value) {
        return value == null ? 0D : value;
    }
}
