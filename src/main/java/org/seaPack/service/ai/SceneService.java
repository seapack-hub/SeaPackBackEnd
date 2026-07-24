package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.dto.ai.SceneBindingInfo;
import org.seaPack.mapper.ai.*;
import org.seaPack.model.ai.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 场景核心服务
 * <p>提供场景的 CRUD、关联管理（Agent、知识库）、部署管理、Agent 配置管理、全量绑定查询及复制等功能。</p>
 */
@Service
public class SceneService {

    @Autowired
    private SceneMapper sceneMapper;

    @Autowired
    private SceneAgentMapper sceneAgentMapper;

    @Autowired
    private SceneKnowledgeMapper sceneKnowledgeMapper;

    @Autowired
    private SceneDeploymentMapper sceneDeploymentMapper;

    @Autowired
    private SceneAgentConfigMapper sceneAgentConfigMapper;

    // ===== 场景 CRUD =====

    /**
     * 分页查询场景列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param status   状态筛选（可选）
     * @param keyword  关键字模糊搜索（可选）
     * @return 分页结果
     */
    public PageInfo<Scene> getList(int pageNum, int pageSize, Integer status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<Scene> list = sceneMapper.selectList(status, keyword);
        return new PageInfo<>(list);
    }

    /**
     * 全量查询已启用的场景列表（下拉选择用）
     *
     * @return 已启用的场景列表
     */
    public List<Scene> getAll() {
        return sceneMapper.selectList(1, null);
    }

    /**
     * 根据 ID 查询场景详情
     *
     * @param id 场景ID
     * @return 场景实体，不存在返回 null
     */
    public Scene getById(Long id) {
        return sceneMapper.selectById(id);
    }

    /**
     * 校验场景编码是否已存在
     *
     * @param code      场景编码
     * @param excludeId 排除的ID（更新时用于排除自身）
     * @return true-存在 false-不存在
     */
    public boolean isCodeDuplicate(String code, Long excludeId) {
        return sceneMapper.countByCode(code, excludeId) > 0;
    }

    /**
     * 新增场景
     *
     * @param scene 场景实体
     * @return 影响行数
     */
    @Transactional
    public int insert(Scene scene) {
        return sceneMapper.insert(scene);
    }

    /**
     * 更新场景
     *
     * @param scene 场景实体（仅更新非空字段）
     * @return 影响行数
     */
    @Transactional
    public int update(Scene scene) {
        return sceneMapper.update(scene);
    }

    /**
     * 删除场景（级联删除关联关系、部署、Agent 配置）
     *
     * @param id 场景ID
     * @return 影响行数
     */
    @Transactional
    public int deleteById(Long id) {
        sceneDeploymentMapper.deleteBySceneId(id);
        sceneAgentConfigMapper.deleteBySceneId(id);
        sceneAgentMapper.deleteBySceneId(id);
        sceneKnowledgeMapper.deleteBySceneId(id);
        return sceneMapper.deleteById(id);
    }

    /**
     * 复制场景
     * <p>创建场景副本（名称加"（副本）"后缀，编码加"_copy"后缀），
     * 同步复制关联的 Agent、知识库、部署配置和 Agent 运行配置。</p>
     *
     * @param id 源场景ID
     * @return 复制后的场景
     */
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
        copy.setIsPublic(source.getIsPublic());
        copy.setStatus(source.getStatus());
        copy.setSortOrder(source.getSortOrder());
        copy.setCreatedBy(source.getCreatedBy());

        sceneMapper.insert(copy);

        Long newId = copy.getId();

        // 复制助手关联
        List<SceneAgent> agents = sceneAgentMapper.selectBySceneId(id);
        for (SceneAgent a : agents) {
            SceneAgent ca = new SceneAgent();
            ca.setSceneId(newId);
            ca.setAgentId(a.getAgentId());
            ca.setIsDefault(a.getIsDefault());
            ca.setSortOrder(a.getSortOrder());
            sceneAgentMapper.insert(ca);
        }

