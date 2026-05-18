package com.example.shop.model;

import java.time.LocalDateTime;

public class Sale {
    private Integer id;
    private Integer goodsId;
    private Integer stockId;
    private String goodsName;
    private String platform;
    private String shop;
    private Double costPrice;
    private Double sellingPrice;
    private Double charge;
    private Double profit;
    private String goodsSource;
    private LocalDateTime createdTime;
    private String remark;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }
    public Integer getStockId() { return stockId; }
    public void setStockId(Integer stockId) { this.stockId = stockId; }
    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getShop() { return shop; }
    public void setShop(String shop) { this.shop = shop; }
    public Double getCostPrice() { return costPrice; }
    public void setCostPrice(Double costPrice) { this.costPrice = costPrice; }
    public Double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(Double sellingPrice) { this.sellingPrice = sellingPrice; }
    public Double getCharge() { return charge; }
    public void setCharge(Double charge) { this.charge = charge; }
    public Double getProfit() { return profit; }
    public void setProfit(Double profit) { this.profit = profit; }
    public String getGoodsSource() { return goodsSource; }
    public void setGoodsSource(String goodsSource) { this.goodsSource = goodsSource; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
