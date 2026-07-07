package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.Agent;

import java.util.List;

@Mapper
public interface AgentMapper {

    List<Agent> selectList(@Param("status") Integer status, @Param("keyword") String keyword);

    Agent selectById(@Param("id") Long id);

    Agent selectByCode(@Param("code") String code);

    int countByCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    int insert(Agent agent);

    int update(Agent agent);

    int incrementUseCount(@Param("id") Long id);

    int deleteById(@Param("id") Long id);
}
