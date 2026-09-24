package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.OrchestrationSession;

import java.util.List;

/**
 * 编排执行会话 Mapper
 */
@Mapper
public interface OrchestrationSessionMapper {

    /** 根据 ID 查询会话 */
    OrchestrationSession selectById(@Param("id") Long id);

    /** 根据会话ID查询 */
    OrchestrationSession selectByConversationId(@Param("conversationId") String conversationId);

    /** 查询场景的执行会话列表（分页，按创建时间倒序） */
    List<OrchestrationSession> selectBySceneId(@Param("sceneId") Long sceneId,
                                                @Param("offset") Integer offset,
                                                @Param("limit") Integer limit);

    /** 查询场景的执行会话总数 */
    int countBySceneId(@Param("sceneId") Long sceneId);

    /** 新增会话 */
    int insert(OrchestrationSession session);

    /** 更新会话（只更新非空字段） */
    int update(OrchestrationSession session);

    /** 删除会话 */
    int deleteById(@Param("id") Long id);

    /** 删除场景的所有会话 */
    int deleteBySceneId(@Param("sceneId") Long sceneId);
}
