package org.seaPack.service.workflow;

import org.seaPack.mapper.workflow.WorkflowDefinitionMapper;
import org.seaPack.mapper.workflow.WorkflowVersionMapper;
import org.seaPack.model.workflow.WorkflowDefinition;
import org.seaPack.model.workflow.WorkflowVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 工作流版本管理服务
 * <p>提供版本快照创建、版本列表/详情查询、版本删除、版本对比、版本回滚等功能。</p>
 */
@Service
public class WorkflowVersionService {

    @Autowired
    private WorkflowDefinitionMapper definitionMapper;

    @Autowired
    private WorkflowVersionMapper versionMapper;

    /** 创建版本快照 */
    @Transactional
    public WorkflowVersion createVersion(Long workflowId, String changeLog, Long userId) {
        WorkflowDefinition def = definitionMapper.selectById(workflowId);
        if (def == null) {
            throw new RuntimeException("工作流不存在: " + workflowId);
        }

        // 获取当前版本号并递增
        int newVersion = (def.getVersion() != null ? def.getVersion() : 0) + 1;
        definitionMapper.incrementVersion(workflowId);

        // 创建版本快照
        WorkflowVersion version = new WorkflowVersion();
        version.setWorkflowId(workflowId);
        version.setVersion(newVersion);
        version.setNodes(def.getNodes());
        version.setEdges(def.getEdges());
        version.setNodeConfigs(def.getNodeConfigs());
        version.setEdgeConfigs(def.getEdgeConfigs());
        version.setVariables(def.getVariables());
        version.setViewport(def.getViewport());
        version.setChangeLog(changeLog);
        version.setCreatedBy(userId);

        versionMapper.insert(version);
        return version;
    }

    /** 获取版本列表（仅摘要字段） */
    public List<WorkflowVersion> getVersionList(Long workflowId) {
        return versionMapper.selectByWorkflowId(workflowId);
    }

    /** 获取版本详情（含画布数据） */
    public WorkflowVersion getVersionDetail(Long workflowId, Integer versionNum) {
        return versionMapper.selectByVersion(workflowId, versionNum);
    }

    /** 获取最新版本 */
    public WorkflowVersion getLatestVersion(Long workflowId) {
        return versionMapper.selectLatest(workflowId);
    }

    /** 删除指定版本 */
    @Transactional
    public int deleteVersion(Long workflowId, Integer versionNum) {
        WorkflowVersion version = versionMapper.selectByVersion(workflowId, versionNum);
        if (version == null) {
            throw new RuntimeException("版本不存在: v" + versionNum);
        }
        return versionMapper.deleteById(version.getId());
    }

    /** 回滚到指定版本 */
    @Transactional
    public int rollbackToVersion(Long workflowId, Integer versionNum) {
        WorkflowVersion version = versionMapper.selectByVersion(workflowId, versionNum);
        if (version == null) {
            throw new RuntimeException("版本不存在: " + versionNum);
        }

        // 用版本快照覆盖当前定义
        return definitionMapper.updateDefinition(workflowId,
                version.getNodes(), version.getEdges(),
                version.getNodeConfigs(), version.getEdgeConfigs(),
                version.getVariables(), version.getViewport());
    }

    /**
     * 版本对比
     * <p>返回两个版本的摘要信息，画布数据由前端做 diff 展示。</p>
     */
    public VersionCompareResult compareVersions(Long workflowId, Integer versionA, Integer versionB) {
        WorkflowVersion vA = versionMapper.selectByVersion(workflowId, versionA);
        WorkflowVersion vB = versionMapper.selectByVersion(workflowId, versionB);
        if (vA == null) {
            throw new RuntimeException("版本 A 不存在: v" + versionA);
        }
        if (vB == null) {
            throw new RuntimeException("版本 B 不存在: v" + versionB);
        }
        return new VersionCompareResult(vA, vB);
    }

    /** 版本对比结果 DTO */
    public static class VersionCompareResult {
        private WorkflowVersion versionA;
        private WorkflowVersion versionB;

        public VersionCompareResult(WorkflowVersion versionA, WorkflowVersion versionB) {
            this.versionA = versionA;
            this.versionB = versionB;
        }

        public WorkflowVersion getVersionA() { return versionA; }
        public void setVersionA(WorkflowVersion versionA) { this.versionA = versionA; }
        public WorkflowVersion getVersionB() { return versionB; }
        public void setVersionB(WorkflowVersion versionB) { this.versionB = versionB; }
    }
}
