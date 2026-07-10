package org.seaPack.mapper.workflow;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.workflow.FeedbackRecord;

import java.util.List;

@Mapper
public interface FeedbackRecordMapper {

    /** 分页查询反馈记录（支持反馈类型、技能ID筛选） */
    List<FeedbackRecord> selectList(@Param("feedbackType") String feedbackType,
                                     @Param("skillId") Long skillId);

    /** 根据 ID 查询反馈详情 */
    FeedbackRecord selectById(@Param("id") Long id);

    /** 新增反馈 */
    int insert(FeedbackRecord record);

    /** 删除反馈 */
    int deleteById(@Param("id") Long id);
}
