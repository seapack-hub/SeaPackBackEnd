package org.seaPack.controller;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.DividendRule;
import org.seaPack.service.DividendRuleService;
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

    /**
     * 查询指定股票的分红规则列表
     * @param stockId 股票ID
     * @return 分红规则列表
     */
    @GetMapping("/list/{stockId}")
    public ResponseEntity<List<DividendRule>> list(@PathVariable Long stockId) {
        return ResponseEntity.ok(dividendRuleService.getRulesByStockId(stockId));
    }

    /**
     * 查询所有已启用的分红规则
     * @return 启用规则列表
     */
    @GetMapping("/active")
    public ResponseEntity<List<DividendRule>> activeRules() {
        return ResponseEntity.ok(dividendRuleService.getActiveRules());
    }

    /**
     * 新增分红规则
     * @param rule 规则信息
     * @return 影响行数
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody DividendRule rule) {
        return ResponseEntity.ok(dividendRuleService.insertRule(rule));
    }

    /**
     * 更新分红规则
     * @param rule 待更新数据（必须含id）
     * @return 影响行数
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody DividendRule rule) {
        return ResponseEntity.ok(dividendRuleService.updateRule(rule));
    }

    /**
     * 删除分红规则
     * @param id 规则ID
     * @return 影响行数
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(dividendRuleService.deleteRule(id));
    }
}
