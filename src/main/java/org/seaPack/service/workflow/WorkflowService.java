package org.seaPack.service.workflow;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.workflow.WorkflowDefinitionMapper;
import org.seaPack.mapper.workflow.WorkflowInstanceMapper;
import org.seaPack.mapper.workflow.WorkflowNodeLogMapper;
import org.seaPack.mapper.workflow.WorkflowVersionMapper;
import org.seaPack.model.workflow.WorkflowDefinition;
import org.seaPack.model.workflow.WorkflowInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 工作流核心服务
 * <p>提供工作流定义 CRUD、画布数据保存/读取、启停切换等功能。</p>
 */
@Service
public class WorkflowService {

    @Autowired
    private WorkflowDefinitionMapper definitionMapper;

    @Autowired
    private WorkflowVersionMapper versionMapper;

    @Autowired
    private WorkflowInstanceMapper instanceMapper;

    @Autowired
    private WorkflowNodeLogMapper nodeLogMapper;

    // ===== 工作流定义 CRUD =====

    /** 分页查询工作流列表 */
    public PageInfo<WorkflowDefinition> getList(int pageNum, int pageSize, Long categoryId, Integer status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<WorkflowDefinition> list = definitionMapper.selectList(categoryId, status, keyword);
        return new PageInfo<>(list);
    }

    /** 全量查询工作流列表 */
    public List<WorkflowDefinition> getAll(Long categoryId, Integer status) {
        return definitionMapper.selectList(categoryId, status, null);
    }

    /** 根据 ID 查询工作流详情 */
    public WorkflowDefinition getById(Long id) {
        return definitionMapper.selectById(id);
    }

    /** 校验编码是否已存在 */
    public boolean isCodeDuplicate(String code, Long excludeId) {
        return definitionMapper.countByCode(code, excludeId) > 0;
    }

    /** 新增工作流 */
    @Transactional
    public int insert(WorkflowDefinition definition) {
        return definitionMapper.insert(definition);
    }

    /** 更新工作流 */
    @Transactional
    public int update(WorkflowDefinition definition) {
        return definitionMapper.update(definition);
    }

    /** 删除工作流（级联删除版本和实例） */
    @Transactional
    public int deleteById(Long id) {
        // 删除节点日志（通过实例）
        List<WorkflowInstance> instances = instanceMapper.selectList(id, null, null);
        for (WorkflowInstance inst : instances) {
            nodeLogMapper.deleteByInstanceId(inst.getId());
        }
        instanceMapper.deleteByInstanceId(id);
        versionMapper.selectByWorkflowId(id).forEach(v -> versionMapper.deleteById(v.getId()));
        return definitionMapper.deleteById(id);
    }

    /** 复制工作流 */
    @Transactional
    public WorkflowDefinition copy(Long id) {
        WorkflowDefinition source = definitionMapper.selectById(id);
        if (source == null) {
            throw new RuntimeException("工作流不存在: " + id);
        }

        WorkflowDefinition copy = new WorkflowDefinition();
        copy.setName(source.getName() + "（副本）");
        copy.setCode(source.getCode() + "_copy");
        copy.setDescription(source.getDescription());
        copy.setCategoryId(source.getCategoryId());
        copy.setVersion(1);
        copy.setStatus(source.getStatus());
        copy.setNodes(source.getNodes());
        copy.setEdges(source.getEdges());
        copy.setNodeConfigs(source.getNodeConfigs());
        copy.setEdgeConfigs(source.getEdgeConfigs());
        copy.setVariables(source.getVariables());
        copy.setViewport(source.getViewport());
        copy.setCreatedBy(source.getCreatedBy());

        definitionMapper.insert(copy);
        return copy;
    }

    /** 更新启停状态 */
    @Transactional
    public int updateStatus(Long id, Integer status) {
        WorkflowDefinition def = new WorkflowDefinition();
        def.setId(id);
        def.setStatus(status);
        return definitionMapper.update(def);
    }

    // ===== 画布定义保存/读取 =====

    /** 保存工作流定义（含画布数据） */
    @Transactional
    public int saveDefinition(Long id, String nodes, String edges, String nodeConfigs,
                               String edgeConfigs, String variables, String viewport) {
        return definitionMapper.updateDefinition(id, nodes, edges, nodeConfigs, edgeConfigs, variables, viewport);
    }

    /** 获取工作流定义（含画布数据） */
    public WorkflowDefinition getDefinition(Long id) {
        return definitionMapper.selectById(id);
    }
}
