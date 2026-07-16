package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.SkillExecutionLog;

import java.util.List;

@Mapper
public interface SkillExecutionLogMapper {

    List<SkillExecutionLog> selectList(@Param("skillId") Long skillId,
                                        @Param("skillCode") String skillCode,
                                        @Param("modelName") String modelName,
                                        @Param("moduleKey") String moduleKey,
                                        @Param("sceneId") Long sceneId,
                                        @Param("agentId") Long agentId,
                                        @Param("status") String status,
                                        @Param("createdBy") Long createdBy);

    SkillExecutionLog selectById(@Param("id") Long id);

    int insert(SkillExecutionLog log);
}
