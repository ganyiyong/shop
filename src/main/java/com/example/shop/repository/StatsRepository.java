package com.example.shop.repository;

import com.example.shop.model.DashboardStats;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class StatsRepository {
    private final JdbcTemplate jdbcTemplate;

    public StatsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DashboardStats dashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime nextMonthStart = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime yearStart = LocalDate.of(today.getYear(), 1, 1).atStartOfDay();
        LocalDateTime nextYearStart = LocalDate.of(today.getYear() + 1, 1, 1).atStartOfDay();

        DashboardStats stats = new DashboardStats();
        stats.setGoodsCount(intValue("SELECT COUNT(*) FROM t_shop_goods WHERE deleted='0'"));
        stats.setStockCount(intValue("SELECT COALESCE(SUM(stock),0) FROM t_shop_stock"));
        stats.setStockAmount(doubleValue("SELECT COALESCE(SUM(stockTotalAmount),0) FROM t_shop_stock"));
        stats.setSaleCount(intValue("SELECT COUNT(*) FROM t_shop_sale"));
        stats.setSaleAmount(doubleValue("SELECT COALESCE(SUM(sellingPrice),0) FROM t_shop_sale"));
        stats.setProfit(doubleValue("SELECT COALESCE(SUM(profit),0) FROM t_shop_sale"));
        stats.setExtraCost(doubleValue("SELECT COALESCE(SUM(amount),0) FROM t_shop_extra_cost"));
        stats.setNetProfit(stats.getProfit() - stats.getExtraCost());
        stats.setTotalSaleAmount(stats.getSaleAmount());
        stats.setTotalProfit(stats.getNetProfit());
        stats.setTodayIncome(doubleValue("SELECT COALESCE(SUM(profit),0) FROM t_shop_sale WHERE createdTime >= ? AND createdTime < ?", todayStart, tomorrowStart));
        stats.setTodaySaleAmount(doubleValue("SELECT COALESCE(SUM(sellingPrice),0) FROM t_shop_sale WHERE createdTime >= ? AND createdTime < ?", todayStart, tomorrowStart));
        double todayCostPrice = doubleValue("SELECT COALESCE(SUM(costPrice),0) FROM t_shop_sale WHERE createdTime >= ? AND createdTime < ?", todayStart, tomorrowStart);
        stats.setTodayProfit(stats.getTodaySaleAmount() - todayCostPrice);
        stats.setTodayOrderCount(intValue("SELECT COUNT(*) FROM t_shop_sale WHERE createdTime >= ? AND createdTime < ?", todayStart, tomorrowStart));
        stats.setMonthIncome(doubleValue("SELECT COALESCE(SUM(profit),0) FROM t_shop_sale WHERE createdTime >= ? AND createdTime < ?", monthStart, nextMonthStart));
        double monthExtraCost = doubleValue("SELECT COALESCE(SUM(amount),0) FROM t_shop_extra_cost WHERE createdTime >= ? AND createdTime < ?", monthStart, nextMonthStart);
        stats.setMonthProfit(stats.getMonthIncome() - monthExtraCost);
        stats.setMonthSaleAmount(doubleValue("SELECT COALESCE(SUM(sellingPrice),0) FROM t_shop_sale WHERE createdTime >= ? AND createdTime < ?", monthStart, nextMonthStart));
        stats.setMonthOrderCount(intValue("SELECT COUNT(*) FROM t_shop_sale WHERE createdTime >= ? AND createdTime < ?", monthStart, nextMonthStart));
        stats.setYearSaleAmount(doubleValue("SELECT COALESCE(SUM(sellingPrice),0) FROM t_shop_sale WHERE createdTime >= ? AND createdTime < ?", yearStart, nextYearStart));
        stats.setYearIncome(doubleValue("SELECT COALESCE(SUM(profit),0) FROM t_shop_sale WHERE createdTime >= ? AND createdTime < ?", yearStart, nextYearStart));
        double yearExtraCost = doubleValue("SELECT COALESCE(SUM(amount),0) FROM t_shop_extra_cost WHERE createdTime >= ? AND createdTime < ?", yearStart, nextYearStart);
        stats.setYearProfit(stats.getYearIncome() - yearExtraCost);
        return stats;
    }

    public List<Map<String, Object>> topGoods() {
        return jdbcTemplate.queryForList("SELECT g.name, COUNT(s.id) saleCount, COALESCE(SUM(s.profit),0) profit, COALESCE(SUM(s.sellingPrice),0) amount " +
                "FROM t_shop_sale s " +
                "LEFT JOIN t_shop_goods g ON g.id = s.goodsId " +
                "GROUP BY g.id, g.name " +
                "ORDER BY saleCount DESC, profit DESC " +
                "LIMIT 8 ");
    }

    public List<Map<String, Object>> stockRanking() {
        return jdbcTemplate.queryForList("SELECT g.name, COALESCE(SUM(s.stock),0) stock, COALESCE(SUM(s.stockTotalAmount),0) amount " +
                "FROM t_shop_stock s " +
                "LEFT JOIN t_shop_goods g ON g.id = s.goodsId " +
                "GROUP BY g.id, g.name " +
                "HAVING COALESCE(SUM(s.stock),0) > 0 " +
                "ORDER BY amount DESC ");
    }

    public List<Map<String, Object>> inventoryStats(Integer goodsId, String sortBy, String sortDir) {
        List<Object> args = new ArrayList<>();
        String where = " WHERE g.deleted='0' AND COALESCE(s.stock, 0) > 0";
        if (goodsId != null) {
            where += " AND g.id = ?";
            args.add(goodsId);
        }
        String orderColumn = "amount".equals(sortBy) ? "stockAmount" : "stock";
        String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        return jdbcTemplate.queryForList("SELECT s.id stockId, " +
                "       g.id goodsId, " +
                "       g.name goodsName, " +
                "       g.type goodsType, " +
                "       g.model goodsModel, " +
                "       g.producer producer, " +
                "       '进货' goodsSource, " +
                "       COALESCE(s.unitPrice, 0) unitPrice, " +
                "       COALESCE(s.stock, 0) stock, " +
                "       COALESCE(s.stockTotalAmount, 0) stockAmount, " +
                "       s.createdTime createdTime " +
                "FROM t_shop_stock s " +
                "LEFT JOIN t_shop_goods g ON g.id = s.goodsId " + where + " " + "ORDER BY  " + " " + orderColumn + " " + direction + ", g.sortKey DESC, g.name, s.id DESC");
    }

    public List<Map<String, Object>> monthlySales() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        jdbcTemplate.query("SELECT createdTime, sellingPrice, profit " +
                "FROM t_shop_sale " +
                "WHERE createdTime IS NOT NULL " +
                "ORDER BY createdTime DESC ", rs -> {
            Timestamp createdTime = rs.getTimestamp("createdTime");
            String month = createdTime.toLocalDateTime().format(formatter);
            Map<String, Object> row = grouped.computeIfAbsent(month, key -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("MONTH", key);
                item.put("SALECOUNT", 0);
                item.put("AMOUNT", 0D);
                item.put("PROFIT", 0D);
                return item;
            });
            row.put("SALECOUNT", ((Number) row.get("SALECOUNT")).intValue() + 1);
            row.put("AMOUNT", ((Number) row.get("AMOUNT")).doubleValue() + rs.getDouble("sellingPrice"));
            row.put("PROFIT", ((Number) row.get("PROFIT")).doubleValue() + rs.getDouble("profit"));
        });
        return firstItems(grouped.values(), 12);
    }

    private List<Map<String, Object>> firstItems(Collection<Map<String, Object>> source, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : source) {
            if (result.size() >= limit) {
                break;
            }
            result.add(item);
        }
        return result;
    }

    private int intValue(String sql) {
        Number value = jdbcTemplate.queryForObject(sql, Number.class);
        return value == null ? 0 : value.intValue();
    }

    private int intValue(String sql, Object... args) {
        Number value = jdbcTemplate.queryForObject(sql, Number.class, args);
        return value == null ? 0 : value.intValue();
    }

    private double doubleValue(String sql) {
        Number value = jdbcTemplate.queryForObject(sql, Number.class);
        return value == null ? 0 : value.doubleValue();
    }

    private double doubleValue(String sql, Object... args) {
        Number value = jdbcTemplate.queryForObject(sql, Number.class, args);
        return value == null ? 0 : value.doubleValue();
    }
}