        // 复制知识库关联
        List<SceneKnowledge> knowledges = sceneKnowledgeMapper.selectBySceneId(id);
        for (SceneKnowledge k : knowledges) {
            SceneKnowledge ck = new SceneKnowledge();
            ck.setSceneId(newId);
            ck.setKnowledgeId(k.getKnowledgeId());
            ck.setEnabled(k.getEnabled());
            sceneKnowledgeMapper.insert(ck);
        }

        // 复制部署配置
        List<SceneDeployment> deployments = sceneDeploymentMapper.selectBySceneId(id);
        for (SceneDeployment d : deployments) {
            SceneDeployment cd = new SceneDeployment();
            cd.setSceneId(newId);
            cd.setModuleKey(d.getModuleKey());
            cd.setPositionKey(d.getPositionKey());
            cd.setConfig(d.getConfig());
            cd.setIsDefault(d.getIsDefault());
            cd.setSortOrder(d.getSortOrder());
            cd.setStatus(d.getStatus());
            sceneDeploymentMapper.insert(cd);
        }

        // 复制场景级 Agent 运行配置
        List<SceneAgentConfig> agentConfigs = sceneAgentConfigMapper.selectBySceneId(id);
        for (SceneAgentConfig ac : agentConfigs) {
            SceneAgentConfig cac = new SceneAgentConfig();
            cac.setSceneId(newId);
            cac.setAgentId(ac.getAgentId());
            cac.setModel(ac.getModel());
            cac.setTemperature(ac.getTemperature());
            cac.setMaxTokens(ac.getMaxTokens());
            cac.setSystemPrompt(ac.getSystemPrompt());
            cac.setOutputFormat(ac.getOutputFormat());
            cac.setContextLimit(ac.getContextLimit());
            sceneAgentConfigMapper.insert(cac);
        }

