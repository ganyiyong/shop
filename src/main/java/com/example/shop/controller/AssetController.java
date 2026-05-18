package com.example.shop.controller;

import com.example.shop.model.AssetSnapshot;
import com.example.shop.repository.AssetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;

@Controller
public class AssetController {
    private final AssetRepository assetRepository;

    public AssetController(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @GetMapping("/assets")
    public String assets(@RequestParam(required = false) String month, Model model) {
        YearMonth selectedMonth = parseMonth(month);
        AssetSnapshot asset = assetRepository.findByMonth(selectedMonth.toString());
        double currentPurchaseAmount = assetRepository.stockPurchaseAmount();
        double purchaseAmount = asset.getId() == null || asset.getPurchaseAmount() == null || asset.getPurchaseAmount() <= 0
            ? currentPurchaseAmount
            : n(asset.getPurchaseAmount());
        asset.setPurchaseAmount(purchaseAmount);
        double estimateRate = assetRepository.estimateRate();
        double receivableTotal = assetRepository.receivableTotal(asset);
        double loanTotal = assetRepository.loanTotal(asset);
        double assetItemTotal = assetRepository.assetItemTotal(asset);
        double actualDeposit = n(asset.getCashDeposit()) + receivableTotal + purchaseAmount + assetItemTotal - loanTotal;
        double estimatedDeposit = actualDeposit + purchaseAmount * estimateRate;
        double estimatedNetAsset = estimatedDeposit;
        Double lastYearAutoDeposit = assetRepository.actualDepositOfMonth(selectedMonth.minusYears(1).withMonth(12).toString());
        double lastYearDeposit = lastYearAutoDeposit == null ? n(asset.getLastYearDeposit()) : lastYearAutoDeposit;
        asset.setLastYearDeposit(lastYearDeposit);
        double yearlyProfit = actualDeposit - lastYearDeposit;
        double previousActualDeposit = previousActualDeposit(selectedMonth.minusMonths(1), currentPurchaseAmount);
        double monthDeposit = actualDeposit - previousActualDeposit;

        model.addAttribute("active", "assets");
        model.addAttribute("month", selectedMonth);
        model.addAttribute("asset", asset);
        model.addAttribute("estimateRate", estimateRate);
        model.addAttribute("purchaseAmount", purchaseAmount);
        model.addAttribute("currentPurchaseAmount", currentPurchaseAmount);
        model.addAttribute("receivableTotal", receivableTotal);
        model.addAttribute("loanTotal", loanTotal);
        model.addAttribute("assetItemTotal", assetItemTotal);
        model.addAttribute("actualDeposit", actualDeposit);
        model.addAttribute("estimatedDeposit", estimatedDeposit);
        model.addAttribute("lastYearDeposit", lastYearDeposit);
        model.addAttribute("lastYearAutoDeposit", lastYearAutoDeposit);
        model.addAttribute("estimatedNetAsset", estimatedNetAsset);
        model.addAttribute("yearlyProfit", yearlyProfit);
        model.addAttribute("monthDeposit", monthDeposit);
        return "assets";
    }

    @PostMapping("/assets/rate/save")
    public String saveRate(@RequestParam(required = false) String month,
                           @RequestParam Double estimateRate) {
        YearMonth selectedMonth = parseMonth(month);
        assetRepository.saveEstimateRate(estimateRate);
        return "redirect:/assets?month=" + selectedMonth;
    }

    @PostMapping("/assets/save")
    public String save(@ModelAttribute AssetSnapshot asset) {
        YearMonth selectedMonth = parseMonth(asset.getMonth());
        asset.setMonth(selectedMonth.toString());
        if (asset.getPurchaseAmount() == null || asset.getPurchaseAmount() <= 0) {
            asset.setPurchaseAmount(assetRepository.stockPurchaseAmount());
        }
        assetRepository.save(asset);
        return "redirect:/assets?month=" + selectedMonth;
    }

    private YearMonth parseMonth(String value) {
        try {
            return value == null || value.trim().isEmpty() ? YearMonth.now() : YearMonth.parse(value);
        } catch (Exception ignored) {
            return YearMonth.now();
        }
    }

    private double n(Double value) {
        return value == null ? 0D : value;
    }

    private double previousActualDeposit(YearMonth month, double currentPurchaseAmount) {
        AssetSnapshot asset = assetRepository.findByMonth(month.toString());
        if (asset.getId() == null) {
            return 0D;
        }
        double purchaseAmount = asset.getPurchaseAmount() == null || asset.getPurchaseAmount() <= 0
            ? currentPurchaseAmount
            : n(asset.getPurchaseAmount());
        double actualDeposit = n(asset.getCashDeposit()) + assetRepository.receivableTotal(asset)
            + purchaseAmount + assetRepository.assetItemTotal(asset) - assetRepository.loanTotal(asset);
        return actualDeposit;
    }
}
