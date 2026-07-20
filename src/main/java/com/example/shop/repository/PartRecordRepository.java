package com.example.shop.repository;

import com.example.shop.model.PartBatch;
import com.example.shop.model.PartSale;
import com.example.shop.model.PartSummary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.sql.Types;
import java.util.List;

@Repository
public class PartRecordRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<PartBatch> BATCH_MAPPER = (rs, rowNum) -> {
        PartBatch item = new PartBatch();
        item.setId(rs.getInt("id"));
        int goodsId = rs.getInt("goodsId");
        if (!rs.wasNull()) item.setGoodsId(goodsId);
        item.setName(rs.getString("name"));
        item.setProductCost(rs.getBigDecimal("productCost"));
        item.setRemark(rs.getString("remark"));
        item.setCreatedTime(RowMappers.time(rs.getTimestamp("createdTime")));
        try { item.setPartCount(rs.getInt("partCount")); } catch (Exception ignored) { }
        try { item.setSellingAmount(rs.getBigDecimal("sellingAmount")); } catch (Exception ignored) { }
        try { item.setGrossProfit(rs.getBigDecimal("grossProfit")); } catch (Exception ignored) { }
        return item;
    };

    private static final RowMapper<PartSale> SALE_MAPPER = (rs, rowNum) -> {
        PartSale item = new PartSale();
        item.setId(rs.getInt("id"));
        item.setBatchId(rs.getInt("batchId"));
        item.setPartNumber(rs.getString("partNumber"));
        item.setSellingPrice(rs.getBigDecimal("sellingPrice"));
        item.setCost(rs.getBigDecimal("cost"));
        item.setProfit(rs.getBigDecimal("profit"));
        item.setRemark(rs.getString("remark"));
        item.setCreatedTime(RowMappers.time(rs.getTimestamp("createdTime")));
        return item;
    };

    public PartRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PartBatch> findBatches(Integer goodsId) {
        String sql = batchSelect() + (goodsId == null ? "" : " WHERE b.goodsId=?") +
                " ORDER BY b.createdTime DESC, b.id DESC";
        return goodsId == null
                ? jdbcTemplate.query(sql, BATCH_MAPPER)
                : jdbcTemplate.query(sql, BATCH_MAPPER, goodsId);
    }

    public PartBatch findBatch(int id) {
        List<PartBatch> records = jdbcTemplate.query(batchSelect() + " WHERE b.id=?", BATCH_MAPPER, id);
        return records.isEmpty() ? null : records.get(0);
    }

    public List<PartSale> findSales(int batchId) {
        return jdbcTemplate.query("SELECT * FROM t_shop_part_sale WHERE batchId=? ORDER BY createdTime DESC, id DESC",
                SALE_MAPPER, batchId);
    }

    public PartSummary summary(Integer goodsId) {
        String sql = "SELECT COUNT(b.id) batchCount, COALESCE(SUM(s.partCount), 0) partCount, " +
                "COALESCE(SUM(s.sellingAmount), 0) sellingAmount, COALESCE(SUM(s.grossProfit), 0) grossProfit, " +
                "COALESCE(SUM(b.productCost), 0) productCost " +
                "FROM t_shop_part_batch b LEFT JOIN (" + saleSummarySql() + ") s ON s.batchId=b.id" +
                (goodsId == null ? "" : " WHERE b.goodsId=?");
        RowMapper<PartSummary> mapper = (rs, rowNum) -> {
            PartSummary result = new PartSummary();
            result.setBatchCount(rs.getInt("batchCount"));
            result.setPartCount(rs.getInt("partCount"));
            result.setSellingAmount(rs.getBigDecimal("sellingAmount"));
            result.setGrossProfit(rs.getBigDecimal("grossProfit"));
            result.setProductCost(rs.getBigDecimal("productCost"));
            return result;
        };
        return goodsId == null
                ? jdbcTemplate.queryForObject(sql, mapper)
                : jdbcTemplate.queryForObject(sql, mapper, goodsId);
    }

    public int saveBatch(PartBatch batch) {
        BigDecimal productCost = money(batch.getProductCost());
        if (batch.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO t_shop_part_batch(goodsId,name,productCost,remark,createdTime) VALUES(?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                if (batch.getGoodsId() == null) statement.setNull(1, Types.INTEGER);
                else statement.setInt(1, batch.getGoodsId());
                statement.setString(2, clean(batch.getName()));
                statement.setBigDecimal(3, productCost);
                statement.setString(4, clean(batch.getRemark()));
                statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                return statement;
            }, keyHolder);
            Number key = keyHolder.getKey();
            return key == null ? 0 : key.intValue();
        }
        jdbcTemplate.update("UPDATE t_shop_part_batch SET goodsId=?, name=?, productCost=?, remark=? WHERE id=?",
                batch.getGoodsId(), clean(batch.getName()), productCost, clean(batch.getRemark()), batch.getId());
        return batch.getId();
    }

    public void saveSale(PartSale sale) {
        BigDecimal sellingPrice = money(sale.getSellingPrice());
        BigDecimal cost = money(sale.getCost());
        BigDecimal profit = sellingPrice.subtract(cost).setScale(2, RoundingMode.HALF_UP);
        if (sale.getId() == null) {
            jdbcTemplate.update("INSERT INTO t_shop_part_sale(batchId,partNumber,sellingPrice,cost,profit,remark,createdTime) " +
                            "VALUES(?,?,?,?,?,?,?)", sale.getBatchId(), clean(sale.getPartNumber()), sellingPrice, cost,
                    profit, clean(sale.getRemark()), LocalDateTime.now());
        } else {
            jdbcTemplate.update("UPDATE t_shop_part_sale SET partNumber=?, sellingPrice=?, cost=?, profit=?, remark=? " +
                            "WHERE id=? AND batchId=?", clean(sale.getPartNumber()), sellingPrice, cost, profit,
                    clean(sale.getRemark()), sale.getId(), sale.getBatchId());
        }
    }

    @Transactional
    public void saveSales(List<PartSale> sales) {
        for (PartSale sale : sales) saveSale(sale);
    }

    public void deleteSale(int id, int batchId) {
        jdbcTemplate.update("DELETE FROM t_shop_part_sale WHERE id=? AND batchId=?", id, batchId);
    }

    @Transactional
    public void deleteBatch(int id) {
        jdbcTemplate.update("DELETE FROM t_shop_part_sale WHERE batchId=?", id);
        jdbcTemplate.update("DELETE FROM t_shop_part_batch WHERE id=?", id);
    }

    private static String batchSelect() {
        return "SELECT b.id, b.goodsId, " +
                "COALESCE(NULLIF(CONCAT_WS(':', NULLIF(g.name,''), NULLIF(g.type,''), NULLIF(g.model,'')), ''), b.name) name, " +
                "b.productCost, b.remark, b.createdTime, COALESCE(s.partCount, 0) partCount, " +
                "COALESCE(s.sellingAmount, 0) sellingAmount, COALESCE(s.grossProfit, 0) grossProfit " +
                "FROM t_shop_part_batch b LEFT JOIN t_shop_goods g ON g.id=b.goodsId " +
                "LEFT JOIN (" + saleSummarySql() + ") s ON s.batchId=b.id";
    }

    private static String saleSummarySql() {
        return "SELECT batchId, COUNT(*) partCount, COALESCE(SUM(sellingPrice), 0) sellingAmount, " +
                "COALESCE(SUM(profit), 0) grossProfit FROM t_shop_part_sale GROUP BY batchId";
    }

    private static BigDecimal money(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String clean(String value) {
        return value == null ? null : value.trim();
    }
}
