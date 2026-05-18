package com.example.shop.repository;

import com.example.shop.model.ExtraCost;
import com.example.shop.model.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ExtraCostRepository {
    private final JdbcTemplate jdbcTemplate;

    public ExtraCostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ExtraCost> findAll() {
        return page(null, null, 1, Integer.MAX_VALUE).getRecords();
    }

    public PageResult<ExtraCost> page(LocalDateTime startTime, LocalDateTime endTime, int page, int size) {
        List<Object> args = new ArrayList<>();
        String where = " WHERE 1=1";
        if (startTime != null) {
            where += " AND createdTime >= ?";
            args.add(startTime);
        }
        if (endTime != null) {
            where += " AND createdTime < ?";
            args.add(endTime);
        }
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM t_shop_extra_cost" + where, Integer.class, args.toArray());
        List<Object> queryArgs = new ArrayList<>(args);
        int currentPage = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        queryArgs.add(pageSize);
        queryArgs.add((currentPage - 1) * pageSize);
        List<ExtraCost> records = jdbcTemplate.query("SELECT * FROM t_shop_extra_cost" + where + " ORDER BY createdTime DESC, id DESC LIMIT ? OFFSET ?",
            RowMappers.EXTRA_COST, queryArgs.toArray());
        return new PageResult<>(records, currentPage, pageSize, total == null ? 0 : total);
    }

    public void save(ExtraCost cost) {
        jdbcTemplate.update("INSERT INTO t_shop_extra_cost(category,amount,createdTime,remark) VALUES(?,?,?,?)",
            cost.getCategory(), cost.getAmount() == null ? 0D : cost.getAmount(), LocalDateTime.now(), cost.getRemark());
    }

    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM t_shop_extra_cost WHERE id=?", id);
    }
}
