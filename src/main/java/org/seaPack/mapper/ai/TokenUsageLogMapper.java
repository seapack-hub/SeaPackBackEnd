package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.TokenUsageLog;

import java.util.List;

@Mapper
public interface TokenUsageLogMapper {

    /**
     * 分页查询调用明细（支持多条件筛选）
     */
    List<TokenUsageLog> selectList(@Param("startDate") String startDate,
                                   @Param("endDate") String endDate,
                                   @Param("modelName") String modelName,
                                   @Param("moduleKey") String moduleKey,
                                   @Param("status") String status);

    /**
     * 插入调用明细
     */
    int insert(TokenUsageLog log);
}
