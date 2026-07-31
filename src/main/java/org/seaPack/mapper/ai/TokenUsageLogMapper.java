package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.TokenUsageLog;

import java.util.List;

/**
 * Token 调用明细 Mapper
 */
@Mapper
public interface TokenUsageLogMapper {

    /**
     * 分页查询调用明细（支持多条件筛选）
     *
     * @param startDate 起始日期
     * @param endDate   结束日期
     * @param userId    用户ID（可选）
     * @param bizType   用途（可选）
     * @param modelName 模型编码（可选）
     * @param status    状态（可选）
     * @return 调用明细列表
     */
    List<TokenUsageLog> selectList(@Param("startDate") String startDate,
                                   @Param("endDate") String endDate,
                                   @Param("userId") Long userId,
                                   @Param("bizType") String bizType,
                                   @Param("modelName") String modelName,
                                   @Param("status") String status);

    /**
     * 插入调用明细
     */
    int insert(TokenUsageLog log);
}
