package org.seaPack.service.ai;

import org.seaPack.mapper.ai.SceneOrchestrationMapper;
import org.seaPack.mapper.ai.SceneOrchestrationStepMapper;
import org.seaPack.model.ai.SceneOrchestration;
import org.seaPack.model.ai.SceneOrchestrationStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 场景编排服务
 * <p>提供编排策略和步骤的 CRUD、复制、排序等功能。</p>
 */
@Service
public class SceneOrchestrationService {

    @Autowired
    private SceneOrchestrationMapper orchestrationMapper;

    @Autowired
    private SceneOrchestrationStepMapper stepMapper;

    // ===== 编排 CRUD =====

    /**
     * 查询场景下的所有编排
     *
     * @param sceneId 场景ID
     * @return 编排列表
     */
    public List<SceneOrchestration> getList(Long sceneId) {
        return orchestrationMapper.selectBySceneId(sceneId);
    }

    /**
     * 查询编排详情（含步骤列表）
     *
     * @param id 编排ID
     * @return 编排实体，不存在返回 null
     */
    public SceneOrchestration getById(Long id) {
        return orchestrationMapper.selectById(id);
    }

    /**
     * 查询编排的所有步骤
     *
     * @param orchestrationId 编排ID
     * @return 步骤列表
     */
    public List<SceneOrchestrationStep> getSteps(Long orchestrationId) {
        return stepMapper.selectByOrchestrationId(orchestrationId);
    }

    /**
     * 查询编排详情（含步骤）
     *
     * @param id 编排ID
     * @return 返回编排及其步骤列表，不存在返回 null
     */
    public SceneOrchestration getDetailWithSteps(Long id) {
        SceneOrchestration orch = orchestrationMapper.selectById(id);
        if (orch == null) {
            return null;
        }
        orch.setSteps(stepMapper.selectByOrchestrationId(id));
        return orch;
    }

    /**
     * 校验编排编码是否重复
     *
     * @param sceneId   场景ID
     * @param code      编码
     * @param excludeId 排除的ID（更新时用）
     * @return true-存在 false-不存在
     */
    public boolean isCodeDuplicate(Long sceneId, String code, Long excludeId) {
        return orchestrationMapper.countBySceneAndCode(sceneId, code, excludeId) > 0;
    }

    /**
     * 新增编排
     *
     * @param orchestration 编排实体
     * @return 影响行数
     */
    @Transactional
    public int insert(SceneOrchestration orchestration) {
        return orchestrationMapper.insert(orchestration);
    }

    /**
     * 更新编排
     *
     * @param orchestration 编排实体（仅更新非空字段）
     * @return 影响行数
     */
    @Transactional
    public int update(SceneOrchestration orchestration) {
        return orchestrationMapper.update(orchestration);
    }

    /**
     * 删除编排（级联删除步骤）
     *
     * @param id 编排ID
     * @return 影响行数
     */
    @Transactional
    public int deleteById(Long id) {
        stepMapper.deleteByOrchestrationId(id);
        return orchestrationMapper.deleteById(id);
    }

    /**
     * 更新启停状态
     *
     * @param id     编排ID
     * @param status 状态（1-启用 0-禁用）
     * @return 影响行数
     */
    @Transactional
    public int updateStatus(Long id, Integer status) {
        SceneOrchestration orch = new SceneOrchestration();
        orch.setId(id);
        orch.setStatus(status);
        return orchestrationMapper.update(orch);
    }

    /**
     * 复制编排（含步骤）
     *
     * @param id 源编排ID
     * @return 复制后的编排
     * @throws RuntimeException 编排不存在
     */
    @Transactional
    public SceneOrchestration copy(Long id) {
        SceneOrchestration source = orchestrationMapper.selectById(id);
        if (source == null) {
            throw new RuntimeException("编排不存在: " + id);
        }

        SceneOrchestration copy = new SceneOrchestration();
        copy.setSceneId(source.getSceneId());
        copy.setName(source.getName() + "（副本）");
        copy.setCode(source.getCode() + "_copy");
        copy.setDescription(source.getDescription());
        copy.setStrategy(source.getStrategy());
        copy.setStatus(source.getStatus());
        copy.setSortOrder(source.getSortOrder());
        copy.setCreatedBy(source.getCreatedBy());
        orchestrationMapper.insert(copy);

        Long newId = copy.getId();
        List<SceneOrchestrationStep> steps = stepMapper.selectByOrchestrationId(id);
        for (SceneOrchestrationStep s : steps) {
            SceneOrchestrationStep cs = new SceneOrchestrationStep();
            cs.setOrchestrationId(newId);
            cs.setStepIndex(s.getStepIndex());
            cs.setStepName(s.getStepName());
            cs.setAgentId(s.getAgentId());
            cs.setInputMapping(s.getInputMapping());
            cs.setCondition(s.getCondition());
            cs.setRetryCount(s.getRetryCount());
            cs.setTimeoutMs(s.getTimeoutMs());
            cs.setStatus(s.getStatus());
            cs.setSortOrder(s.getSortOrder());
            stepMapper.insert(cs);
        }

        return copy;
    }

    // ===== 步骤管理 =====

    /**
     * 新增步骤（校验序号是否重复）
     *
     * @param step 步骤实体
     * @return 影响行数
     * @throws RuntimeException 步骤序号已存在
     */
    @Transactional
    public int addStep(SceneOrchestrationStep step) {
        if (stepMapper.countByOrchAndIndex(step.getOrchestrationId(), step.getStepIndex(), null) > 0) {
            throw new RuntimeException("步骤序号 " + step.getStepIndex() + " 已存在");
        }
        return stepMapper.insert(step);
    }

    /**
     * 更新步骤
     *
     * @param step 步骤实体（仅更新非空字段）
     * @return 影响行数
     * @throws RuntimeException 步骤序号冲突
     */
    @Transactional
    public int updateStep(SceneOrchestrationStep step) {
        if (step.getStepIndex() != null) {
            if (stepMapper.countByOrchAndIndex(step.getOrchestrationId(), step.getStepIndex(), step.getId()) > 0) {
                throw new RuntimeException("步骤序号 " + step.getStepIndex() + " 已存在");
            }
        }
        return stepMapper.update(step);
    }

    /**
     * 删除步骤
     *
     * @param id 步骤ID
     * @return 影响行数
     */
    @Transactional
    public int deleteStep(Long id) {
        return stepMapper.deleteById(id);
    }

    /**
     * 批量排序步骤
     * <p>根据传入的 stepIds 顺序，重新分配 step_index 为 1, 2, 3...</p>
     *
     * @param orchestrationId 编排ID
     * @param stepIds         按新排序排列的步骤ID列表
     */
    @Transactional
    public void sortSteps(Long orchestrationId, List<Long> stepIds) {
        if (stepIds == null || stepIds.isEmpty()) {
            return;
        }
        stepMapper.batchUpdateStepIndex(orchestrationId, stepIds);
    }
}
