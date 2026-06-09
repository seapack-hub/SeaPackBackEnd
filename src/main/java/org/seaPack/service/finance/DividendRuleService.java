package org.seaPack.service.finance;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.finance.DividendRuleMapper;
import org.seaPack.model.finance.DividendRule;
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

    public List<DividendRule> getRulesByStockId(Long stockId) {
        return dividendRuleMapper.selectRulesByStockId(stockId);
    }

    public List<DividendRule> getActiveRules() {
        return dividendRuleMapper.selectActiveRules();
    }

    public int insertRule(DividendRule rule) {
        return dividendRuleMapper.insertRule(rule);
    }

    public int updateRule(DividendRule rule) {
        return dividendRuleMapper.updateRule(rule);
    }

    public int deleteRule(Long id) {
        return dividendRuleMapper.deleteRule(id);
    }
}