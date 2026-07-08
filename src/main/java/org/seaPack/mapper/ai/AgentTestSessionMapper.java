package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.AgentTestSession;

import java.util.List;

@Mapper
public interface AgentTestSessionMapper {

    List<AgentTestSession> selectByAgentId(@Param("agentId") Long agentId);

    AgentTestSession selectById(@Param("id") Long id);

    int insert(AgentTestSession session);

    int deleteById(@Param("id") Long id);

    int deleteByAgentId(@Param("agentId") Long agentId);
}
