package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.TemplateVariable;

import java.util.List;

/**
 * 模板变量 Mapper
 */
@Mapper
public interface TemplateVariableMapper {

    List<TemplateVariable> selectByTemplateId(@Param("templateId") Long templateId);

    int deleteByTemplateId(@Param("templateId") Long templateId);

    int batchInsert(@Param("list") List<TemplateVariable> list);
}
