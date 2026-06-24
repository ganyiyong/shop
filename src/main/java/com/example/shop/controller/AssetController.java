package com.example.shop.controller;

import com.example.shop.model.AssetSnapshot;
import com.example.shop.repository.AssetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.YearMonth;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AssetController {
    private final AssetRepository assetRepository;

    public AssetController(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @GetMapping("/assets")
    public String assets(@RequestParam(required = false) String month,
                         @RequestParam(required = false) String compareMonth,
                         Model model) {
        YearMonth selectedMonth = parseMonth(month);
        YearMonth selectedCompareMonth = parseMonthOrDefault(compareMonth, selectedMonth.minusMonths(1));
        AssetSnapshot asset = assetRepository.findByMonth(selectedMonth.toString());
        double currentPurchaseAmount = assetRepository.stockPurchaseAmount();
        double estimateRate = assetRepository.estimateRate();
        AssetMetrics metrics = calculateMetrics(asset, selectedMonth, estimateRate, currentPurchaseAmount);
        AssetSnapshot compareAsset = assetRepository.findByMonth(selectedCompareMonth.toString());
        boolean compareAssetExists = compareAsset.getId() != null;
        AssetMetrics compareMetrics = null;
        if (compareAssetExists) {
            compareMetrics = calculateMetrics(compareAsset, selectedCompareMonth, estimateRate, currentPurchaseAmount);
        }

        model.addAttribute("active", "assets");
        model.addAttribute("month", selectedMonth);
        model.addAttribute("compareMonth", selectedCompareMonth);
        model.addAttribute("asset", asset);
        model.addAttribute("compareAsset", compareAsset);
        model.addAttribute("compareAssetExists", compareAssetExists);
        model.addAttribute("compareDiff", compareAssetExists ? compareDiff(asset, metrics, compareAsset, compareMetrics) : new HashMap<String, Double>());
        model.addAttribute("estimateRate", estimateRate);
        model.addAttribute("purchaseAmount", metrics.purchaseAmount);
        model.addAttribute("currentPurchaseAmount", currentPurchaseAmount);
        model.addAttribute("receivableTotal", metrics.receivableTotal);
        model.addAttribute("loanTotal", metrics.loanTotal);
        model.addAttribute("assetItemTotal", metrics.assetItemTotal);
        model.addAttribute("actualDeposit", metrics.actualDeposit);
        model.addAttribute("estimatedDeposit", metrics.estimatedDeposit);
        model.addAttribute("lastYearDeposit", metrics.lastYearDeposit);
        model.addAttribute("lastYearAutoDeposit", metrics.lastYearAutoDeposit);
        model.addAttribute("estimatedNetAsset", metrics.estimatedDeposit);
        model.addAttribute("yearlyProfit", metrics.yearlyProfit);
        model.addAttribute("monthDeposit", metrics.monthDeposit);
        return "assets";
    }

    @PostMapping("/assets/rate/save")
    public String saveRate(@RequestParam(required = false) String month,
                           @RequestParam(required = false) String compareMonth,
                           @RequestParam Double estimateRate) {
        YearMonth selectedMonth = parseMonth(month);
        YearMonth selectedCompareMonth = parseMonthOrDefault(compareMonth, selectedMonth.minusMonths(1));
        assetRepository.saveEstimateRate(estimateRate);
        return redirectToAssets(selectedMonth, selectedCompareMonth);
    }

    @PostMapping("/assets/save")
    public String save(@ModelAttribute AssetSnapshot asset,
                       @RequestParam(required = false) String compareMonth) {
        YearMonth selectedMonth = parseMonth(asset.getMonth());
        YearMonth selectedCompareMonth = parseMonthOrDefault(compareMonth, selectedMonth.minusMonths(1));
        asset.setMonth(selectedMonth.toString());
        if (asset.getPurchaseAmount() == null || asset.getPurchaseAmount() <= 0) {
            asset.setPurchaseAmount(assetRepository.stockPurchaseAmount());
        }
        assetRepository.save(asset);
        return redirectToAssets(selectedMonth, selectedCompareMonth);
    }

    @PostMapping("/assets/snapshot/save")
    @ResponseBody
    public Map<String, Object> saveSnapshot(@ModelAttribute AssetSnapshot asset) {
        YearMonth selectedMonth = parseMonth(asset.getMonth());
        asset.setMonth(selectedMonth.toString());
        if (asset.getPurchaseAmount() == null || asset.getPurchaseAmount() <= 0) {
            asset.setPurchaseAmount(assetRepository.stockPurchaseAmount());
        }
        assetRepository.saveHistory(asset);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("month", selectedMonth.toString());
        result.put("message", "快照保存成功");
        return result;
    }

    @GetMapping("/assets/snapshots")
    public String snapshots(@RequestParam(required = false) String month,
                            @RequestParam(required = false) String compareMonth,
                            Model model) {
        YearMonth selectedMonth = parseMonth(month);
        YearMonth selectedCompareMonth = parseMonthOrDefault(compareMonth, selectedMonth.minusMonths(1));
        AssetSnapshot currentAsset = assetRepository.findByMonth(selectedMonth.toString());
        double currentPurchaseAmount = assetRepository.stockPurchaseAmount();
        double estimateRate = assetRepository.estimateRate();
        calculateMetrics(currentAsset, selectedMonth, estimateRate, currentPurchaseAmount);
        List<AssetSnapshot> snapshots = assetRepository.historyByMonth(selectedMonth.toString());
        for (AssetSnapshot snapshot : snapshots) {
            calculateMetrics(snapshot, selectedMonth, estimateRate, currentPurchaseAmount);
        }
        model.addAttribute("active", "assets");
        model.addAttribute("month", selectedMonth);
        model.addAttribute("compareMonth", selectedCompareMonth);
        model.addAttribute("currentAsset", currentAsset);
        model.addAttribute("snapshots", snapshots);
        return "asset-snapshots";
    }

    @PostMapping("/assets/snapshot/delete")
    public String deleteSnapshot(@RequestParam int id,
                                 @RequestParam(required = false) String month,
                                 @RequestParam(required = false) String compareMonth) {
        YearMonth selectedMonth = parseMonth(month);
        YearMonth selectedCompareMonth = parseMonthOrDefault(compareMonth, selectedMonth.minusMonths(1));
        assetRepository.deleteHistory(id);
        return "redirect:/assets/snapshots?month=" + selectedMonth + "&compareMonth=" + selectedCompareMonth;
    }

    private YearMonth parseMonth(String value) {
        return parseMonthOrDefault(value, YearMonth.now());
    }

    private YearMonth parseMonthOrDefault(String value, YearMonth fallback) {
        try {
            return value == null || value.trim().isEmpty() ? fallback : YearMonth.parse(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private double n(Double value) {
        return value == null ? 0D : value;
    }

    private AssetMetrics calculateMetrics(AssetSnapshot asset, YearMonth month, double estimateRate, double currentPurchaseAmount) {
        applyPurchaseAmount(asset, currentPurchaseAmount);
        double purchaseAmount = n(asset.getPurchaseAmount());
        double receivableTotal = assetRepository.receivableTotal(asset);
        double loanTotal = assetRepository.loanTotal(asset);
        double assetItemTotal = assetRepository.assetItemTotal(asset);
        double actualDeposit = n(asset.getCashDeposit()) + receivableTotal + purchaseAmount + assetItemTotal - loanTotal;
        double estimatedDeposit = actualDeposit + purchaseAmount * estimateRate;
        Double lastYearAutoDeposit = actualDepositOfMonth(month.minusYears(1).withMonth(12), currentPurchaseAmount);
        double lastYearDeposit = lastYearAutoDeposit == null ? n(asset.getLastYearDeposit()) : lastYearAutoDeposit;
        double previousActualDeposit = n(actualDepositOfMonth(month.minusMonths(1), currentPurchaseAmount));

        AssetMetrics metrics = new AssetMetrics();
        metrics.purchaseAmount = purchaseAmount;
        metrics.receivableTotal = receivableTotal;
        metrics.loanTotal = loanTotal;
        metrics.assetItemTotal = assetItemTotal;
        metrics.actualDeposit = actualDeposit;
        metrics.estimatedDeposit = estimatedDeposit;
        metrics.lastYearAutoDeposit = lastYearAutoDeposit;
        metrics.lastYearDeposit = lastYearDeposit;
        metrics.yearlyProfit = actualDeposit - lastYearDeposit;
        metrics.monthDeposit = actualDeposit - previousActualDeposit;

        asset.setLastYearDeposit(lastYearDeposit);
        asset.setActualDeposit(metrics.actualDeposit);
        asset.setMonthDeposit(metrics.monthDeposit);
        asset.setEstimatedDeposit(metrics.estimatedDeposit);
        asset.setYearlyProfit(metrics.yearlyProfit);
        return metrics;
    }

    private Double actualDepositOfMonth(YearMonth month, double currentPurchaseAmount) {
        AssetSnapshot asset = assetRepository.findByMonth(month.toString());
        if (asset.getId() == null) {
            return null;
        }
        applyPurchaseAmount(asset, currentPurchaseAmount);
        return n(asset.getCashDeposit()) + assetRepository.receivableTotal(asset)
            + n(asset.getPurchaseAmount()) + assetRepository.assetItemTotal(asset) - assetRepository.loanTotal(asset);
    }

    private void applyPurchaseAmount(AssetSnapshot asset, double currentPurchaseAmount) {
        double purchaseAmount = asset.getId() == null || asset.getPurchaseAmount() == null || asset.getPurchaseAmount() <= 0
            ? currentPurchaseAmount
            : n(asset.getPurchaseAmount());
        asset.setPurchaseAmount(purchaseAmount);
    }

    private String redirectToAssets(YearMonth selectedMonth, YearMonth compareMonth) {
        return "redirect:/assets?month=" + selectedMonth + "&compareMonth=" + compareMonth;
    }

    private Map<String, Double> compareDiff(AssetSnapshot asset, AssetMetrics metrics, AssetSnapshot compareAsset, AssetMetrics compareMetrics) {
        Map<String, Double> diff = new HashMap<>();
        diff.put("cashDeposit", n(asset.getCashDeposit()) - n(compareAsset.getCashDeposit()));
        diff.put("purchaseAmount", metrics.purchaseAmount - compareMetrics.purchaseAmount);
        diff.put("receivableTotal", metrics.receivableTotal - compareMetrics.receivableTotal);
        diff.put("loanTotal", metrics.loanTotal - compareMetrics.loanTotal);
        diff.put("actualDeposit", metrics.actualDeposit - compareMetrics.actualDeposit);
        diff.put("estimatedDeposit", metrics.estimatedDeposit - compareMetrics.estimatedDeposit);
        diff.put("yearlyProfit", metrics.yearlyProfit - compareMetrics.yearlyProfit);
        return diff;
    }

    private static class AssetMetrics {
        private double purchaseAmount;
        private double receivableTotal;
        private double loanTotal;
        private double assetItemTotal;
        private double actualDeposit;
        private double estimatedDeposit;
        private Double lastYearAutoDeposit;
        private double lastYearDeposit;
        private double yearlyProfit;
        private double monthDeposit;
    }
}
