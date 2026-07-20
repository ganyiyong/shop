package com.example.shop.model;

import java.math.BigDecimal;

public class PartSummary {
    private int batchCount;
    private int partCount;
    private BigDecimal sellingAmount = BigDecimal.ZERO;
    private BigDecimal grossProfit = BigDecimal.ZERO;
    private BigDecimal productCost = BigDecimal.ZERO;

    public int getBatchCount() { return batchCount; }
    public void setBatchCount(int batchCount) { this.batchCount = batchCount; }
    public int getPartCount() { return partCount; }
    public void setPartCount(int partCount) { this.partCount = partCount; }
    public BigDecimal getSellingAmount() { return sellingAmount; }
    public void setSellingAmount(BigDecimal sellingAmount) { this.sellingAmount = money(sellingAmount); }
    public BigDecimal getGrossProfit() { return grossProfit; }
    public void setGrossProfit(BigDecimal grossProfit) { this.grossProfit = money(grossProfit); }
    public BigDecimal getProductCost() { return productCost; }
    public void setProductCost(BigDecimal productCost) { this.productCost = money(productCost); }
    public BigDecimal getTotalProfit() { return getGrossProfit().subtract(getProductCost()); }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
