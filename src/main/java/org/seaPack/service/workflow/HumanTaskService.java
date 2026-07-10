package org.seaPack.service.workflow;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.workflow.HumanTaskMapper;
import org.seaPack.model.workflow.HumanTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 人工任务管理服务
 * <p>提供人工任务的分页查询、处理（审批/驳回/转办）、待办查询等功能。</p>
 */
@Service
public class HumanTaskService {

    @Autowired
    private HumanTaskMapper humanTaskMapper;

    /** 分页查询任务列表 */
    public PageInfo<HumanTask> getPageList(int pageNum, int pageSize, String taskType,
                                            Integer status, Long instanceId, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<HumanTask> list = humanTaskMapper.selectList(taskType, status, instanceId, keyword);
        return new PageInfo<>(list);
    }

    /** 根据 ID 查询任务详情 */
    public HumanTask getById(Long id) {
        return humanTaskMapper.selectById(id);
    }

    /** 处理任务（审批/驳回/转办） */
    @Transactional
    public HumanTask handleTask(Long id, String action, String result, String comment, Long delegateTo) {
        HumanTask task = humanTaskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + id);
        }

        Date now = new Date();

        switch (action) {
            case "approve":
                task.setAction("approve");
                task.setResult(result);
                task.setComment(comment);
                task.setStatus(2); // 已通过
                task.setCompletedAt(now);
                break;

            case "reject":
                task.setAction("reject");
                task.setResult(result);
                task.setComment(comment);
                task.setStatus(3); // 已驳回
                task.setCompletedAt(now);
                break;

            case "return":
                task.setAction("return");
                task.setComment(comment);
                task.setStatus(0); // 回到待处理
                task.setStartedAt(null);
                break;

            case "delegate":
                if (delegateTo == null) {
                    throw new RuntimeException("转办人不能为空");
                }
                task.setAction("delegate");
                task.setDelegatedTo(delegateTo);
                task.setDelegatedAt(now);
                task.setStatus(6); // 已转办
                task.setComment(comment);
                break;

            default:
                throw new RuntimeException("不支持的操作: " + action);
        }

        humanTaskMapper.update(task);
        return humanTaskMapper.selectById(id);
    }

    /** 我的待办任务 */
    public PageInfo<HumanTask> getMyPending(int pageNum, int pageSize, Long userId) {
        PageHelper.startPage(pageNum, pageSize);
        List<HumanTask> list = humanTaskMapper.selectMyPending(userId, 0); // status=0 待处理
        return new PageInfo<>(list);
    }

    /** 新增任务 */
    @Transactional
    public int insert(HumanTask task) {
        if (task.getStatus() == null) {
            task.setStatus(0); // 默认待处理
        }
        return humanTaskMapper.insert(task);
    }

    /** 更新任务 */
    @Transactional
    public int update(HumanTask task) {
        return humanTaskMapper.update(task);
    }

    /** 删除任务 */
    @Transactional
    public int deleteById(Long id) {
        return humanTaskMapper.deleteById(id);
    }
}