        return copy;
    }

    /**
     * 增加使用次数
     *
     * @param id 场景ID
     * @return 影响行数
     */
    @Transactional
    public int incrementUseCount(Long id) {
        return sceneMapper.incrementUseCount(id);
    }

    /**
     * 更新启停状态
     *
     * @param id     场景ID
     * @param status 状态（1-启用 0-禁用）
     * @return 影响行数
     */
    @Transactional
    public int updateStatus(Long id, Integer status) {
        Scene scene = new Scene();
        scene.setId(id);
        scene.setStatus(status);
        return sceneMapper.update(scene);
    }

    // ===== 关联管理：Agent =====

    /**
     * 查询场景关联的 Agent 列表（含名称和编码）
     *
     * @param sceneId 场景ID
     * @return 关联的 Agent 列表
     */
    public List<SceneAgent> getAgents(Long sceneId) {
        return sceneAgentMapper.selectBySceneId(sceneId);
    }

    /**
     * 添加关联 Agent（校验是否重复）
     *
     * @param sceneAgent 关联关系
     * @return 影响行数
     * @throws RuntimeException 该助手已关联
     */
    @Transactional
    public int addAgent(SceneAgent sceneAgent) {
        if (sceneAgentMapper.countBySceneIdAndAgentId(sceneAgent.getSceneId(), sceneAgent.getAgentId()) > 0) {
            throw new RuntimeException("该助手已关联");
        }
        return sceneAgentMapper.insert(sceneAgent);
    }

    /**
     * 更新关联 Agent
     *
     * @param sceneAgent 要更新的关联关系（仅更新非空字段）
     * @return 影响行数
     */
    @Transactional
    public int updateAgent(SceneAgent sceneAgent) {
        return sceneAgentMapper.update(sceneAgent);
    }

    /**
     * 删除关联 Agent
     *
     * @param id 关联关系ID
     * @return 影响行数
     */
    @Transactional
    public int deleteAgent(Long id) {
        return sceneAgentMapper.deleteById(id);
    }

    // ===== 关联管理：知识库 =====

    /**
     * 查询场景关联的知识库列表（含名称）
     *
     * @param sceneId 场景ID
     * @return 关联的知识库列表
     */
    public List<SceneKnowledge> getKnowledgeList(Long sceneId) {
        return sceneKnowledgeMapper.selectBySceneId(sceneId);
    }

    /**
     * 添加关联知识库（校验是否重复）
     *
     * @param sceneKnowledge 关联关系
     * @return 影响行数
     * @throws RuntimeException 该知识库已关联
     */
    @Transactional
    public int addKnowledge(SceneKnowledge sceneKnowledge) {
        if (sceneKnowledgeMapper.countBySceneIdAndKnowledgeId(sceneKnowledge.getSceneId(), sceneKnowledge.getKnowledgeId()) > 0) {
            throw new RuntimeException("该知识库已关联");
        }
        return sceneKnowledgeMapper.insert(sceneKnowledge);
    }

    /**
     * 更新关联知识库
     *
     * @param sceneKnowledge 要更新的关联关系（仅更新非空字段）
     * @return 影响行数
     */
    @Transactional
    public int updateKnowledge(SceneKnowledge sceneKnowledge) {
        return sceneKnowledgeMapper.update(sceneKnowledge);
    }

    /**
     * 删除关联知识库
     *
     * @param id 关联关系ID
     * @return 影响行数
     */
    @Transactional
    public int deleteKnowledge(Long id) {
        return sceneKnowledgeMapper.deleteById(id);
    }

    // ===== 场景部署管理 =====

    /**
     * 查询场景的所有部署配置
     *
     * @param sceneId 场景ID
     * @return 部署配置列表（按排序和创建时间升序）
     */
    public List<SceneDeployment> getDeployments(Long sceneId) {
        return sceneDeploymentMapper.selectBySceneId(sceneId);
    }

    /**
     * 新增部署（校验唯一约束）
     *
     * @param deployment 部署配置
     * @return 影响行数
     * @throws RuntimeException 该场景在此位置已部署
     */
    @Transactional
    public int addDeployment(SceneDeployment deployment) {
        if (sceneDeploymentMapper.countByUnique(
                deployment.getSceneId(), deployment.getModuleKey(), deployment.getPositionKey()) > 0) {
            throw new RuntimeException("该场景在此位置已部署");
        }
        return sceneDeploymentMapper.insert(deployment);
    }

    /**
     * 更新部署属性
     *
     * @param deployment 要更新的部署（仅更新非空字段）
     * @return 影响行数
     */
    @Transactional
    public int updateDeployment(SceneDeployment deployment) {
        return sceneDeploymentMapper.update(deployment);
    }

    /**
     * 删除部署
     *
     * @param id 部署ID
     * @return 影响行数
     */
    @Transactional
    public int deleteDeployment(Long id) {
        return sceneDeploymentMapper.deleteById(id);
    }

    // ===== 场景级 Agent 配置管理 =====

    /**
     * 查询场景的所有 Agent 运行配置
     *
     * @param sceneId 场景ID
     * @return Agent 运行配置列表
     */
    public List<SceneAgentConfig> getAgentConfigs(Long sceneId) {
        return sceneAgentConfigMapper.selectBySceneId(sceneId);
    }

    /**
     * 新增场景级 Agent 配置（校验唯一约束）
     *
     * @param config Agent 运行配置
     * @return 影响行数
     * @throws RuntimeException 该场景的助手配置已存在
     */
    @Transactional
    public int addAgentConfig(SceneAgentConfig config) {
        if (sceneAgentConfigMapper.countBySceneAndAgent(config.getSceneId(), config.getAgentId()) > 0) {
            throw new RuntimeException("该场景的助手配置已存在");
        }
        return sceneAgentConfigMapper.insert(config);
    }

    /**
     * 更新场景级 Agent 配置
     *
     * @param config 要更新的配置（仅更新非空字段）
     * @return 影响行数
     */
    @Transactional
    public int updateAgentConfig(SceneAgentConfig config) {
        return sceneAgentConfigMapper.update(config);
    }

    /**
     * 删除场景级 Agent 配置
     *
     * @param id 配置ID
     * @return 影响行数
     */
    @Transactional
    public int deleteAgentConfig(Long id) {
        return sceneAgentConfigMapper.deleteById(id);
    }

    /**
     * 解析场景级 Agent 配置覆盖
     * <p>如果 sceneId 不为空，查询 ai_scene_agent_config 获取覆盖参数。
     * 覆盖优先级：ai_scene_agent_config > ai_agent 默认值。</p>
     *
     * @param agentId Agent ID
     * @param sceneId 场景 ID（可选，为空则返回 null）
     * @return 场景级配置实体，无配置则返回 null
     */
    public SceneAgentConfig resolveAgentConfig(Long agentId, Long sceneId) {
        if (sceneId == null) {
            return null;
        }
        return sceneAgentConfigMapper.selectBySceneAndAgent(sceneId, agentId);
    }

    // ===== 全量绑定查询 =====

    /**
     * 全量绑定查询
     * <p>JOIN 多表查询所有已启用的场景部署、默认 Agent、场景级配置和知识库绑定信息。
     * 遍历所有默认 Agent（is_default=1），关联查询其场景的部署配置、场景级 Agent 配置和知识库列表。</p>
     *
     * @return 全量绑定信息列表，每个元素包含场景信息、部署信息、默认 Agent 信息、场景级配置和知识库 ID 列表
     */
    @Transactional(readOnly = true)
    public List<SceneBindingInfo> getAllBindings() {
        List<SceneBindingInfo> result = new ArrayList<>();
        List<SceneAgent> defaultAgents = sceneAgentMapper.selectDefaultAgents();

        for (SceneAgent sa : defaultAgents) {
            // 获取该场景的所有部署
            List<SceneDeployment> sceneDeployments = sceneDeploymentMapper.selectBySceneId(sa.getSceneId());
            for (SceneDeployment d : sceneDeployments) {
                // 只返回已启用的部署
                if (d.getStatus() == null || d.getStatus() != 1) continue;

                // 场景必须已启用
                Scene scene = sceneMapper.selectById(sa.getSceneId());
                if (scene == null || scene.getStatus() == null || scene.getStatus() != 1) continue;

                // 查询场景级配置（可选）
                SceneAgentConfig config = sceneAgentConfigMapper.selectBySceneAndAgent(sa.getSceneId(), sa.getAgentId());

                // 查询场景关联的已启用的知识库
                List<SceneKnowledge> kList = sceneKnowledgeMapper.selectBySceneId(sa.getSceneId());
                List<Long> knowledgeIds = kList.stream()
                        .filter(k -> k.getEnabled() != null && k.getEnabled() == 1)
                        .map(SceneKnowledge::getKnowledgeId)
                        .collect(Collectors.toList());

                // 组装绑定信息
                SceneBindingInfo info = new SceneBindingInfo();
                info.setSceneId(sa.getSceneId());
                info.setSceneName(scene.getName());
                info.setSceneCode(scene.getCode());
                info.setModuleKey(d.getModuleKey());
                info.setPositionKey(d.getPositionKey());
                info.setAgentId(sa.getAgentId());
                info.setAgentName(sa.getAgentName());
                info.setAgentCode(sa.getAgentCode());
                info.setDeploymentConfig(d.getConfig());
                info.setIsDefault(d.getIsDefault());
                info.setStatus(d.getStatus());
                info.setKnowledgeIds(knowledgeIds);

                // 填充场景级配置覆盖
                if (config != null) {
                    info.setAgentModel(config.getModel());
                    info.setAgentTemperature(config.getTemperature());
                    info.setAgentMaxTokens(config.getMaxTokens());
                    info.setAgentSystemPrompt(config.getSystemPrompt());
                    info.setAgentOutputFormat(config.getOutputFormat());
                    info.setAgentContextLimit(config.getContextLimit());
                }

                result.add(info);
            }
        }

        return result;
    }
}
