package com.example.shop.controller;

import com.example.shop.model.Sale;
import com.example.shop.repository.GoodsRepository;
import com.example.shop.repository.StatsRepository;
import com.example.shop.repository.StockRepository;
import com.example.shop.service.SaleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class DashboardController {
    private final StatsRepository statsRepository;
    private final GoodsRepository goodsRepository;
    private final StockRepository stockRepository;
    private final SaleService saleService;

    public DashboardController(StatsRepository statsRepository, GoodsRepository goodsRepository, StockRepository stockRepository, SaleService saleService) {
        this.statsRepository = statsRepository;
        this.goodsRepository = goodsRepository;
        this.stockRepository = stockRepository;
        this.saleService = saleService;
    }

    @GetMapping({"/", "/dashboard", "/stats"})
    public String dashboard(Model model) {
        model.addAttribute("active", "stats");
        model.addAttribute("stats", statsRepository.dashboard());
        model.addAttribute("topGoods", statsRepository.topGoods());
        model.addAttribute("stockRanking", statsRepository.stockRanking());
        model.addAttribute("monthlySales", statsRepository.monthlySales());
        model.addAttribute("goodsList", goodsRepository.findInStock());
        model.addAttribute("stockList", stockRepository.findPage(null, true, 1, Integer.MAX_VALUE).getRecords());
        Sale sale = new Sale();
        sale.setShop("模型小屋");
        sale.setGoodsSource("进货");
        sale.setPlatform("拼多多");
        model.addAttribute("sale", sale);
        return "dashboard";
    }

    @PostMapping("/dashboard/sales/save")
    public String saveSale(@ModelAttribute Sale sale) {
        saleService.create(sale);
        return "redirect:/dashboard";
    }
}
