package org.seaPack.service.workflow;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.workflow.FeedbackRecordMapper;
import org.seaPack.model.workflow.FeedbackRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 反馈记录管理服务
 * <p>提供反馈记录的分页查询、新增等功能。</p>
 */
@Service
public class FeedbackRecordService {

    @Autowired
    private FeedbackRecordMapper feedbackRecordMapper;

    /** 分页查询反馈记录 */
    public PageInfo<FeedbackRecord> getPageList(int pageNum, int pageSize, String feedbackType, Long skillId) {
        PageHelper.startPage(pageNum, pageSize);
        List<FeedbackRecord> list = feedbackRecordMapper.selectList(feedbackType, skillId);
        return new PageInfo<>(list);
    }

    /** 根据 ID 查询反馈详情 */
    public FeedbackRecord getById(Long id) {
        return feedbackRecordMapper.selectById(id);
    }

    /** 新增反馈 */
    @Transactional
    public int insert(FeedbackRecord record) {
        return feedbackRecordMapper.insert(record);
    }

    /** 删除反馈 */
    @Transactional
    public int deleteById(Long id) {
        return feedbackRecordMapper.deleteById(id);
    }
}
