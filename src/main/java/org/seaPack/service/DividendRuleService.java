package org.seaPack.service;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.DividendRuleMapper;
import org.seaPack.model.DividendRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = false)
public class DividendRuleService {

    @Autowired
    private DividendRuleMapper dividendRuleMapper;

    /**
     * 查询指定股票的所有分红规则
     * @param stockId 股票ID
     * @return 分红规则列表
     */
    public List<DividendRule> getRulesByStockId(Long stockId) {
        return dividendRuleMapper.selectRulesByStockId(stockId);
    }

    /**
     * 查询所有已启用的分红规则
     * @return 启用规则列表
     */
    public List<DividendRule> getActiveRules() {
        return dividendRuleMapper.selectActiveRules();
    }

    /**
     * 新增分红规则
     * @param rule 规则信息
     * @return 影响行数
     */
    public int insertRule(DividendRule rule) {
        return dividendRuleMapper.insertRule(rule);
    }

    /**
     * 更新分红规则
     * @param rule 待更新数据（必须含id）
     * @return 影响行数
     */
    public int updateRule(DividendRule rule) {
        return dividendRuleMapper.updateRule(rule);
    }

    /**
     * 删除分红规则
     * @param id 规则ID
     * @return 影响行数
     */
    public int deleteRule(Long id) {
        return dividendRuleMapper.deleteRule(id);
    }
}
