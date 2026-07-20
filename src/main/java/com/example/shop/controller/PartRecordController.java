package com.example.shop.controller;

import com.example.shop.model.Goods;
import com.example.shop.model.PartBatch;
import com.example.shop.model.PartSale;
import com.example.shop.repository.GoodsRepository;
import com.example.shop.repository.PartRecordRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PartRecordController {
    private final PartRecordRepository partRecordRepository;
    private final GoodsRepository goodsRepository;

    public PartRecordController(PartRecordRepository partRecordRepository, GoodsRepository goodsRepository) {
        this.partRecordRepository = partRecordRepository;
        this.goodsRepository = goodsRepository;
    }

    @GetMapping("/parts")
    public String list(@RequestParam(required = false) Integer goodsId,
                       @RequestParam(required = false) Integer batchId,
                       Model model) {
        List<PartBatch> batches = partRecordRepository.findBatches(goodsId);
        PartBatch selectedBatch = findSelected(batches, batchId);

        PartBatch batchForm = new PartBatch();
        batchForm.setGoodsId(goodsId);
        PartSale saleForm = new PartSale();
        if (selectedBatch != null) {
            saleForm.setBatchId(selectedBatch.getId());
        }

        model.addAttribute("active", "parts");
        model.addAttribute("goodsId", goodsId);
        model.addAttribute("filterGoodsList", goodsRepository.findAll(null));
        model.addAttribute("batches", batches);
        model.addAttribute("selectedBatch", selectedBatch);
        model.addAttribute("sales", selectedBatch == null
                ? java.util.Collections.emptyList()
                : partRecordRepository.findSales(selectedBatch.getId()));
        model.addAttribute("summary", partRecordRepository.summary(goodsId));
        model.addAttribute("batchForm", batchForm);
        model.addAttribute("saleForm", saleForm);
        return "parts";
    }

    @GetMapping("/parts/detail")
    public String detail(@RequestParam int batchId,
                         @RequestParam(required = false) Integer goodsId,
                         Model model) {
        PartBatch selectedBatch = partRecordRepository.findBatch(batchId);
        if (selectedBatch == null || (goodsId != null && !goodsId.equals(selectedBatch.getGoodsId()))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("goodsId", goodsId);
        model.addAttribute("selectedBatch", selectedBatch);
        model.addAttribute("sales", partRecordRepository.findSales(batchId));
        return "parts :: detail";
    }

    @PostMapping("/parts/batch/save")
    public String saveBatch(@ModelAttribute PartBatch batch) {
        Goods goods = validGoods(batch.getGoodsId());
        if (goods == null) {
            return "redirect:/parts";
        }
        batch.setName(goodsName(goods));
        int id = partRecordRepository.saveBatch(batch);
        PartBatch saved = partRecordRepository.findBatch(id);
        return redirectBatch(saved == null ? batch.getGoodsId() : saved.getGoodsId(), id);
    }

    @PostMapping("/parts/batch/delete")
    public String deleteBatch(@RequestParam int id, @RequestParam(required = false) Integer goodsId) {
        partRecordRepository.deleteBatch(id);
        return redirectGoods(goodsId);
    }

    @PostMapping("/parts/sale/save")
    public String saveSale(@ModelAttribute PartSale sale,
                           @RequestParam(required = false) Integer goodsId) {
        PartBatch batch = sale.getBatchId() == null ? null : partRecordRepository.findBatch(sale.getBatchId());
        List<String> partNumbers = splitPartNumbers(sale.getPartNumber());
        if (batch == null || partNumbers.isEmpty()) {
            return redirectGoods(goodsId);
        }
        BigDecimal sellingPrice = averageMoney(sale.getSellingPrice(), partNumbers.size());
        BigDecimal cost = averageMoney(sale.getCost(), partNumbers.size());
        List<PartSale> sales = new ArrayList<>();
        for (int index = 0; index < partNumbers.size(); index++) {
            PartSale item = new PartSale();
            item.setId(index == 0 ? sale.getId() : null);
            item.setBatchId(sale.getBatchId());
            item.setPartNumber(partNumbers.get(index));
            item.setSellingPrice(sellingPrice);
            item.setCost(cost);
            item.setRemark(sale.getRemark());
            sales.add(item);
        }
        partRecordRepository.saveSales(sales);
        return redirectBatch(batch.getGoodsId(), sale.getBatchId());
    }

    @PostMapping("/parts/sale/delete")
    public String deleteSale(@RequestParam int id, @RequestParam int batchId,
                             @RequestParam(required = false) Integer goodsId) {
        partRecordRepository.deleteSale(id, batchId);
        PartBatch batch = partRecordRepository.findBatch(batchId);
        return redirectBatch(batch == null ? goodsId : batch.getGoodsId(), batchId);
    }

    private PartBatch findSelected(List<PartBatch> batches, Integer batchId) {
        if (batchId != null) {
            for (PartBatch batch : batches) {
                if (batchId.equals(batch.getId())) {
                    return batch;
                }
            }
        }
        return batches.isEmpty() ? null : batches.get(0);
    }

    private List<String> splitPartNumbers(String value) {
        List<String> result = new ArrayList<>();
        if (value == null) return result;
        for (String partNumber : value.split("[,，]")) {
            String cleaned = partNumber.trim();
            if (!cleaned.isEmpty()) result.add(cleaned);
        }
        return result;
    }

    private BigDecimal averageMoney(BigDecimal total, int count) {
        if (total == null || total.signum() < 0) total = BigDecimal.ZERO;
        return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private Goods validGoods(Integer goodsId) {
        if (goodsId == null) return null;
        Goods goods = goodsRepository.findById(goodsId);
        return goods == null || "1".equals(goods.getDeleted()) ? null : goods;
    }

    private String goodsName(Goods goods) {
        StringBuilder name = new StringBuilder(goods.getName() == null ? "" : goods.getName().trim());
        appendName(name, goods.getType());
        appendName(name, goods.getModel());
        return name.toString();
    }

    private void appendName(StringBuilder name, String value) {
        if (value != null && !value.trim().isEmpty()) name.append(':').append(value.trim());
    }

    private String redirectGoods(Integer goodsId) {
        return goodsId == null ? "redirect:/parts" : "redirect:/parts?goodsId=" + goodsId;
    }

    private String redirectBatch(Integer goodsId, int batchId) {
        return redirectGoods(goodsId) + (goodsId == null ? "?" : "&") + "batchId=" + batchId;
    }
}
