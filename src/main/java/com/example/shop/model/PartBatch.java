package com.example.shop.model;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PartBatch {
    private Integer id;
    private Integer goodsId;
    private String name;
    private BigDecimal productCost;
    private String remark;
    private LocalDateTime createdTime;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate recordDate;
    private int partCount;
    private BigDecimal sellingAmount = BigDecimal.ZERO;
    private BigDecimal grossProfit = BigDecimal.ZERO;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getProductCost() { return productCost; }
    public void setProductCost(BigDecimal productCost) { this.productCost = productCost; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
        this.recordDate = createdTime == null ? null : createdTime.toLocalDate();
    }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public int getPartCount() { return partCount; }
    public void setPartCount(int partCount) { this.partCount = partCount; }
    public BigDecimal getSellingAmount() { return sellingAmount; }
    public void setSellingAmount(BigDecimal sellingAmount) { this.sellingAmount = money(sellingAmount); }
    public BigDecimal getGrossProfit() { return grossProfit; }
    public void setGrossProfit(BigDecimal grossProfit) { this.grossProfit = money(grossProfit); }
    public BigDecimal getTotalProfit() { return getGrossProfit().subtract(money(productCost)); }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
