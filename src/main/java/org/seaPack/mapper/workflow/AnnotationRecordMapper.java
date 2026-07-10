package org.seaPack.mapper.workflow;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.workflow.AnnotationRecord;

import java.util.List;

@Mapper
public interface AnnotationRecordMapper {

    /** 分页查询标注记录（支持任务ID、实例ID、标注类型筛选） */
    List<AnnotationRecord> selectList(@Param("taskId") Long taskId,
                                       @Param("instanceId") Long instanceId,
                                       @Param("contentType") String contentType);

    /** 根据 ID 查询标注详情 */
    AnnotationRecord selectById(@Param("id") Long id);

    /** 新增标注 */
    int insert(AnnotationRecord record);

    /** 删除标注 */
    int deleteById(@Param("id") Long id);
}
