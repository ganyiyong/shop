package com.example.shop.controller;

import com.example.shop.model.ExtraCost;
import com.example.shop.repository.ExtraCostRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;

@Controller
public class ExtraCostController {
    private final ExtraCostRepository extraCostRepository;

    public ExtraCostController(ExtraCostRepository extraCostRepository) {
        this.extraCostRepository = extraCostRepository;
    }

    @GetMapping("/costs")
    public String list(@RequestParam(required = false) String month,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        YearMonth selectedMonth = parseMonth(month);
        LocalDate start = selectedMonth.atDay(1);
        LocalDate end = selectedMonth.plusMonths(1).atDay(1);
        model.addAttribute("active", "costs");
        model.addAttribute("pageData", extraCostRepository.page(start.atStartOfDay(), end.atStartOfDay(), page, size));
        model.addAttribute("cost", new ExtraCost());
        model.addAttribute("month", selectedMonth);
        return "costs";
    }

    @PostMapping("/costs/save")
    public String save(@ModelAttribute ExtraCost cost) {
        extraCostRepository.save(cost);
        return "redirect:/costs";
    }

    @PostMapping("/costs/delete")
    public String delete(@RequestParam int id) {
        extraCostRepository.delete(id);
        return "redirect:/costs";
    }

    private YearMonth parseMonth(String value) {
        try {
            return value == null || value.trim().isEmpty() ? YearMonth.now() : YearMonth.parse(value);
        } catch (Exception ignored) {
            return YearMonth.now();
        }
    }
}
