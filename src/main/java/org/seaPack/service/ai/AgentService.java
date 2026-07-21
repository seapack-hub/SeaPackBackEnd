package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.ai.*;
import org.seaPack.model.ai.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Agent 核心服务
 * <p>提供 Agent 的 CRUD、关联管理（提示词模板、技能、知识库）功能。</p>
 */
@Slf4j
@Service
public class AgentService {

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private AgentPromptMapper agentPromptMapper;

    @Autowired
    private AgentSkillMapper agentSkillMapper;

    @Autowired
    private AgentKnowledgeMapper agentKnowledgeMapper;

    // ===== Agent CRUD =====

    /** 分页查询 Agent 列表 */
    public PageInfo<Agent> getList(int pageNum, int pageSize, Integer status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<Agent> list = agentMapper.selectList(status, keyword);
        return new PageInfo<>(list);
    }

    /** 全量查询已启用的 Agent 列表（下拉选择用） */
    public List<Agent> getAll() {
        return agentMapper.selectList(1, null);
    }

    /** 根据 ID 查询 Agent 详情 */
    public Agent getById(Long id) {
        return agentMapper.selectById(id);
    }

    /** 校验 Agent 编码是否已存在（excludeId 用于更新时排除自身） */
    public boolean isCodeDuplicate(String code, Long excludeId) {
        return agentMapper.countByCode(code, excludeId) > 0;
    }

    /** 新增 Agent */
    @Transactional
    public int insert(Agent agent) {
        return agentMapper.insert(agent);
    }

    /** 更新 Agent */
    @Transactional
    public int update(Agent agent) {
        return agentMapper.update(agent);
    }

    /** 删除 Agent（级联删除关联关系） */
    @Transactional
    public int deleteById(Long id) {
        agentPromptMapper.deleteByAgentId(id);
        agentSkillMapper.deleteByAgentId(id);
        agentKnowledgeMapper.deleteByAgentId(id);
        return agentMapper.deleteById(id);
    }

    /** 复制 Agent（创建副本，含关联关系） */
    @Transactional
    public Agent copy(Long id) {
        Agent source = agentMapper.selectById(id);
        if (source == null) {
            throw new RuntimeException("Agent 不存在: " + id);
        }

        Agent copy = new Agent();
        copy.setName(source.getName() + "（副本）");
        copy.setCode(source.getCode() + "_copy");
        copy.setAvatar(source.getAvatar());
        copy.setDescription(source.getDescription());
        copy.setSystemPrompt(source.getSystemPrompt());
        copy.setGreeting(source.getGreeting());
        copy.setModelCode(source.getModelCode());
        copy.setTemperature(source.getTemperature());
        copy.setMaxTokens(source.getMaxTokens());
        copy.setOutputFormat(source.getOutputFormat());
        copy.setMemoryEnabled(source.getMemoryEnabled());
        copy.setMemoryWindow(source.getMemoryWindow());
        copy.setVersion(source.getVersion());
        copy.setStatus(source.getStatus());
        copy.setSortOrder(source.getSortOrder());
        copy.setCreatedBy(source.getCreatedBy());

        agentMapper.insert(copy);

        // 复制提示词模板关联
        List<AgentPrompt> prompts = agentPromptMapper.selectByAgentId(id);
        for (AgentPrompt p : prompts) {
            AgentPrompt cp = new AgentPrompt();
            cp.setAgentId(copy.getId());
            cp.setTemplateId(p.getTemplateId());
            cp.setIsPrimary(p.getIsPrimary());
            cp.setEnabled(p.getEnabled());
            cp.setSortOrder(p.getSortOrder());
            agentPromptMapper.insert(cp);
        }

        // 复制技能关联
        List<AgentSkill> skills = agentSkillMapper.selectByAgentId(id);
        for (AgentSkill s : skills) {
            AgentSkill cs = new AgentSkill();
            cs.setAgentId(copy.getId());
            cs.setSkillId(s.getSkillId());
            cs.setEnabled(s.getEnabled());
            cs.setIsPrimary(s.getIsPrimary());
            cs.setSortOrder(s.getSortOrder());
            agentSkillMapper.insert(cs);
        }

        // 复制知识库关联
        List<AgentKnowledge> knowledges = agentKnowledgeMapper.selectByAgentId(id);
        for (AgentKnowledge k : knowledges) {
            AgentKnowledge ck = new AgentKnowledge();
            ck.setAgentId(copy.getId());
            ck.setKnowledgeId(k.getKnowledgeId());
            ck.setEnabled(k.getEnabled());
            ck.setRetrievalCount(k.getRetrievalCount());
            ck.setSortOrder(k.getSortOrder());
            agentKnowledgeMapper.insert(ck);
        }

        return copy;
    }

