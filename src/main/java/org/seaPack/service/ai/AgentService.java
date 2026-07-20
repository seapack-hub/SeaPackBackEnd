package org.seaPack.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.*;
import org.seaPack.mapper.ai.*;
import org.seaPack.model.ai.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    private SkillMapper skillMapper;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AIProperties aiProperties;

    @Autowired
    private ExecutionSessionMapper executionSessionMapper;

    @Value("${server.port:8080}")
    private int serverPort;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    // ===== 测试对话（含链路追踪） =====

    /**
     * 执行测试对话（含完整链路追踪）
     * <p>核心流程：加载 Agent → 提示词组装 → 知识库检索 → 技能调用 → LLM 调用 → 保存测试会话。</p>
     *
     * @param request 测试对话请求
     * @param userId  当前用户 ID
     * @return 测试对话响应（含链路追踪快照）
     */
    @Transactional
    public AgentTestChatResponse testChat(AgentTestChatRequest request, Long userId) {
        long totalStart = System.currentTimeMillis();
        List<AgentTraceStep> steps = new ArrayList<>();
        int stepIndex = 1;

        // 1. 加载 Agent 并校验状态
        Agent agent = agentMapper.selectById(request.getAgentId());
        if (agent == null) {
            throw new RuntimeException("Agent 不存在: " + request.getAgentId());
        }
        if (agent.getStatus() == null || agent.getStatus() != 1) {
            throw new RuntimeException("Agent 已禁用: " + agent.getName());
        }

        // ===== Step 1: 提示词组装 =====
        String systemPrompt;
        try {
            AgentTraceStepResult stepResult = assemblePrompt(agent, stepIndex);
            systemPrompt = stepResult.output;
            stepIndex = stepResult.nextStepIndex;
            steps.add(stepResult.step);
        } catch (Exception e) {
            steps.add(buildFailStep(stepIndex++, "prompt_assembly", "提示词组装", e.getMessage()));
            return buildErrorResponse(agent, request, steps, totalStart, userId, e);
        }

        // ===== Step 2: 知识库检索 =====
        String knowledgeContext = "";
        try {
            AgentTraceStepResult stepResult = retrieveKnowledge(agent, request.getMessage(), stepIndex);
            knowledgeContext = stepResult.output;
            stepIndex = stepResult.nextStepIndex;
            steps.add(stepResult.step);

            // 将检索结果注入系统提示词
            if (knowledgeContext != null && !knowledgeContext.isBlank()) {
                systemPrompt += "\n\n【参考知识】\n" + knowledgeContext;
            }
        } catch (Exception e) {
            steps.add(buildFailStep(stepIndex++, "knowledge_retrieval", "知识库检索", e.getMessage()));
            // 知识库检索失败不中断流程，继续执行
        }

        // ===== Step 3: 技能调用 =====
        String skillContext = "";
        try {
            AgentTraceStepResult stepResult = executeSkill(agent, request.getMessage(), stepIndex);
            skillContext = stepResult.output;
            stepIndex = stepResult.nextStepIndex;
            steps.add(stepResult.step);

            // 将技能执行结果注入系统提示词
            if (skillContext != null && !skillContext.isBlank()) {
                systemPrompt += "\n\n【技能执行结果】\n" + skillContext;
            }
        } catch (Exception e) {
            steps.add(buildFailStep(stepIndex++, "skill_execution", "技能调用", e.getMessage()));
            // 技能调用失败不中断流程，继续执行
        }

        // ===== Step 4: LLM 调用 =====
        String replyContent;
        int promptTokens;
        int completionTokens;
        String modelName;
        try {
            AgentTraceStepResult stepResult = callLLM(agent, systemPrompt, request, stepIndex);
            replyContent = stepResult.output;
            promptTokens = stepResult.tokensPrompt;
            completionTokens = stepResult.tokensCompletion;
            modelName = stepResult.modelName;
            stepIndex = stepResult.nextStepIndex;
            steps.add(stepResult.step);
        } catch (Exception e) {
            steps.add(buildFailStep(stepIndex++, "llm_call", "LLM 调用", e.getMessage()));
            return buildErrorResponse(agent, request, steps, totalStart, userId, e);
        }

        // ===== 组装链路追踪快照 =====
        long totalDuration = System.currentTimeMillis() - totalStart;
        AgentTraceSnapshot snapshot = buildTraceSnapshot(steps, totalDuration, promptTokens, completionTokens);

        // ===== 保存测试会话 =====
        saveTestSession(agent, request, replyContent, snapshot, (int) totalDuration,
                promptTokens, completionTokens, modelName, "success", null, userId);

        // 增加使用次数
        agentMapper.incrementUseCount(agent.getId());

        // 组装响应
        AgentTestChatResponse response = new AgentTestChatResponse();
        response.setContent(replyContent);
        response.setTokensPrompt(promptTokens);
        response.setTokensCompletion(completionTokens);
        response.setDurationMs((int) totalDuration);
        response.setTraceSnapshot(snapshot);
        return response;
    }

    // ===== Step 1: 提示词组装 =====

    /**
     * 组装系统提示词
     * <p>将 Agent 基础提示词与关联的提示词模板按顺序拼接。</p>
     */
    private AgentTraceStepResult assemblePrompt(Agent agent, int stepIndex) {
        long stepStart = System.currentTimeMillis();
        StringBuilder systemPromptBuilder = new StringBuilder();

        if (agent.getSystemPrompt() != null && !agent.getSystemPrompt().isBlank()) {
            systemPromptBuilder.append(agent.getSystemPrompt());
        }

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

        AgentTraceStep step = new AgentTraceStep();
        step.setStepIndex(stepIndex);
        step.setStepType("prompt_assembly");
        step.setStepName("提示词组装");
        step.setStatus("success");
        step.setDurationMs(System.currentTimeMillis() - stepStart);
        step.setInput(agent.getSystemPrompt());
        step.setOutput(systemPrompt);
        Map<String, Object> meta = new HashMap<>();
        meta.put("templateCount", enabledPrompts.size());
        step.setMetadata(meta);

        AgentTraceStepResult result = new AgentTraceStepResult();
        result.step = step;
        result.output = systemPrompt;
        result.nextStepIndex = stepIndex + 1;
        return result;
    }

    // ===== Step 2: 知识库检索 =====

    /**
     * 从 Agent 关联的知识库中检索相关内容
     * <p>遍历 Agent 关联的已启用知识库，对每个知识库执行检索，汇总结果。</p>
     */
    private AgentTraceStepResult retrieveKnowledge(Agent agent, String query, int stepIndex) {
        long stepStart = System.currentTimeMillis();
        StringBuilder knowledgeBuilder = new StringBuilder();
        int totalChunks = 0;

        // 获取 Agent 关联的已启用知识库
        List<AgentKnowledge> enabledKnowledge = agentKnowledgeMapper.selectByAgentId(agent.getId()).stream()
                .filter(k -> k.getEnabled() != null && k.getEnabled() == 1)
                .sorted(Comparator.comparingInt(k -> k.getSortOrder() != null ? k.getSortOrder() : 0))
                .collect(Collectors.toList());

        for (AgentKnowledge ak : enabledKnowledge) {
            int topK = ak.getRetrievalCount() != null ? ak.getRetrievalCount() : 3;
            List<RetrievalResult> results = knowledgeBaseService.retrieve(ak.getKnowledgeId(), query, topK);

            if (!results.isEmpty()) {
                knowledgeBuilder.append("【").append(ak.getKnowledgeName() != null ? ak.getKnowledgeName() : "知识库").append("】\n");
                for (RetrievalResult r : results) {
                    knowledgeBuilder.append("- ").append(r.getContent()).append("\n");
                    totalChunks++;
                }
                knowledgeBuilder.append("\n");
            }
        }

        AgentTraceStep step = new AgentTraceStep();
        step.setStepIndex(stepIndex);
        step.setStepType("knowledge_retrieval");
        step.setStepName("知识库检索");
        step.setStatus(totalChunks > 0 ? "success" : "skip");
        step.setDurationMs(System.currentTimeMillis() - stepStart);
        step.setOutput(knowledgeBuilder.toString());
        Map<String, Object> meta = new HashMap<>();
        meta.put("knowledgeCount", enabledKnowledge.size());
        meta.put("chunkCount", totalChunks);
        step.setMetadata(meta);

        AgentTraceStepResult result = new AgentTraceStepResult();
        result.step = step;
        result.output = knowledgeBuilder.toString();
        result.nextStepIndex = stepIndex + 1;
        return result;
    }

    // ===== Step 3: 技能调用 =====

    /**
     * 执行 Agent 关联的技能
     * <p>流程：遍历技能 → LLM 提取参数 → 调用 endpoint → 返回结果。</p>
     */
    private AgentTraceStepResult executeSkill(Agent agent, String userMessage, int stepIndex) {
        long stepStart = System.currentTimeMillis();
        StringBuilder skillBuilder = new StringBuilder();
        int executedCount = 0;

        // 获取 Agent 关联的已启用技能
        List<AgentSkill> enabledSkills = agentSkillMapper.selectByAgentId(agent.getId()).stream()
                .filter(s -> s.getEnabled() != null && s.getEnabled() == 1)
                .sorted(Comparator.comparingInt(s -> s.getSortOrder() != null ? s.getSortOrder() : 0))
                .collect(Collectors.toList());

        // 获取 AI 配置（用于 LLM 智能选择技能）
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);

        // LLM 智能选择最匹配的技能，避免全部执行
        if (config != null) {
            enabledSkills = selectSkillsByLLM(userMessage, enabledSkills, config);
        }

        for (AgentSkill as : enabledSkills) {
            Skill skill = skillMapper.selectById(as.getSkillId());
            if (skill == null || skill.getStatus() == null || skill.getStatus() != 1) {
                continue;
            }
            if (skill.getEndpoint() == null || skill.getEndpoint().isBlank()) {
                continue;
            }

            try {
                // 1. 检查 AI 配置
                if (config == null) {
                    continue;
                }

                // 2. LLM 提取参数
                String inputSchema = skill.getInputSchema();
                Map<String, Object> extractedParams = extractParamsByLLM(userMessage, inputSchema, config);

                // 3. 处理 URL
                String url = skill.getEndpoint();
                boolean isInternalCall = url.startsWith("/");
                if (isInternalCall) {
                    url = "http://localhost:" + serverPort + url;
                    // 分页参数拼接到 URL
                    if (!url.contains("?")) {
                        url += "?pageNum=1&pageSize=10";
                    }
                }

                // 4. 构建请求体
                Map<String, Object> requestBody;
                if (isInternalCall) {
                    // 内部接口：直接使用提取的参数作为请求体
                    requestBody = extractedParams != null ? extractedParams : new HashMap<>();
                } else {
                    // 外部 API：使用通用格式
                    requestBody = new HashMap<>();
                    requestBody.put("model", config.getChatModel());
                    requestBody.put("query", userMessage);
                    requestBody.put("params", extractedParams);
                }

                // 5. 构建 Headers
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                if (isInternalCall) {
                    // 内部调用：转发当前请求的 JWT token，跳过安全检查
                    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attrs != null) {
                        String authHeader = attrs.getRequest().getHeader("Authorization");
                        if (authHeader != null) {
                            headers.set("Authorization", authHeader);
                        }
                    }
                } else {
                    headers.set("Authorization", "Bearer " + config.getApiKey());
                }

                org.springframework.http.HttpEntity<Map<String, Object>> entity =
                        new org.springframework.http.HttpEntity<>(requestBody, headers);

                // 6. 创建不抛异常的 RestTemplate 用于内部调用
                RestTemplate silentRt = new RestTemplate();
                silentRt.setRequestFactory(restTemplate.getRequestFactory());
                silentRt.setErrorHandler(new org.springframework.web.client.ResponseErrorHandler() {
                    public boolean hasError(org.springframework.http.client.ClientHttpResponse resp) { return false; }
                    public void handleError(org.springframework.http.client.ClientHttpResponse resp) {}
                });
                // 6a. 先试 POST（兼容 POST-only 接口如 /stockMarketQuote/page）
                ResponseEntity<Map> responseEntity = silentRt.exchange(url, HttpMethod.POST, entity, Map.class);
                // 6b. POST 失败（如 405/400）则用 GET 重试（兼容 GET-only 接口如 /stockDividend/list）
                if (responseEntity.getStatusCode().isError()) {
                    StringBuilder fullUrl = new StringBuilder(url);
                    try {
                        for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
                            if (entry.getValue() != null) {
                                fullUrl.append(fullUrl.indexOf("?") > -1 ? "&" : "?")
                                      .append(entry.getKey()).append("=")
                                      .append(java.net.URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
                            }
                        }
                    } catch (java.io.UnsupportedEncodingException ignored) {
                    }
                    org.springframework.http.HttpEntity<Void> getEntity =
                            new org.springframework.http.HttpEntity<>(null, headers);
                    responseEntity = silentRt.exchange(fullUrl.toString(), HttpMethod.GET, getEntity, Map.class);
                }
                Map<String, Object> response = responseEntity.getBody();

                // 7. 解析响应
                if (response != null) {
                    Object resultData = extractResponseData(response);

                    if (resultData != null) {
                        skillBuilder.append("【").append(skill.getName()).append("】\n");
                        skillBuilder.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultData)).append("\n\n");
                        executedCount++;
                    }
                }
            } catch (Exception e) {
                skillBuilder.append("【").append(skill.getName()).append("】执行失败: ").append(e.getMessage()).append("\n\n");
            }
        }

        AgentTraceStep step = new AgentTraceStep();
        step.setStepIndex(stepIndex);
        step.setStepType("skill_execution");
        step.setStepName("技能调用");
        step.setStatus(executedCount > 0 ? "success" : "skip");
        step.setDurationMs(System.currentTimeMillis() - stepStart);
        step.setOutput(skillBuilder.toString());
        Map<String, Object> meta = new HashMap<>();
        meta.put("skillCount", enabledSkills.size());
        meta.put("executedCount", executedCount);
        step.setMetadata(meta);

        AgentTraceStepResult result = new AgentTraceStepResult();
        result.step = step;
        result.output = skillBuilder.toString();
        result.nextStepIndex = stepIndex + 1;
        return result;
    }

    /**
     * LLM 提取技能参数
     * <p>根据 input_schema 和用户消息，调用 LLM 提取结构化参数。
     * 轻量级调用，仅要求返回 JSON 对象。</p>
     *
     * @param userMessage 用户原始消息
     * @param inputSchema 技能的输入参数 JSON Schema
     * @param config      AI 提供商配置
     * @return 提取的参数键值对，提取失败返回空 Map
     */
    private Map<String, Object> extractParamsByLLM(String userMessage, String inputSchema,
                                                    AIProperties.ProviderConfig config) {
        if (inputSchema == null || inputSchema.isBlank()) {
            // 没有 schema，返回关键词兜底
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("keywords", userMessage);
            return fallback;
        }

        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

        // 构建参数提取的 Prompt
        String systemPrompt = "你是一个参数提取器。根据以下 JSON Schema 定义的参数结构，从用户消息中提取对应的参数值。\n" +
                "规则：\n" +
                "1. 只返回一个 JSON 对象，不要返回任何其他文字、解释或 markdown 标记\n" +
                "2. 如果用户消息中没有提到某个参数，不要包含该字段\n" +
                "3. 参数类型必须与 Schema 定义一致（字符串、整数等）\n" +
                "4. 如果无法从用户消息中提取任何参数，返回空对象 {}\n\n" +
                "Schema 定义：\n" + inputSchema;

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);
        messages.add(sysMsg);

        Map<String, String> usrMsg = new HashMap<>();
        usrMsg.put("role", "user");
        usrMsg.put("content", userMessage);
        messages.add(usrMsg);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getChatModel());
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0);

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            org.springframework.http.HttpEntity<Map<String, Object>> entity =
                    new org.springframework.http.HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> apiResponse = restTemplate.postForObject(url, entity, Map.class);

            if (apiResponse != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    if (message != null && message.get("content") != null) {
                        String content = message.get("content").trim();
                        // 去除可能的 markdown 代码块标记
                        if (content.startsWith("```")) {
                            content = content.replaceAll("^```(json)?\\s*", "").replaceAll("\\s*```$", "");
                        }
                        // 解析 JSON
                        @SuppressWarnings("unchecked")
                        Map<String, Object> extracted = objectMapper.readValue(content, Map.class);
                        return extracted;
                    }
                }
            }
        } catch (Exception e) {
            // 参数提取失败，返回兜底参数
        }

        // 兜底：返回关键词搜索
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("keywords", userMessage);
        return fallback;
    }

    /**
     * LLM 智能选择技能
     * <p>根据用户消息和技能列表，调用 LLM 选出最匹配的技能，避免全部执行。</p>
     *
     * @param userMessage    用户原始消息
     * @param enabledSkills  Agent 关联的已启用技能列表
     * @param config         AI 提供商配置
     * @return 过滤后的技能列表（仅包含 LLM 选中的技能）
     */
    private List<AgentSkill> selectSkillsByLLM(String userMessage, List<AgentSkill> enabledSkills,
                                                 AIProperties.ProviderConfig config) {
        if (enabledSkills == null || enabledSkills.isEmpty()) {
            return enabledSkills;
        }

        // 如果只有一个技能，直接返回，无需 LLM 选择
        if (enabledSkills.size() == 1) {
            return enabledSkills;
        }

        // 构建技能列表描述
        StringBuilder skillListDesc = new StringBuilder("[");
        for (int i = 0; i < enabledSkills.size(); i++) {
            AgentSkill as = enabledSkills.get(i);
            Skill skill = skillMapper.selectById(as.getSkillId());
            if (skill == null) {
                continue;
            }
            if (i > 0) {
                skillListDesc.append(", ");
            }
            skillListDesc.append("{\"code\":\"").append(skill.getCode())
                    .append("\",\"name\":\"").append(skill.getName())
                    .append("\",\"description\":\"")
                    .append(skill.getDescription() != null ? skill.getDescription() : "")
                    .append("\"}");
        }
        skillListDesc.append("]");

        String systemPrompt = "你是一个技能选择器。根据用户消息，从技能列表中选出最匹配的技能。\n\n" +
                "可用技能：\n" + skillListDesc + "\n\n" +
                "规则：\n" +
                "1. 只返回 JSON 数组，包含选中技能的 code，如 [\"stock_market_quote\"]\n" +
                "2. 根据用户意图选择最相关的技能，可以选多个\n" +
                "3. 如果用户意图不明确或与所有技能无关，返回空数组 []\n" +
                "4. 不要返回任何解释文字、markdown 标记或其他内容\n\n" +
                "用户消息：" + userMessage;

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);
        messages.add(sysMsg);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getChatModel());
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0);

        try {
            String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            org.springframework.http.HttpEntity<Map<String, Object>> entity =
                    new org.springframework.http.HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> apiResponse = restTemplate.postForObject(url, entity, Map.class);

            if (apiResponse != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    if (message != null && message.get("content") != null) {
                        String content = message.get("content").trim();
                        // 去除可能的 markdown 代码块标记
                        if (content.startsWith("```")) {
                            content = content.replaceAll("^```(json)?\\s*", "").replaceAll("\\s*```$", "");
                        }
                        // 解析 JSON 数组
                        @SuppressWarnings("unchecked")
                        List<String> selectedCodes = objectMapper.readValue(content, List.class);
                        if (selectedCodes != null && !selectedCodes.isEmpty()) {
                            // 过滤出选中的技能
                            Set<String> codeSet = new HashSet<>(selectedCodes);
                            return enabledSkills.stream()
                                    .filter(as -> {
                                        Skill skill = skillMapper.selectById(as.getSkillId());
                                        return skill != null && codeSet.contains(skill.getCode());
                                    })
                                    .collect(Collectors.toList());
                        }
                        // LLM 返回空数组，不执行任何技能
                        return new ArrayList<>();
                    }
                }
            }
        } catch (Exception e) {
            // 选择失败，降级为执行所有技能
        }

        return enabledSkills;
    }

    /**
     * 从响应中提取结果数据
     * <p>适配多种返回格式：result / data / list / records</p>
     */
    private Object extractResponseData(Map<String, Object> response) {
        Object data = response.get("result");
        if (data == null) data = response.get("data");
        if (data == null) data = response.get("list");
        if (data == null) data = response.get("records");
        return data;
    }

    // ===== Step 4: LLM 调用 =====

    /**
     * 调用 LLM API
     * <p>构建消息列表并调用 LLM，返回响应内容和 Token 统计。</p>
     */
    private AgentTraceStepResult callLLM(Agent agent, String systemPrompt,
                                          AgentTestChatRequest request, int stepIndex) {
        long llmStart = System.currentTimeMillis();
        String modelName = agent.getModelCode() != null ? agent.getModelCode() :
                aiProperties.getProviders().get(aiProperties.getActiveProvider()).getChatModel();

        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        // 添加历史消息（如果启用记忆）
        if (agent.getMemoryEnabled() != null && agent.getMemoryEnabled() == 1
                && request.getHistory() != null && !request.getHistory().isEmpty()) {
            int window = agent.getMemoryWindow() != null ? agent.getMemoryWindow() : 20;
            List<Map<String, String>> history = new ArrayList<>(request.getHistory());
            if (history.size() > window * 2) {
                history = history.subList(history.size() - window * 2, history.size());
            }
            messages.addAll(history);
        }

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", request.getMessage());
        messages.add(userMsg);

        // 获取 AI 配置
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
        if (config == null) {
            throw new RuntimeException("AI 配置错误：未找到提供商 [" + providerName + "]");
        }

        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        if (agent.getTemperature() != null) {
            requestBody.put("temperature", agent.getTemperature());
        }
        if (agent.getMaxTokens() != null) {
            requestBody.put("max_tokens", agent.getMaxTokens());
        }

        // 发送请求
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey());

        org.springframework.http.HttpEntity<Map<String, Object>> entity =
                new org.springframework.http.HttpEntity<>(requestBody, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> apiResponse = restTemplate.postForObject(url, entity, Map.class);

        String replyContent = "";
        int promptTokens = 0;
        int completionTokens = 0;

        if (apiResponse != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                @SuppressWarnings("unchecked")
                Map<String, String> message = (Map<String, String>) choice.get("message");
                if (message != null && message.get("content") != null) {
                    replyContent = message.get("content");
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> usage = (Map<String, Object>) apiResponse.get("usage");
            if (usage != null) {
                promptTokens = usage.get("prompt_tokens") != null ? (Integer) usage.get("prompt_tokens") : 0;
                completionTokens = usage.get("completion_tokens") != null ? (Integer) usage.get("completion_tokens") : 0;
            }
        }

        long llmDuration = System.currentTimeMillis() - llmStart;
        AgentTraceStep step = new AgentTraceStep();
        step.setStepIndex(stepIndex);
        step.setStepType("llm_call");
        step.setStepName("LLM 调用");
        step.setStatus("success");
        step.setDurationMs(llmDuration);
        step.setInput(systemPrompt);
        step.setOutput(replyContent);
        Map<String, Object> llmMeta = new HashMap<>();
        llmMeta.put("model", modelName);
        llmMeta.put("tokensPrompt", promptTokens);
        llmMeta.put("tokensCompletion", completionTokens);
        llmMeta.put("temperature", agent.getTemperature());
        step.setMetadata(llmMeta);

        AgentTraceStepResult result = new AgentTraceStepResult();
        result.step = step;
        result.output = replyContent;
        result.tokensPrompt = promptTokens;
        result.tokensCompletion = completionTokens;
        result.modelName = modelName;
        result.nextStepIndex = stepIndex + 1;
        return result;
    }

    // ===== 辅助方法 =====

    /** 构建失败步骤 */
    private AgentTraceStep buildFailStep(int stepIndex, String stepType, String stepName, String errorMessage) {
        AgentTraceStep step = new AgentTraceStep();
        step.setStepIndex(stepIndex);
        step.setStepType(stepType);
        step.setStepName(stepName);
        step.setStatus("fail");
        step.setDurationMs(0L);
        step.setOutput(errorMessage);
        step.setMetadata(new HashMap<>());
        return step;
    }

    /** 构建链路追踪快照 */
    private AgentTraceSnapshot buildTraceSnapshot(List<AgentTraceStep> steps, long totalDuration,
                                                   int promptTokens, int completionTokens) {
        AgentTraceSnapshot snapshot = new AgentTraceSnapshot();
        snapshot.setSteps(steps);
        snapshot.setTotalDurationMs(totalDuration);
        AgentTraceSnapshot.TotalTokens tokens = new AgentTraceSnapshot.TotalTokens();
        tokens.setPrompt(promptTokens);
        tokens.setCompletion(completionTokens);
        snapshot.setTotalTokens(tokens);
        return snapshot;
    }

    /** Step 结果内部类 */
    private static class AgentTraceStepResult {
        AgentTraceStep step;
        String output;
        int nextStepIndex;
        int tokensPrompt;
        int tokensCompletion;
        String modelName;
    }

    /** 保存失败的测试会话 */
    private AgentTestChatResponse buildErrorResponse(Agent agent, AgentTestChatRequest request,
                                                     List<AgentTraceStep> steps, long totalStart,
                                                     Long userId, Exception e) {
        long totalDuration = System.currentTimeMillis() - totalStart;
        AgentTraceSnapshot snapshot = new AgentTraceSnapshot();
        snapshot.setSteps(steps);
        snapshot.setTotalDurationMs(totalDuration);
        AgentTraceSnapshot.TotalTokens tokens = new AgentTraceSnapshot.TotalTokens();
        tokens.setPrompt(0);
        tokens.setCompletion(0);
        snapshot.setTotalTokens(tokens);

        saveTestSession(agent, request, null, snapshot, (int) totalDuration,
                0, 0, null, "fail", e.getMessage(), userId);

        AgentTestChatResponse response = new AgentTestChatResponse();
        response.setContent("");
        response.setTokensPrompt(0);
        response.setTokensCompletion(0);
        response.setDurationMs((int) totalDuration);
        response.setTraceSnapshot(snapshot);
        return response;
    }

    /** 保存测试会话到数据库 */
    private void saveTestSession(Agent agent, AgentTestChatRequest request, String reply,
                                 AgentTraceSnapshot snapshot, int durationMs,
                                 int promptTokens, int completionTokens, String modelName,
                                 String status, String errorMessage, Long userId) {
        ExecutionSession session = new ExecutionSession();
        session.setBizType("agent");
        session.setBizId(agent.getId());
        session.setBizName(agent.getName());
        session.setUserMessage(request.getMessage());
        session.setOutputResult(reply);
        try {
            session.setTraceSnapshot(objectMapper.writeValueAsString(snapshot));
        } catch (JsonProcessingException ex) {
            session.setTraceSnapshot("{}");
        }
        session.setTotalDurationMs(durationMs);
        session.setTokensPrompt(promptTokens);
        session.setTokensCompletion(completionTokens);
        session.setTokensTotal(promptTokens + completionTokens);
        session.setModelName(modelName);
        session.setStatus(status);
        session.setErrorMessage(errorMessage);
        session.setCreatedBy(userId);
        executionSessionMapper.insert(session);
    }

    // ===== 测试会话查询 =====

    /** 分页查询 Agent 的测试会话列表 */
    public PageInfo<ExecutionSession> getTestSessions(Long agentId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ExecutionSession> list = executionSessionMapper.selectList("agent", agentId, null, null, null);
        return new PageInfo<>(list);
    }

    /** 查询测试会话详情 */
    public ExecutionSession getTestSessionDetail(Long agentId, Long sessionId) {
        return executionSessionMapper.selectById(sessionId);
    }

    /** 删除测试会话（逻辑删除） */
    @Transactional
    public int deleteTestSession(Long agentId, Long sessionId) {
        return executionSessionMapper.logicalDelete(sessionId);
    }
}
