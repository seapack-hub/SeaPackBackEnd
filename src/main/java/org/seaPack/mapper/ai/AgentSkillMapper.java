package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.AgentSkill;

import java.util.List;

@Mapper
public interface AgentSkillMapper {

    List<AgentSkill> selectByAgentId(@Param("agentId") Long agentId);

    AgentSkill selectById(@Param("id") Long id);

    int countByAgentIdAndSkillId(@Param("agentId") Long agentId, @Param("skillId") Long skillId);

    int insert(AgentSkill agentSkill);

    int update(AgentSkill agentSkill);

    int deleteById(@Param("id") Long id);

    int deleteByAgentId(@Param("agentId") Long agentId);
}
