package org.seaPack.mapper.finance;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.finance.DividendRule;

import java.util.List;

@Mapper
public interface DividendRuleMapper {

    /**
     * 查询指定股票的所有分红规则
     * @param stockId 股票ID
     * @return 分红规则列表
     */
    List<DividendRule> selectRulesByStockId(@Param("stockId") Long stockId);

    /**
     * 查询所有已启用的分红规则
     * @return 启用规则列表
     */
    List<DividendRule> selectActiveRules();

    /**
     * 新增分红规则
     * @param rule 规则信息
     * @return 影响行数
     */
    int insertRule(DividendRule rule);

    /**
     * 更新分红规则
     * @param rule 待更新数据
     * @return 影响行数
     */
    int updateRule(DividendRule rule);

    /**
     * 删除分红规则
     * @param id 规则ID
     * @return 影响行数
     */
    int deleteRule(@Param("id") Long id);
}