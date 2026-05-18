package com.example.shop.repository;

import com.example.shop.model.AssetSnapshot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Repository
public class AssetRepository {
    private final JdbcTemplate jdbcTemplate;

    public AssetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AssetSnapshot findByMonth(String month) {
        List<AssetSnapshot> list = jdbcTemplate.query("SELECT * FROM t_shop_asset_snapshot WHERE month=?", RowMappers.ASSET, month);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        AssetSnapshot snapshot = new AssetSnapshot();
        snapshot.setMonth(month);
        return snapshot;
    }

    public List<AssetSnapshot> recent() {
        return jdbcTemplate.query("SELECT * " +
                "FROM t_shop_asset_snapshot " +
                "ORDER BY month DESC " +
                "LIMIT 12 ", RowMappers.ASSET);
    }

    public void save(AssetSnapshot asset) {
        AssetSnapshot existing = findByMonth(asset.getMonth());
        if (existing.getId() == null) {
            jdbcTemplate.update("INSERT INTO t_shop_asset_snapshot(month,purchaseAmount,cashDeposit,cmbCreditLoan,gzCreditLoan,huabeiLoan,otherLoan,lentOut,guaranteeDeposit,housingFund,lastYearDeposit,modelHouseReceivable,mileReceivable,xingyuReceivable,huanyingReceivable,wanmeiReceivable,weiliReceivable,remark,createdTime,updatedTime) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ", defaultMonth(asset.getMonth()), n(asset.getPurchaseAmount()), n(asset.getCashDeposit()), n(asset.getCmbCreditLoan()), n(asset.getGzCreditLoan()),
                    n(asset.getHuabeiLoan()), n(asset.getOtherLoan()), n(asset.getLentOut()), n(asset.getGuaranteeDeposit()),
                    n(asset.getHousingFund()), n(asset.getLastYearDeposit()), n(asset.getModelHouseReceivable()), n(asset.getMileReceivable()), n(asset.getXingyuReceivable()),
                    n(asset.getHuanyingReceivable()), n(asset.getWanmeiReceivable()), n(asset.getWeiliReceivable()), asset.getRemark(),
                    LocalDateTime.now(), LocalDateTime.now());
        } else {
            jdbcTemplate.update("UPDATE t_shop_asset_snapshot " +
                "SET purchaseAmount=?, cashDeposit=?, cmbCreditLoan=?, gzCreditLoan=?, huabeiLoan=?, otherLoan=?, lentOut=?, guaranteeDeposit=?, housingFund=?, lastYearDeposit=?, " +
                "    modelHouseReceivable=?, mileReceivable=?, xingyuReceivable=?, huanyingReceivable=?, wanmeiReceivable=?, weiliReceivable=?, remark=?, updatedTime=? " +
                "WHERE month=? ", n(asset.getPurchaseAmount()), n(asset.getCashDeposit()), n(asset.getCmbCreditLoan()), n(asset.getGzCreditLoan()), n(asset.getHuabeiLoan()),
                    n(asset.getOtherLoan()), n(asset.getLentOut()), n(asset.getGuaranteeDeposit()), n(asset.getHousingFund()),
                    n(asset.getLastYearDeposit()), n(asset.getModelHouseReceivable()), n(asset.getMileReceivable()), n(asset.getXingyuReceivable()),
                    n(asset.getHuanyingReceivable()), n(asset.getWanmeiReceivable()), n(asset.getWeiliReceivable()), asset.getRemark(),
                    LocalDateTime.now(), defaultMonth(asset.getMonth()));
        }
    }

    public double stockPurchaseAmount() {
        Number value = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(stockTotalAmount),0) FROM t_shop_stock", Number.class);
        return value == null ? 0D : value.doubleValue();
    }

    public double estimateRate() {
        List<Double> values = jdbcTemplate.query("SELECT configValue " +
                "FROM t_shop_asset_config " +
                "WHERE configKey = 'assetEstimateRate' ", (rs, rowNum) -> rs.getDouble("configValue"));
        return values.isEmpty() ? 0.4D : values.get(0);
    }

    public void saveEstimateRate(Double estimateRate) {
        double value = estimateRate == null ? 0.4D : estimateRate;
        jdbcTemplate.update("INSERT INTO t_shop_asset_config(configKey, configValue, remark, updatedTime) " +
                "VALUES('assetEstimateRate', ?, '预估存款系数', ?) " +
                "ON DUPLICATE KEY UPDATE configValue = VALUES(configValue), updatedTime = VALUES(updatedTime) ", value, LocalDateTime.now());
    }

    public Double actualDepositOfMonth(String month) {
        AssetSnapshot asset = findByMonth(month);
        if (asset.getId() == null) {
            return null;
        }
        double purchaseAmount = asset.getPurchaseAmount() == null || asset.getPurchaseAmount() <= 0 ? stockPurchaseAmount() : n(asset.getPurchaseAmount());
        return n(asset.getCashDeposit()) + receivableTotal(asset) + purchaseAmount + assetItemTotal(asset) - loanTotal(asset);
    }

    public double receivableTotal(AssetSnapshot asset) {
        return n(asset.getModelHouseReceivable()) + n(asset.getMileReceivable()) + n(asset.getXingyuReceivable())
            + n(asset.getHuanyingReceivable()) + n(asset.getWanmeiReceivable()) + n(asset.getWeiliReceivable());
    }

    public double assetItemTotal(AssetSnapshot asset) {
        return n(asset.getLentOut()) + n(asset.getGuaranteeDeposit()) + n(asset.getHousingFund());
    }

    public double loanTotal(AssetSnapshot asset) {
        return n(asset.getCmbCreditLoan()) + n(asset.getGzCreditLoan()) + n(asset.getHuabeiLoan()) + n(asset.getOtherLoan());
    }

    private String defaultMonth(String month) {
        return month == null || month.trim().isEmpty() ? YearMonth.now().toString() : month;
    }

    private double n(Double value) {
        return value == null ? 0D : value;
    }
}
