package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.ai.SceneAgentMapper;
import org.seaPack.mapper.ai.SceneKnowledgeMapper;
import org.seaPack.mapper.ai.SceneMapper;
import org.seaPack.model.ai.Scene;
import org.seaPack.model.ai.SceneAgent;
import org.seaPack.model.ai.SceneKnowledge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI 场景核心服务
 * <p>提供场景的 CRUD、关联管理（Agent、知识库）及使用统计等功能。</p>
 */
@Service
public class SceneService {

    @Autowired
    private SceneMapper sceneMapper;

    @Autowired
    private SceneAgentMapper sceneAgentMapper;

    @Autowired
    private SceneKnowledgeMapper sceneKnowledgeMapper;

    // ===== 场景 CRUD =====

    /** 分页查询场景列表 */
    public PageInfo<Scene> getList(int pageNum, int pageSize, Integer status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<Scene> list = sceneMapper.selectList(status, keyword);
        return new PageInfo<>(list);
    }

    /** 全量查询已启用的场景列表（下拉选择用） */
    public List<Scene> getAll() {
        return sceneMapper.selectList(1, null);
    }

    /** 根据 ID 查询场景详情 */
    public Scene getById(Long id) {
        return sceneMapper.selectById(id);
    }

    /** 校验场景编码是否已存在（excludeId 用于更新时排除自身） */
    public boolean isCodeDuplicate(String code, Long excludeId) {
        return sceneMapper.countByCode(code, excludeId) > 0;
    }

    /** 新增场景 */
    @Transactional
    public int insert(Scene scene) {
        return sceneMapper.insert(scene);
    }

    /** 更新场景 */
    @Transactional
    public int update(Scene scene) {
        return sceneMapper.update(scene);
    }

    /** 删除场景（级联删除关联关系） */
    @Transactional
    public int deleteById(Long id) {
        sceneAgentMapper.deleteBySceneId(id);
        sceneKnowledgeMapper.deleteBySceneId(id);
        return sceneMapper.deleteById(id);
    }

    /** 复制场景（创建副本，含关联关系） */
    @Transactional
    public Scene copy(Long id) {
        Scene source = sceneMapper.selectById(id);
        if (source == null) {
            throw new RuntimeException("场景不存在: " + id);
        }

        Scene copy = new Scene();
        copy.setName(source.getName() + "（副本）");
        copy.setCode(source.getCode() + "_copy");
        copy.setIcon(source.getIcon());
        copy.setCoverColor(source.getCoverColor());
        copy.setDescription(source.getDescription());
        copy.setModuleKey(source.getModuleKey());
        copy.setIsPublic(source.getIsPublic());
        copy.setStatus(source.getStatus());
        copy.setSortOrder(source.getSortOrder());
        copy.setCreatedBy(source.getCreatedBy());

        sceneMapper.insert(copy);

        // 复制助手关联
        List<SceneAgent> agents = sceneAgentMapper.selectBySceneId(id);
        for (SceneAgent a : agents) {
            SceneAgent ca = new SceneAgent();
            ca.setSceneId(copy.getId());
            ca.setAgentId(a.getAgentId());
            ca.setIsDefault(a.getIsDefault());
            ca.setSortOrder(a.getSortOrder());
            sceneAgentMapper.insert(ca);
        }

        // 复制知识库关联
        List<SceneKnowledge> knowledges = sceneKnowledgeMapper.selectBySceneId(id);
        for (SceneKnowledge k : knowledges) {
            SceneKnowledge ck = new SceneKnowledge();
            ck.setSceneId(copy.getId());
            ck.setKnowledgeId(k.getKnowledgeId());
            ck.setEnabled(k.getEnabled());
            sceneKnowledgeMapper.insert(ck);
        }

        return copy;
    }

    /** 增加使用次数 */
    @Transactional
    public int incrementUseCount(Long id) {
        return sceneMapper.incrementUseCount(id);
    }

    /** 更新启停状态 */
    @Transactional
    public int updateStatus(Long id, Integer status) {
        Scene scene = new Scene();
        scene.setId(id);
        scene.setStatus(status);
        return sceneMapper.update(scene);
    }

    // ===== 关联管理：Agent =====

    /** 获取场景关联的 Agent 列表 */
    public List<SceneAgent> getAgents(Long sceneId) {
        return sceneAgentMapper.selectBySceneId(sceneId);
    }

    /** 添加关联 Agent */
    @Transactional
    public int addAgent(SceneAgent sceneAgent) {
        if (sceneAgentMapper.countBySceneIdAndAgentId(sceneAgent.getSceneId(), sceneAgent.getAgentId()) > 0) {
            throw new RuntimeException("该助手已关联");
        }
        return sceneAgentMapper.insert(sceneAgent);
    }

    /** 更新关联 Agent */
    @Transactional
    public int updateAgent(SceneAgent sceneAgent) {
        return sceneAgentMapper.update(sceneAgent);
    }

    /** 删除关联 Agent */
    @Transactional
    public int deleteAgent(Long id) {
        return sceneAgentMapper.deleteById(id);
    }

    // ===== 关联管理：知识库 =====

    /** 获取场景关联的知识库列表 */
    public List<SceneKnowledge> getKnowledgeList(Long sceneId) {
        return sceneKnowledgeMapper.selectBySceneId(sceneId);
    }

    /** 添加关联知识库 */
    @Transactional
    public int addKnowledge(SceneKnowledge sceneKnowledge) {
        if (sceneKnowledgeMapper.countBySceneIdAndKnowledgeId(sceneKnowledge.getSceneId(), sceneKnowledge.getKnowledgeId()) > 0) {
            throw new RuntimeException("该知识库已关联");
        }
        return sceneKnowledgeMapper.insert(sceneKnowledge);
    }

    /** 更新关联知识库 */
    @Transactional
    public int updateKnowledge(SceneKnowledge sceneKnowledge) {
        return sceneKnowledgeMapper.update(sceneKnowledge);
    }

    /** 删除关联知识库 */
    @Transactional
    public int deleteKnowledge(Long id) {
        return sceneKnowledgeMapper.deleteById(id);
    }
}
