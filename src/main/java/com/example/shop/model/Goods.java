package com.example.shop.model;

import java.time.LocalDateTime;

public class Goods {
    private Integer id;
    private String name;
    private String type;
    private String producer;
    private String model;
    private String imgUrl;
    private String keywords;
    private LocalDateTime createdTime;
    private String state;
    private Integer sortKey;
    private String remark;
    private String deleted;
    private Integer stock;
    private Double stockAmount;
    private Double lastPrice;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getProducer() { return producer; }
    public void setProducer(String producer) { this.producer = producer; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Integer getSortKey() { return sortKey; }
    public void setSortKey(Integer sortKey) { this.sortKey = sortKey; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getDeleted() { return deleted; }
    public void setDeleted(String deleted) { this.deleted = deleted; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Double getStockAmount() { return stockAmount; }
    public void setStockAmount(Double stockAmount) { this.stockAmount = stockAmount; }
    public Double getLastPrice() { return lastPrice; }
    public void setLastPrice(Double lastPrice) { this.lastPrice = lastPrice; }
}
