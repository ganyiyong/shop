package com.example.shop.controller;

import com.example.shop.model.Sale;
import com.example.shop.repository.GoodsRepository;
import com.example.shop.repository.StockRepository;
import com.example.shop.service.SaleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class SaleController {
    private final SaleService saleService;
    private final GoodsRepository goodsRepository;
    private final StockRepository stockRepository;

    public SaleController(SaleService saleService, GoodsRepository goodsRepository, StockRepository stockRepository) {
        this.saleService = saleService;
        this.goodsRepository = goodsRepository;
        this.stockRepository = stockRepository;
    }

    @GetMapping("/sales")
    public String list(@RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       @RequestParam(required = false) Integer goodsId,
                       @RequestParam(defaultValue = "detail") String view,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        LocalDate start = parseDate(startDate, LocalDate.now());
        LocalDate end = parseDate(endDate, start);
        model.addAttribute("active", "sales");
        model.addAttribute("pageData", saleService.page(start.atStartOfDay(), end.plusDays(1).atStartOfDay(), goodsId, page, size));
        List<Map<String, Object>> goodsStats = saleService.goodsStats(start.atStartOfDay(), end.plusDays(1).atStartOfDay(), goodsId);
        model.addAttribute("goodsStats", goodsStats);
        model.addAttribute("statsSaleCount", goodsStats.stream().mapToInt(row -> ((Number) row.get("saleCount")).intValue()).sum());
        model.addAttribute("statsCostPrice", goodsStats.stream().mapToDouble(row -> ((Number) row.get("costPrice")).doubleValue()).sum());
        model.addAttribute("statsSellingPrice", goodsStats.stream().mapToDouble(row -> ((Number) row.get("sellingPrice")).doubleValue()).sum());
        model.addAttribute("statsCharge", goodsStats.stream().mapToDouble(row -> ((Number) row.get("charge")).doubleValue()).sum());
        model.addAttribute("statsProfit", goodsStats.stream().mapToDouble(row -> ((Number) row.get("profit")).doubleValue()).sum());
        model.addAttribute("goodsList", goodsRepository.findInStock());
        model.addAttribute("stockList", stockRepository.findAvailableForSale());
        Sale sale = new Sale();
        sale.setShop("模型小屋");
        sale.setGoodsSource("进货");
        sale.setPlatform("拼多多");
        model.addAttribute("sale", sale);
        model.addAttribute("goodsId", goodsId);
        model.addAttribute("view", "stats".equals(view) ? "stats" : "detail");
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        return "sales";
    }

    @PostMapping("/sales/save")
    public String save(@ModelAttribute Sale sale) {
        saleService.create(sale);
        return "redirect:/sales";
    }

    @PostMapping("/sales/delete")
    public String delete(@RequestParam int id) {
        saleService.delete(id);
        return "redirect:/sales";
    }

    private LocalDate parseDate(String value, LocalDate defaultValue) {
        try {
            return value == null || value.trim().isEmpty() ? defaultValue : LocalDate.parse(value);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }
}
