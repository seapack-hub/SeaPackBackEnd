package org.seaPack.controller.finance;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.finance.StockDividend;
import org.seaPack.service.finance.StockDividendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/stockDividend")
public class StockDividendController {

    @Autowired
    private StockDividendService stockDividendService;

    @GetMapping("/list")
    public ResponseEntity<PageInfo<StockDividend>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String stockCode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String dividendType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(
                stockDividendService.getList(pageNum, pageSize, stockCode, year, dividendType, status, keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockDividend> detail(@PathVariable Long id) {
        return ResponseEntity.ok(stockDividendService.getById(id));
    }

    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody StockDividend record) {
        return ResponseEntity.ok(stockDividendService.insert(record));
    }

    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody StockDividend record) {
        return ResponseEntity.ok(stockDividendService.update(record));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(stockDividendService.delete(id));
    }
}