package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.AgentMessage;

import java.util.List;

/**
 * Agent 间消息 Mapper
 */
@Mapper
public interface AgentMessageMapper {

    /** 查询会话的所有消息（按创建时间正序） */
    List<AgentMessage> selectBySessionId(@Param("sessionId") Long sessionId);

    /** 查询会话某一轮的所有消息 */
    List<AgentMessage> selectBySessionAndRound(@Param("sessionId") Long sessionId,
                                               @Param("roundIndex") Integer roundIndex);

    /** 新增消息 */
    int insert(AgentMessage message);

    /** 批量新增消息 */
    int batchInsert(@Param("list") List<AgentMessage> list);

    /** 删除会话的所有消息 */
    int deleteBySessionId(@Param("sessionId") Long sessionId);
}
