package com.example.shop.repository;

import com.example.shop.model.ExtraCost;
import com.example.shop.model.AssetSnapshot;
import com.example.shop.model.Goods;
import com.example.shop.model.Sale;
import com.example.shop.model.Stock;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;

final class RowMappers {
    private RowMappers() {
    }

    static LocalDateTime time(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    static final RowMapper<Goods> GOODS = (rs, rowNum) -> {
        Goods item = new Goods();
        item.setId(rs.getInt("id"));
        item.setName(rs.getString("name"));
        item.setType(rs.getString("type"));
        item.setProducer(rs.getString("producer"));
        item.setModel(rs.getString("model"));
        item.setImgUrl(rs.getString("imgUrl"));
        item.setKeywords(rs.getString("keywords"));
        item.setCreatedTime(time(rs.getTimestamp("createdTime")));
        item.setState(rs.getString("state"));
        item.setSortKey(rs.getInt("sortKey"));
        item.setRemark(rs.getString("remark"));
        item.setDeleted(rs.getString("deleted"));
        trySetGoodsStats(rs, item);
        return item;
    };

    static final RowMapper<Stock> STOCK = (rs, rowNum) -> {
        Stock item = new Stock();
        item.setId(rs.getInt("id"));
        item.setGoodsId(rs.getInt("goodsId"));
        try { item.setGoodsName(rs.getString("goodsName")); } catch (Exception ignored) { }
        item.setCtns(rs.getDouble("ctns"));
        item.setNumber(rs.getInt("number"));
        item.setUnitPrice(rs.getDouble("unitPrice"));
        item.setCost(rs.getDouble("cost"));
        item.setSellingPrice(rs.getDouble("sellingPrice"));
        item.setStock(rs.getInt("stock"));
        item.setTotalAmount(rs.getDouble("totalAmount"));
        item.setStockTotalAmount(rs.getDouble("stockTotalAmount"));
        item.setCreatedTime(time(rs.getTimestamp("createdTime")));
        item.setState(rs.getString("state"));
        return item;
    };

    static final RowMapper<Sale> SALE = (rs, rowNum) -> {
        Sale item = new Sale();
        item.setId(rs.getInt("id"));
        item.setGoodsId(rs.getInt("goodsId"));
        try { item.setGoodsName(rs.getString("goodsName")); } catch (Exception ignored) { }
        item.setPlatform(rs.getString("platform"));
        item.setShop(rs.getString("shop"));
        item.setCostPrice(rs.getDouble("costPrice"));
        item.setSellingPrice(rs.getDouble("sellingPrice"));
        item.setCharge(rs.getDouble("charge"));
        item.setProfit(rs.getDouble("profit"));
        item.setGoodsSource(rs.getString("goodsSource"));
        item.setCreatedTime(time(rs.getTimestamp("createdTime")));
        item.setRemark(rs.getString("remark"));
        return item;
    };

    static final RowMapper<ExtraCost> EXTRA_COST = (rs, rowNum) -> {
        ExtraCost item = new ExtraCost();
        item.setId(rs.getInt("id"));
        item.setCategory(rs.getString("category"));
        item.setAmount(rs.getDouble("amount"));
        item.setCreatedTime(time(rs.getTimestamp("createdTime")));
        item.setRemark(rs.getString("remark"));
        return item;
    };

    static final RowMapper<AssetSnapshot> ASSET = (rs, rowNum) -> {
        AssetSnapshot item = new AssetSnapshot();
        item.setId(rs.getInt("id"));
        item.setMonth(rs.getString("month"));
        item.setPurchaseAmount(rs.getDouble("purchaseAmount"));
        item.setCashDeposit(rs.getDouble("cashDeposit"));
        item.setCmbCreditLoan(rs.getDouble("cmbCreditLoan"));
        item.setGzCreditLoan(rs.getDouble("gzCreditLoan"));
        item.setHuabeiLoan(rs.getDouble("huabeiLoan"));
        item.setOtherLoan(rs.getDouble("otherLoan"));
        item.setLentOut(rs.getDouble("lentOut"));
        item.setGuaranteeDeposit(rs.getDouble("guaranteeDeposit"));
        item.setHousingFund(rs.getDouble("housingFund"));
        item.setLastYearDeposit(rs.getDouble("lastYearDeposit"));
        item.setModelHouseReceivable(rs.getDouble("modelHouseReceivable"));
        item.setMileReceivable(rs.getDouble("mileReceivable"));
        item.setXingyuReceivable(rs.getDouble("xingyuReceivable"));
        item.setHuanyingReceivable(rs.getDouble("huanyingReceivable"));
        item.setWanmeiReceivable(rs.getDouble("wanmeiReceivable"));
        item.setWeiliReceivable(rs.getDouble("weiliReceivable"));
        item.setRemark(rs.getString("remark"));
        try { item.setSourceAssetId(rs.getInt("sourceAssetId")); } catch (Exception ignored) { }
        try { item.setSnapshotTime(time(rs.getTimestamp("snapshotTime"))); } catch (Exception ignored) { }
        item.setCreatedTime(time(rs.getTimestamp("createdTime")));
        item.setUpdatedTime(time(rs.getTimestamp("updatedTime")));
        return item;
    };

    private static void trySetGoodsStats(java.sql.ResultSet rs, Goods item) {
        try { item.setStock(rs.getInt("stock")); } catch (Exception ignored) { }
        try { item.setStockAmount(rs.getDouble("stockAmount")); } catch (Exception ignored) { }
        try { item.setLastPrice(rs.getDouble("lastPrice")); } catch (Exception ignored) { }
    }
}
