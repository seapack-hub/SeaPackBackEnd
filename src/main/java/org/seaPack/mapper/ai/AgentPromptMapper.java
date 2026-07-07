package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.AgentPrompt;

import java.util.List;

@Mapper
public interface AgentPromptMapper {

    List<AgentPrompt> selectByAgentId(@Param("agentId") Long agentId);

    AgentPrompt selectById(@Param("id") Long id);

    int countByAgentIdAndTemplateId(@Param("agentId") Long agentId, @Param("templateId") Long templateId);

    int insert(AgentPrompt agentPrompt);

    int update(AgentPrompt agentPrompt);

    int deleteById(@Param("id") Long id);

    int deleteByAgentId(@Param("agentId") Long agentId);
}
