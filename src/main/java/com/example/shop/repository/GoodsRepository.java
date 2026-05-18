package com.example.shop.repository;

import com.example.shop.model.Goods;
import com.example.shop.model.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GoodsRepository {
    private final JdbcTemplate jdbcTemplate;

    public GoodsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Goods> findAll(String keyword) {
        return findPage(keyword, 1, Integer.MAX_VALUE).getRecords();
    }

    public PageResult<Goods> findPage(String keyword, int page, int size) {
        return findPage(keyword, null, page, size);
    }

    public PageResult<Goods> findPage(String keyword, Integer goodsId, int page, int size) {
        List<Object> args = new ArrayList<>();
        String where = " WHERE g.deleted = '0'";
        if (goodsId != null) {
            where += " AND g.id = ?";
            args.add(goodsId);
        }
        if (StringUtils.hasText(keyword)) {
            where += " AND (g.name LIKE ? OR g.type LIKE ? OR g.producer LIKE ? OR g.model LIKE ? OR g.keywords LIKE ?)";
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM t_shop_goods g" + where, Integer.class, args.toArray());
        List<Object> queryArgs = new ArrayList<>(args);
        int currentPage = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        queryArgs.add(pageSize);
        queryArgs.add((currentPage - 1) * pageSize);
        String sql = "SELECT g.*, " +
                "       COALESCE(st.stock, 0) stock, " +
                "       COALESCE(st.stockAmount, 0) stockAmount, " +
                "       COALESCE(st.lastPrice, 0) lastPrice " +
                "FROM t_shop_goods g " +
                "LEFT JOIN ( " +
                "    SELECT goodsId, SUM(stock) stock, SUM(stockTotalAmount) stockAmount, MAX(sellingPrice) lastPrice " +
                "    FROM t_shop_stock " +
                "    GROUP BY goodsId " +
                ") st ON st.goodsId = g.id " + where + " ORDER BY g.sortKey DESC, g.createdTime DESC, g.id DESC LIMIT ? OFFSET ?";
        return new PageResult<>(jdbcTemplate.query(sql, RowMappers.GOODS, queryArgs.toArray()), currentPage, pageSize, total == null ? 0 : total);
    }

    public List<Goods> findActive() {
        return jdbcTemplate.query("SELECT g.*, " +
                "       COALESCE(st.stock, 0) stock, " +
                "       COALESCE(st.stockAmount, 0) stockAmount, " +
                "       COALESCE(st.lastPrice, 0) lastPrice " +
                "FROM t_shop_goods g " +
                "LEFT JOIN ( " +
                "    SELECT goodsId, SUM(stock) stock, SUM(stockTotalAmount) stockAmount, MAX(sellingPrice) lastPrice " +
                "    FROM t_shop_stock " +
                "    GROUP BY goodsId " +
                ") st ON st.goodsId = g.id " +
                "WHERE g.deleted='0' AND g.state='0' " +
                "ORDER BY g.sortKey DESC, g.name ", RowMappers.GOODS);
    }

    public List<Goods> findInStock() {
        return jdbcTemplate.query("SELECT g.*, " +
                "       COALESCE(st.stock, 0) stock, " +
                "       COALESCE(st.stockAmount, 0) stockAmount, " +
                "       COALESCE(st.lastPrice, 0) lastPrice " +
                "FROM t_shop_goods g " +
                "INNER JOIN ( " +
                "    SELECT goodsId, SUM(stock) stock, SUM(stockTotalAmount) stockAmount, MAX(sellingPrice) lastPrice " +
                "    FROM t_shop_stock " +
                "    GROUP BY goodsId " +
                "    HAVING SUM(stock) > 0 " +
                ") st ON st.goodsId = g.id " +
                "WHERE g.deleted='0' AND g.state='0' " +
                "ORDER BY g.sortKey DESC, g.name ", RowMappers.GOODS);
    }

    public Goods findById(int id) {
        List<Goods> list = jdbcTemplate.query("SELECT * FROM t_shop_goods WHERE id=?", RowMappers.GOODS, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public void save(Goods goods) {
        if (goods.getId() == null) {
            jdbcTemplate.update("INSERT INTO t_shop_goods(name,type,producer,model,imgUrl,keywords,createdTime,state,sortKey,remark,deleted) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?) ", goods.getName(), goods.getType(), goods.getProducer(), goods.getModel(), goods.getImgUrl(),
                    goods.getKeywords(), LocalDateTime.now(), defaultText(goods.getState(), "0"),
                    defaultInt(goods.getSortKey()), goods.getRemark(), "0");
        } else {
            jdbcTemplate.update("UPDATE t_shop_goods " +
                "SET name=?, type=?, producer=?, model=?, imgUrl=?, keywords=?, state=?, sortKey=?, remark=? " +
                "WHERE id=? ", goods.getName(), goods.getType(), goods.getProducer(), goods.getModel(), goods.getImgUrl(),
                    goods.getKeywords(), defaultText(goods.getState(), "0"), defaultInt(goods.getSortKey()),
                    goods.getRemark(), goods.getId());
        }
    }

    public void delete(int id) {
        jdbcTemplate.update("UPDATE t_shop_goods SET deleted='1' WHERE id=?", id);
    }

    private static String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private static int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
