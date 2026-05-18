package com.example.shop.model;

import java.time.LocalDateTime;

public class Stock {
    private Integer id;
    private Integer goodsId;
    private String goodsName;
    private Double ctns;
    private Integer number;
    private Double unitPrice;
    private Double cost;
    private Double sellingPrice;
    private Integer stock;
    private Double totalAmount;
    private Double stockTotalAmount;
    private LocalDateTime createdTime;
    private String state;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }
    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }
    public Double getCtns() { return ctns; }
    public void setCtns(Double ctns) { this.ctns = ctns; }
    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }
    public Double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(Double sellingPrice) { this.sellingPrice = sellingPrice; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public Double getStockTotalAmount() { return stockTotalAmount; }
    public void setStockTotalAmount(Double stockTotalAmount) { this.stockTotalAmount = stockTotalAmount; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
