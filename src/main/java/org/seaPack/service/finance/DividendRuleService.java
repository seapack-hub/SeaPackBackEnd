package org.seaPack.service.finance;

import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.seaPack.mapper.finance.DividendRuleMapper; // 股息规则 Mapper
import org.seaPack.model.finance.DividendRule; // 股息规则实体
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.stereotype.Service; // Spring 服务注解
import org.springframework.transaction.annotation.Transactional; // 事务管理

import java.util.List; // List 集合

/**
 * 股息规则服务
 * 提供股息规则的查询、新增、修改和删除功能。
 */
@Slf4j // Lombok 日志
@Service // 标识为 Spring 服务 Bean
@Transactional(readOnly = false) // 启用写入事务
public class DividendRuleService {

    @Autowired // 注入股息规则 Mapper
    private DividendRuleMapper dividendRuleMapper;

    /**
     * 根据股票 ID 查询股息规则
     * @param stockId 股票 ID
     * @return 股息规则列表
     */
    public List<DividendRule> getRulesByStockId(Long stockId) {
        return dividendRuleMapper.selectRulesByStockId(stockId); // 按股票 ID 查询
    }

    /**
     * 查询所有启用的股息规则
     * @return 激活的规则列表
     */
    public List<DividendRule> getActiveRules() {
        return dividendRuleMapper.selectActiveRules(); // 查询启用状态的规则
    }

    /**
     * 新增股息规则
     * @param rule 股息规则实体
     * @return 影响行数
     */
    public int insertRule(DividendRule rule) {
        return dividendRuleMapper.insertRule(rule); // 调用 Mapper 插入
    }

    /**
     * 修改股息规则
     * @param rule 股息规则实体（需含 id）
     * @return 影响行数
     */
    public int updateRule(DividendRule rule) {
        return dividendRuleMapper.updateRule(rule); // 调用 Mapper 更新
    }

    /**
     * 删除股息规则
     * @param id 规则 ID
     * @return 影响行数
     */
    public int deleteRule(Long id) {
        return dividendRuleMapper.deleteRule(id); // 调用 Mapper 删除
    }
}