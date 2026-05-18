package com.example.shop.service;

import com.example.shop.model.Sale;
import com.example.shop.model.PageResult;
import com.example.shop.model.Stock;
import com.example.shop.repository.SaleRepository;
import com.example.shop.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Service
public class SaleService {
    private final SaleRepository saleRepository;
    private final StockRepository stockRepository;

    public SaleService(SaleRepository saleRepository, StockRepository stockRepository) {
        this.saleRepository = saleRepository;
        this.stockRepository = stockRepository;
    }

    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    public PageResult<Sale> page(LocalDateTime startTime, LocalDateTime endTime, int page, int size) {
        return saleRepository.page(startTime, endTime, page, size);
    }

    public PageResult<Sale> page(LocalDateTime startTime, LocalDateTime endTime, Integer goodsId, int page, int size) {
        return saleRepository.page(startTime, endTime, goodsId, page, size);
    }

    public List<Map<String, Object>> goodsStats(LocalDateTime startTime, LocalDateTime endTime, Integer goodsId) {
        return saleRepository.goodsStats(startTime, endTime, goodsId);
    }

    @Transactional
    public void create(Sale sale) {
        sale.setPlatform(resolvePlatform(sale.getShop()));
        Stock selectedStock = resolveStock(sale);
        double costPrice = selectedStock == null || selectedStock.getCost() == null ? 0D : selectedStock.getCost();
        sale.setCostPrice(costPrice);
        double sellingPrice = sale.getSellingPrice() == null ? 0D : sale.getSellingPrice();
        double charge = roundMoney(sellingPrice * 0.006D);
        sale.setCharge(charge);
        sale.setProfit(sellingPrice - costPrice - charge);
        saleRepository.save(sale);
        decreaseOneStock(selectedStock);
    }

    public void delete(int id) {
        saleRepository.delete(id);
    }

    private Stock resolveStock(Sale sale) {
        if (sale.getStockId() != null) {
            Stock stock = stockRepository.findById(sale.getStockId());
            if (stock != null && stock.getStock() != null && stock.getStock() > 0) {
                sale.setGoodsId(stock.getGoodsId());
                return stock;
            }
        }
        if (sale.getGoodsId() == null) {
            return null;
        }
        List<Stock> stocks = stockRepository.findAvailableByGoodsId(sale.getGoodsId());
        return stocks.isEmpty() ? null : stocks.get(0);
    }

    private void decreaseOneStock(Stock stock) {
        if (stock == null || stock.getId() == null) {
            return;
        }
        stockRepository.decreaseStock(stock.getId(), 1, stock.getUnitPrice() == null ? 0D : stock.getUnitPrice());
    }

    private String resolvePlatform(String shop) {
        if ("模型小屋".equals(shop) || "星域模型".equals(shop) || "米乐模型".equals(shop)) {
            return "拼多多";
        }
        if ("幻影模玩".equals(shop) || "完美动漫".equals(shop)) {
            return "淘宝";
        }
        if ("维利动漫".equals(shop)) {
            return "抖店";
        }
        return "";
    }

    private double roundMoney(double value) {
        return Math.round(value * 100D) / 100D;
    }
}
