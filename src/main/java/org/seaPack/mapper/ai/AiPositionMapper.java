package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.AiPosition;

import java.util.List;
import java.util.Map;

/**
 * AI 功能位置 Mapper
 */
@Mapper
public interface AiPositionMapper {

    /** 查询所有位置（可按状态筛选） */
    List<AiPosition> selectList(@Param("status") Integer status, @Param("keyword") String keyword);

    /** 根据 ID 查询位置 */
    AiPosition selectById(@Param("id") Long id);

    /** 校验唯一约束 */
    int countByUnique(@Param("moduleKey") String moduleKey, @Param("positionKey") String positionKey,
                      @Param("excludeId") Long excludeId);

    /** 新增位置 */
    int insert(AiPosition position);

    /** 更新位置（只更新非空字段） */
    int update(AiPosition position);

    /** 删除位置 */
    int deleteById(@Param("id") Long id);

    /** 获取已启用的模块列表（去重） */
    List<Map<String, Object>> selectModules();
}
