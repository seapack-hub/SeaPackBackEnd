package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.OrchestrationState;

import java.util.List;

/**
 * 编排运行时共享状态 Mapper
 */
@Mapper
public interface OrchestrationStateMapper {

    /** 查询会话的所有状态键值对 */
    List<OrchestrationState> selectBySessionId(@Param("sessionId") Long sessionId);

    /** 根据 key 查询单个状态 */
    OrchestrationState selectBySessionAndKey(@Param("sessionId") Long sessionId,
                                             @Param("stateKey") String stateKey);

    /** 插入状态（不存在时） */
    int insert(OrchestrationState state);

    /** 更新状态（乐观锁：version 匹配才更新） */
    int updateWithVersion(OrchestrationState state);

    /** Upsert 状态：存在则更新值+版本，不存在则插入 */
    int upsert(OrchestrationState state);

    /** 删除会话的所有状态 */
    int deleteBySessionId(@Param("sessionId") Long sessionId);

    /** 删除会话的指定 key */
    int deleteBySessionAndKey(@Param("sessionId") Long sessionId,
                              @Param("stateKey") String stateKey);
}
