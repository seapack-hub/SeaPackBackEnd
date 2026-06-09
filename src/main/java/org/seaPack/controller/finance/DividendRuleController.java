package org.seaPack.controller.finance;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.finance.DividendRule;
import org.seaPack.service.finance.DividendRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dividendRule")
public class DividendRuleController {

    @Autowired
    private DividendRuleService dividendRuleService;

    @GetMapping("/list/{stockId}")
    public ResponseEntity<List<DividendRule>> list(@PathVariable Long stockId) {
        return ResponseEntity.ok(dividendRuleService.getRulesByStockId(stockId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<DividendRule>> activeRules() {
        return ResponseEntity.ok(dividendRuleService.getActiveRules());
    }

    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody DividendRule rule) {
        return ResponseEntity.ok(dividendRuleService.insertRule(rule));
    }

    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody DividendRule rule) {
        return ResponseEntity.ok(dividendRuleService.updateRule(rule));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(dividendRuleService.deleteRule(id));
    }
}