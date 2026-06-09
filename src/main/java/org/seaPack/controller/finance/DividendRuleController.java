package org.seaPack.controller.finance;

import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.seaPack.model.finance.DividendRule; // 股息规则实体
import org.seaPack.service.finance.DividendRuleService; // 股息规则服务
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.http.ResponseEntity; // HTTP 响应实体
import org.springframework.web.bind.annotation.*; // Spring Web MVC 注解

import java.util.List; // List 集合

/**
 * 股息规则控制器
 * 提供股息规则的分组查询、新增、修改和删除接口。
 */
@Slf4j // Lombok 日志
@RestController // 标识为 RESTful 控制器
@RequestMapping("/dividendRule") // 请求基础路径
public class DividendRuleController {

    @Autowired // 注入股息规则服务
    private DividendRuleService dividendRuleService;

    /**
     * 根据股票 ID 查询股息规则列表
     * @param stockId 股票 ID
     */
    @GetMapping("/list/{stockId}")
    public ResponseEntity<List<DividendRule>> list(@PathVariable Long stockId) {
        return ResponseEntity.ok(dividendRuleService.getRulesByStockId(stockId));
    }

    /**
     * 查询所有启用的股息规则
     */
    @GetMapping("/active")
    public ResponseEntity<List<DividendRule>> activeRules() {
        return ResponseEntity.ok(dividendRuleService.getActiveRules());
    }

    /**
     * 新增股息规则
     * @param rule 股息规则实体
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody DividendRule rule) {
        return ResponseEntity.ok(dividendRuleService.insertRule(rule));
    }

    /**
     * 修改股息规则
     * @param rule 股息规则实体（需含 id）
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody DividendRule rule) {
        return ResponseEntity.ok(dividendRuleService.updateRule(rule));
    }

    /**
     * 删除股息规则
     * @param id 规则 ID
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(dividendRuleService.deleteRule(id));
    }
}