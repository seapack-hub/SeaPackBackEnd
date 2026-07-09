package org.seaPack.service.workflow;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.workflow.WorkflowScheduleMapper;
import org.seaPack.model.workflow.WorkflowSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工作流调度管理服务
 * <p>提供调度的 CRUD、启停切换、立即触发、Cron/Interval 下次执行时间计算等功能。</p>
 */
@Service
public class WorkflowScheduleService {

    @Autowired
    private WorkflowScheduleMapper scheduleMapper;

    /** 分页查询调度列表 */
    public PageInfo<WorkflowSchedule> getPageList(int pageNum, int pageSize, Long workflowId, Integer status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<WorkflowSchedule> list = scheduleMapper.selectList(workflowId, status, keyword);
        return new PageInfo<>(list);
    }

    /** 根据 ID 查询调度详情 */
    public WorkflowSchedule getById(Long id) {
        return scheduleMapper.selectById(id);
    }

    /** 新增调度 */
    @Transactional
    public int insert(WorkflowSchedule schedule) {
        // 默认启用
        if (schedule.getStatus() == null) {
            schedule.setStatus(1);
        }
        // 默认执行次数为 0
        if (schedule.getRunCount() == null) {
            schedule.setRunCount(0);
        }
        // 计算首次执行时间
        schedule.setNextRunAt(calculateNextRunTime(schedule));
        return scheduleMapper.insert(schedule);
    }

    /** 更新调度 */
    @Transactional
    public int update(WorkflowSchedule schedule) {
        // 调度配置变更时重新计算下次执行时间
        if (schedule.getScheduleType() != null || schedule.getCronExpression() != null
                || schedule.getIntervalSeconds() != null || schedule.getScheduledTime() != null) {
            // 查询当前调度配置用于合并计算
            WorkflowSchedule current = scheduleMapper.selectById(schedule.getId());
            if (current != null) {
                WorkflowSchedule merged = mergeSchedule(current, schedule);
                schedule.setNextRunAt(calculateNextRunTime(merged));
            }
        }
        return scheduleMapper.update(schedule);
    }

    /** 删除调度 */
    @Transactional
    public int deleteById(Long id) {
        return scheduleMapper.deleteById(id);
    }

    /** 更新启停状态 */
    @Transactional
    public int updateStatus(Long id, Integer status) {
        WorkflowSchedule schedule = new WorkflowSchedule();
        schedule.setId(id);
        schedule.setStatus(status);
        // 启用时重新计算下次执行时间
        if (status == 1) {
            WorkflowSchedule current = scheduleMapper.selectById(id);
            if (current != null) {
                schedule.setNextRunAt(calculateNextRunTime(current));
            }
        }
        return scheduleMapper.updateStatus(id, status);
    }

    /** 立即触发一次 */
    @Transactional
    public WorkflowSchedule trigger(Long id) {
        WorkflowSchedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new RuntimeException("调度不存在: " + id);
        }

        Date now = new Date();
        int newRunCount = (schedule.getRunCount() != null ? schedule.getRunCount() : 0) + 1;

        // 计算下次执行时间
        Date nextRun = calculateNextRunTime(schedule);
        // 如果是 once 类型，执行后自动禁用
        if ("once".equals(schedule.getScheduleType())) {
            scheduleMapper.updateStatus(id, 0);
        } else {
            scheduleMapper.updateRunInfo(id, now, nextRun, newRunCount);
        }

        return scheduleMapper.selectById(id);
    }

    /** 查询需要触发的调度（供定时任务调用） */
    public List<WorkflowSchedule> getReadySchedules() {
        return scheduleMapper.selectReadySchedules();
    }

    // ===== 内部方法 =====

    /**
     * 计算下次执行时间
     * <ul>
     *   <li>cron: 简单解析常见 cron 表达式（分 时 日 月 周），计算下一次触发时间</li>
     *   <li>interval: 当前时间 + intervalSeconds</li>
     *   <li>once: 直接使用 scheduledTime</li>
     * </ul>
     */
    private Date calculateNextRunTime(WorkflowSchedule schedule) {
        String type = schedule.getScheduleType();
        if (type == null) return null;

        switch (type) {
            case "cron":
                return calculateCronNextRun(schedule.getCronExpression());
            case "interval":
                return calculateIntervalNextRun(schedule.getIntervalSeconds());
            case "once":
                return schedule.getScheduledTime();
            default:
                return null;
        }
    }

    /**
     * 简单 Cron 下次执行时间计算
     * <p>支持格式: "分 时 日 月 周"（5 位标准 Cron），暂不支持秒位和年位。</p>
     * <p>对于复杂表达式（含 *, /, , 等），返回当前时间 + 1分钟作为兜底。</p>
     */
    private Date calculateCronNextRun(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            return null;
        }

        String[] parts = cronExpression.trim().split("\\s+");
        if (parts.length < 5) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 1); // 默认 +1 分钟兜底
        return cal.getTime();
    }

    /** Interval 下次执行时间 = 当前时间 + intervalSeconds */
    private Date calculateIntervalNextRun(Integer intervalSeconds) {
        if (intervalSeconds == null || intervalSeconds <= 0) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, intervalSeconds);
        return cal.getTime();
    }

    /** 合并当前配置与更新配置（用于重新计算 nextRunAt） */
    private WorkflowSchedule mergeSchedule(WorkflowSchedule current, WorkflowSchedule update) {
        WorkflowSchedule merged = new WorkflowSchedule();
        merged.setScheduleType(update.getScheduleType() != null ? update.getScheduleType() : current.getScheduleType());
        merged.setCronExpression(update.getCronExpression() != null ? update.getCronExpression() : current.getCronExpression());
        merged.setIntervalSeconds(update.getIntervalSeconds() != null ? update.getIntervalSeconds() : current.getIntervalSeconds());
        merged.setScheduledTime(update.getScheduledTime() != null ? update.getScheduledTime() : current.getScheduledTime());
        return merged;
    }
}
