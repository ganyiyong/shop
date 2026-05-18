package com.example.shop.controller;

import com.example.shop.model.Goods;
import com.example.shop.repository.GoodsRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GoodsController {
    private final GoodsRepository goodsRepository;

    public GoodsController(GoodsRepository goodsRepository) {
        this.goodsRepository = goodsRepository;
    }

    @GetMapping("/goods")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer goodsId,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        model.addAttribute("active", "goods");
        model.addAttribute("keyword", keyword);
        model.addAttribute("goodsId", goodsId);
        model.addAttribute("goodsList", goodsRepository.findActive());
        model.addAttribute("filterGoodsList", goodsRepository.findInStock());
        model.addAttribute("pageData", goodsRepository.findPage(keyword, goodsId, page, size));
        model.addAttribute("goods", new Goods());
        return "goods";
    }

    @GetMapping("/goods/form")
    public String form(@RequestParam(required = false) Integer id, Model model) {
        Goods goods = id == null ? new Goods() : goodsRepository.findById(id);
        if (goods == null) {
            goods = new Goods();
        }
        if (goods.getState() == null) {
            goods.setState("0");
        }
        if (goods.getSortKey() == null) {
            goods.setSortKey(0);
        }
        model.addAttribute("active", "goods");
        model.addAttribute("goods", goods);
        return "goods-form";
    }

    @PostMapping("/goods/save")
    public String save(@ModelAttribute Goods goods) {
        goodsRepository.save(goods);
        return "redirect:/goods";
    }

    @PostMapping("/goods/delete")
    public String delete(@RequestParam int id) {
        goodsRepository.delete(id);
        return "redirect:/goods";
    }
}
