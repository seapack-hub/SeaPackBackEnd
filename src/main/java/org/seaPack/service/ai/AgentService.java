package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.AgentChatRequest;
import org.seaPack.dto.ai.AgentChatResponse;
import org.seaPack.mapper.ai.*;
import org.seaPack.model.ai.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Agent/助手核心服务
 * <p>提供 Agent 的 CRUD、关联管理（提示词模板、技能、知识库）及对话执行功能。</p>
 */
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

    @Autowired
    private PromptTemplateMapper promptTemplateMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AIProperties aiProperties;

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

    // ===== 对话执行 =====

    /**
     * 执行 Agent 对话
     * <p>核心流程：加载 Agent → 组装系统提示词（Agent prompt + 已启用的模板内容）
     * → 构建消息列表（含可选历史记忆） → 调用 LLM → 增加使用次数。</p>
     *
     * @param request 对话请求（含 Agent ID、用户消息、可选历史）
     * @return 对话响应（含回复内容、Token 统计、耗时）
     */
    public AgentChatResponse chat(AgentChatRequest request) {
        // 1. 加载 Agent 并校验状态
        Agent agent = agentMapper.selectById(request.getAgentId());
        if (agent == null) {
            throw new RuntimeException("Agent 不存在: " + request.getAgentId());
        }
        if (agent.getStatus() == null || agent.getStatus() != 1) {
            throw new RuntimeException("Agent 已禁用: " + agent.getName());
        }

        // 2. 组装系统提示词：Agent 基础 system_prompt + 已启用的主/辅助模板内容
        StringBuilder systemPromptBuilder = new StringBuilder();
        if (agent.getSystemPrompt() != null && !agent.getSystemPrompt().isBlank()) {
            systemPromptBuilder.append(agent.getSystemPrompt());
        }

        // 拼接已启用的提示词模板内容
        List<AgentPrompt> enabledPrompts = agentPromptMapper.selectByAgentId(agent.getId()).stream()
                .filter(p -> p.getEnabled() != null && p.getEnabled() == 1)
                .sorted(Comparator.comparingInt(p -> p.getSortOrder() != null ? p.getSortOrder() : 0))
                .collect(Collectors.toList());

        for (AgentPrompt ap : enabledPrompts) {
            PromptTemplate template = promptTemplateMapper.selectById(ap.getTemplateId());
            if (template != null && template.getContent() != null && !template.getContent().isBlank()) {
                systemPromptBuilder.append("\n\n").append(template.getContent());
            }
        }

        String systemPrompt = systemPromptBuilder.toString();
        if (systemPrompt.isBlank()) {
            throw new RuntimeException("Agent 系统提示词为空: " + agent.getName());
        }

        // 3. 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统提示词
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        // 对话历史（记忆模式）
        if (agent.getMemoryEnabled() != null && agent.getMemoryEnabled() == 1
                && request.getHistory() != null && !request.getHistory().isEmpty()) {
            int window = agent.getMemoryWindow() != null ? agent.getMemoryWindow() : 20;
            List<Map<String, String>> history = request.getHistory();
            // 截取最近 N 轮
            if (history.size() > window * 2) {
                history = history.subList(history.size() - window * 2, history.size());
            }
            messages.addAll(history);
        }

        // 用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", request.getMessage());
        messages.add(userMsg);

        // 4. 获取 AI 提供商配置
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
        if (config == null) {
            throw new RuntimeException("AI 配置错误：未找到提供商 [" + providerName + "]");
        }

        // 5. 构建 LLM API 请求
        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", agent.getModelCode() != null ? agent.getModelCode() : config.getChatModel());
        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        if (agent.getTemperature() != null) {
            requestBody.put("temperature", agent.getTemperature());
        }
        if (agent.getMaxTokens() != null) {
            requestBody.put("max_tokens", agent.getMaxTokens());
        }

        // 6. 发送请求
        long startTime = System.currentTimeMillis();
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            org.springframework.http.HttpEntity<Map<String, Object>> entity =
                    new org.springframework.http.HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> apiResponse = restTemplate.postForObject(url, entity, Map.class);
            long durationMs = System.currentTimeMillis() - startTime;

            // 7. 解析响应
            String content = "";
            Integer promptTokens = 0;
            Integer completionTokens = 0;

            if (apiResponse != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    if (message != null && message.get("content") != null) {
                        content = message.get("content");
                    }
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> usage = (Map<String, Object>) apiResponse.get("usage");
                if (usage != null) {
                    promptTokens = usage.get("prompt_tokens") != null ? (Integer) usage.get("prompt_tokens") : 0;
                    completionTokens = usage.get("completion_tokens") != null ? (Integer) usage.get("completion_tokens") : 0;
                }
            }

            // 8. 增加使用次数
            agentMapper.incrementUseCount(agent.getId());

            // 9. 组装响应
            AgentChatResponse response = new AgentChatResponse();
            response.setContent(content);
            response.setTokensPrompt(promptTokens);
            response.setTokensCompletion(completionTokens);
            response.setDurationMs((int) durationMs);
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Agent 对话失败: " + e.getMessage(), e);
        }
    }
}
