package com.example.shop.controller;

import com.example.shop.model.Stock;
import com.example.shop.repository.GoodsRepository;
import com.example.shop.repository.StockRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StockController {
    private final StockRepository stockRepository;
    private final GoodsRepository goodsRepository;

    public StockController(StockRepository stockRepository, GoodsRepository goodsRepository) {
        this.stockRepository = stockRepository;
        this.goodsRepository = goodsRepository;
    }

    @GetMapping("/stock")
    public String list(@RequestParam(required = false) Integer goodsId,
                       @RequestParam(defaultValue = "valid") String stockScope,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        boolean validOnly = !"all".equals(stockScope);
        model.addAttribute("active", "stock");
        model.addAttribute("goodsId", goodsId);
        model.addAttribute("stockScope", validOnly ? "valid" : "all");
        model.addAttribute("pageData", stockRepository.findPage(goodsId, validOnly, page, size));
        model.addAttribute("goodsList", goodsRepository.findActive());
        model.addAttribute("filterGoodsList", goodsRepository.findInStock());
        model.addAttribute("stock", new Stock());
        return "stock";
    }

    @PostMapping("/stock/save")
    public String save(@ModelAttribute Stock stock) {
        stockRepository.save(stock);
        return "redirect:/stock";
    }

    @PostMapping("/stock/delete")
    public String delete(@RequestParam int id) {
        stockRepository.delete(id);
        return "redirect:/stock";
    }
}