    /** 增加使用次数 */
    @Transactional
    public int incrementUseCount(Long id) {
        return agentMapper.incrementUseCount(id);
    }

    /** 更新启停状态 */
    @Transactional
    public int updateStatus(Long id, Integer status) {
        Agent agent = new Agent();
        agent.setId(id);
        agent.setStatus(status);
        return agentMapper.update(agent);
    }

    // ===== 提示词模板关联管理 =====

    /** 获取 Agent 关联的提示词模板列表 */
    public List<AgentPrompt> getPrompts(Long agentId) {
        return agentPromptMapper.selectByAgentId(agentId);
    }

    /** 添加关联提示词模板 */
    @Transactional
    public int addPrompt(AgentPrompt agentPrompt) {
        if (agentPromptMapper.countByAgentIdAndTemplateId(agentPrompt.getAgentId(), agentPrompt.getTemplateId()) > 0) {
            throw new RuntimeException("该提示词模板已关联");
        }
        return agentPromptMapper.insert(agentPrompt);
    }

    /** 更新关联提示词模板 */
    @Transactional
    public int updatePrompt(AgentPrompt agentPrompt) {
        return agentPromptMapper.update(agentPrompt);
    }

    /** 删除关联提示词模板 */
    @Transactional
    public int deletePrompt(Long id) {
        return agentPromptMapper.deleteById(id);
    }

    // ===== 技能关联管理 =====

    /** 获取 Agent 关联的技能列表 */
    public List<AgentSkill> getSkills(Long agentId) {
        return agentSkillMapper.selectByAgentId(agentId);
    }

    /** 添加关联技能 */
    @Transactional
    public int addSkill(AgentSkill agentSkill) {
        if (agentSkillMapper.countByAgentIdAndSkillId(agentSkill.getAgentId(), agentSkill.getSkillId()) > 0) {
            throw new RuntimeException("该技能已关联");
        }
        return agentSkillMapper.insert(agentSkill);
    }

    /** 更新关联技能 */
    @Transactional
    public int updateSkill(AgentSkill agentSkill) {
        return agentSkillMapper.update(agentSkill);
    }

    /** 删除关联技能 */
    @Transactional
    public int deleteSkill(Long id) {
        return agentSkillMapper.deleteById(id);
    }

    // ===== 知识库关联管理 =====

    /** 获取 Agent 关联的知识库列表 */
    public List<AgentKnowledge> getKnowledgeList(Long agentId) {
        return agentKnowledgeMapper.selectByAgentId(agentId);
    }

    /** 添加关联知识库 */
    @Transactional
    public int addKnowledge(AgentKnowledge agentKnowledge) {
        if (agentKnowledgeMapper.countByAgentIdAndKnowledgeId(agentKnowledge.getAgentId(), agentKnowledge.getKnowledgeId()) > 0) {
            throw new RuntimeException("该知识库已关联");
        }
        return agentKnowledgeMapper.insert(agentKnowledge);
    }

    /** 更新关联知识库 */
    @Transactional
    public int updateKnowledge(AgentKnowledge agentKnowledge) {
        return agentKnowledgeMapper.update(agentKnowledge);
    }

    /** 删除关联知识库 */
    @Transactional
    public int deleteKnowledge(Long id) {
        return agentKnowledgeMapper.deleteById(id);
    }
}
