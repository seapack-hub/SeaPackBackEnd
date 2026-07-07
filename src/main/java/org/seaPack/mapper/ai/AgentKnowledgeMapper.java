package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.AgentKnowledge;

import java.util.List;

@Mapper
public interface AgentKnowledgeMapper {

    List<AgentKnowledge> selectByAgentId(@Param("agentId") Long agentId);

    AgentKnowledge selectById(@Param("id") Long id);

    int countByAgentIdAndKnowledgeId(@Param("agentId") Long agentId, @Param("knowledgeId") Long knowledgeId);

    int insert(AgentKnowledge agentKnowledge);

    int update(AgentKnowledge agentKnowledge);

    int deleteById(@Param("id") Long id);

    int deleteByAgentId(@Param("agentId") Long agentId);
}
