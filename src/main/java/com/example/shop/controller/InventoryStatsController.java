package com.example.shop.controller;

import com.example.shop.repository.GoodsRepository;
import com.example.shop.repository.StatsRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class InventoryStatsController {
    private final StatsRepository statsRepository;
    private final GoodsRepository goodsRepository;

    public InventoryStatsController(StatsRepository statsRepository, GoodsRepository goodsRepository) {
        this.statsRepository = statsRepository;
        this.goodsRepository = goodsRepository;
    }

    @GetMapping("/inventory-stats")
    public String list(@RequestParam(required = false) Integer goodsId,
                       @RequestParam(defaultValue = "stock") String sortBy,
                       @RequestParam(defaultValue = "asc") String sortDir,
                       Model model) {
        List<Map<String, Object>> rows = statsRepository.inventoryStats(goodsId, sortBy, sortDir);
        int totalStock = rows.stream().mapToInt(row -> ((Number) row.get("stock")).intValue()).sum();
        double totalAmount = rows.stream().mapToDouble(row -> ((Number) row.get("stockAmount")).doubleValue()).sum();
        model.addAttribute("active", "inventoryStats");
        model.addAttribute("goodsId", goodsId);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("rows", rows);
        model.addAttribute("totalStock", totalStock);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("filterGoodsList", goodsRepository.findInStock());
        return "inventory-stats";
    }
}
