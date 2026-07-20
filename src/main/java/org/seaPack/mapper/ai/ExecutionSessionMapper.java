package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.ExecutionSession;

import java.util.List;

@Mapper
public interface ExecutionSessionMapper {

    /**
     * 分页查询执行会话列表
     *
     * @param bizType   业务类型（可选）
     * @param bizId     业务ID（可选）
     * @param sessionId 会话ID（可选，用于查询多轮对话）
     * @param createdBy 操作人（可选）
     * @param status    状态（可选）
     * @return 会话列表
     */
    List<ExecutionSession> selectList(@Param("bizType") String bizType,
                                       @Param("bizId") Long bizId,
                                       @Param("sessionId") String sessionId,
                                       @Param("createdBy") Long createdBy,
                                       @Param("status") String status);

    /**
     * 根据ID查询会话详情
     */
    ExecutionSession selectById(@Param("id") Long id);

    /**
     * 根据requestId查询（用于幂等校验）
     */
    ExecutionSession selectByRequestId(@Param("requestId") String requestId);

    /**
     * 新增会话记录
     */
    int insert(ExecutionSession session);

    /**
     * 更新会话记录
     */
    int update(ExecutionSession session);

    /**
     * 逻辑删除
     */
    int logicalDelete(@Param("id") Long id);

    /**
     * 物理删除
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量逻辑删除
     */
    int batchLogicalDelete(@Param("ids") List<Long> ids);
}
