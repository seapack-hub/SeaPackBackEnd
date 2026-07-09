package org.seaPack.service.workflow;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.workflow.WorkflowDefinitionMapper;
import org.seaPack.mapper.workflow.WorkflowInstanceMapper;
import org.seaPack.mapper.workflow.WorkflowNodeLogMapper;
import org.seaPack.model.workflow.WorkflowDefinition;
import org.seaPack.model.workflow.WorkflowInstance;
import org.seaPack.model.workflow.WorkflowNodeLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 工作流执行实例服务
 * <p>提供工作流执行/调试、实例列表/详情查询、节点日志查询、实例暂停/恢复/取消等功能。</p>
 */
@Service
public class WorkflowInstanceService {

    @Autowired
    private WorkflowDefinitionMapper definitionMapper;

    @Autowired
    private WorkflowInstanceMapper instanceMapper;

    @Autowired
    private WorkflowNodeLogMapper nodeLogMapper;

    /** 执行/调试工作流（创建实例） */
    @Transactional
    public WorkflowInstance runWorkflow(Long workflowId, String inputParams, String triggerType, Long userId) {
        WorkflowDefinition def = definitionMapper.selectById(workflowId);
        if (def == null) {
            throw new RuntimeException("工作流不存在: " + workflowId);
        }

        WorkflowInstance instance = new WorkflowInstance();
        instance.setWorkflowId(workflowId);
        instance.setWorkflowVersion(def.getVersion());
        instance.setWorkflowName(def.getName());
        instance.setDefinitionSnapshot(def.getNodes());
        instance.setStatus(0); // 待执行
        instance.setTriggerType(triggerType);
        instance.setInputParams(inputParams);
        instance.setCompletedNodes("[]");
        instance.setTotalNodes(0);
        instance.setCompletedCount(0);
        instance.setCreatedBy(userId);

        instanceMapper.insert(instance);
        return instance;
    }

    /** 分页查询实例列表 */
    public PageInfo<WorkflowInstance> getInstanceList(int pageNum, int pageSize, Long workflowId, Integer status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<WorkflowInstance> list = instanceMapper.selectList(workflowId, status, keyword);
        return new PageInfo<>(list);
    }

    /** 查询实例详情 */
    public WorkflowInstance getInstanceById(Long id) {
        return instanceMapper.selectById(id);
    }

    /** 查询实例节点日志 */
    public List<WorkflowNodeLog> getNodeLogs(Long instanceId) {
        return nodeLogMapper.selectByInstanceId(instanceId);
    }

    /** 取消实例执行 */
    @Transactional
    public int cancelInstance(Long instanceId) {
        return instanceMapper.updateStatus(instanceId, 5);
    }

    /** 暂停实例执行 */
    @Transactional
    public int pauseInstance(Long instanceId) {
        return instanceMapper.updateStatus(instanceId, 4);
    }

    /** 恢复实例执行 */
    @Transactional
    public int resumeInstance(Long instanceId) {
        return instanceMapper.updateStatus(instanceId, 1);
    }
}
