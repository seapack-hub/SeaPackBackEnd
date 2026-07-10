package org.seaPack.service.workflow;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.workflow.AnnotationRecordMapper;
import org.seaPack.model.workflow.AnnotationRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标注记录管理服务
 * <p>提供标注记录的分页查询、新增等功能。</p>
 */
@Service
public class AnnotationRecordService {

    @Autowired
    private AnnotationRecordMapper annotationRecordMapper;

    /** 分页查询标注记录 */
    public PageInfo<AnnotationRecord> getPageList(int pageNum, int pageSize, Long taskId,
                                                   Long instanceId, String contentType) {
        PageHelper.startPage(pageNum, pageSize);
        List<AnnotationRecord> list = annotationRecordMapper.selectList(taskId, instanceId, contentType);
        return new PageInfo<>(list);
    }

    /** 根据 ID 查询标注详情 */
    public AnnotationRecord getById(Long id) {
        return annotationRecordMapper.selectById(id);
    }

    /** 新增标注 */
    @Transactional
    public int insert(AnnotationRecord record) {
        return annotationRecordMapper.insert(record);
    }

    /** 删除标注 */
    @Transactional
    public int deleteById(Long id) {
        return annotationRecordMapper.deleteById(id);
    }
}
