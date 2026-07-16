package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.AiExecuteResult;
import org.seaPack.dto.ai.SkillDebugRequest;
import org.seaPack.dto.ai.SkillDebugResponse;
import org.seaPack.dto.ai.SkillExecuteRequest;
import org.seaPack.mapper.ai.SkillDebugLogMapper;
import org.seaPack.mapper.ai.SkillExecutionLogMapper;
import org.seaPack.mapper.ai.SkillMapper;
import org.seaPack.mapper.ai.SkillModuleBindingMapper;
import org.seaPack.mapper.ai.SkillParamMapper;
import org.seaPack.model.ai.Skill;
import org.seaPack.model.ai.SkillDebugLog;
import org.seaPack.model.ai.SkillExecutionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

/**
 * AI 技能核心服务
 * <p>提供技能的 CRUD、AI 执行调用、执行日志查询等功能。
 * 执行时根据技能类型（tool/rag/hybrid）调用对应的 endpoint 并记录日志。</p>
 */
@Service
public class SkillService {

    @Autowired
    private SkillMapper skillMapper;

    @Autowired
    private SkillParamMapper paramMapper;

    @Autowired
    private SkillModuleBindingMapper bindingMapper;

    @Autowired
    private SkillExecutionLogMapper logMapper;

    @Autowired
    private SkillDebugLogMapper debugLogMapper;

    @Autowired
    private AIProperties aiProperties;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 分页查询技能列表
     *
     * @param categoryId 分类 ID（可选）
     * @param skillType  技能类型（可选：tool/rag/hybrid）
     * @param status     状态（可选，1启用 0禁用）
     * @param keyword    名称/编码关键词（可选）
     */
    public PageInfo<Skill> getList(int pageNum, int pageSize, Long categoryId, String skillType, Integer status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<Skill> list = skillMapper.selectList(categoryId, skillType, status, keyword);
        return new PageInfo<>(list);
    }

    /** 根据 ID 查询技能详情 */
    public Skill getById(Long id) {
        return skillMapper.selectById(id);
    }

    /** 校验技能编码是否已存在（excludeId 用于更新时排除自身） */
    public boolean isCodeDuplicate(String code, Long excludeId) {
        return skillMapper.countByCode(code, excludeId) > 0;
    }

    /** 新增技能 */
    @Transactional
    public int insert(Skill skill) {
        if (skill.getInputSchema() != null && skill.getInputSchema().isEmpty()) {
            skill.setInputSchema(null);
        }
        return skillMapper.insert(skill);
    }

    /** 更新技能 */
    @Transactional
    public int update(Skill skill) {
        if (skill.getInputSchema() != null && skill.getInputSchema().isEmpty()) {
            skill.setInputSchema(null);
        }
        return skillMapper.update(skill);
    }

    /**
     * 删除技能（级联删除）
     * <p>同时删除该技能关联的参数定义和模块绑定关系。</p>
     */
    @Transactional
    public int deleteById(Long id) {
        paramMapper.deleteBySkillId(id);
        bindingMapper.deleteBySkillId(id);
        return skillMapper.deleteById(id);
    }

    /**
     * 执行 AI 技能
     * <p>核心流程：加载技能 → 构建请求 → 调用 endpoint → 记录执行日志 → 增加使用次数。</p>
     *
     * @param skillId 技能 ID
     * @param request 执行请求（含参数和用户补充消息）
     * @param userId  当前用户 ID（用于记录日志）
     * @return 执行结果（含输出内容、Token 统计、耗时）
     */
    @Transactional
    public AiExecuteResult execute(Long skillId, SkillExecuteRequest request, Long userId) {

        // 1. 加载技能并校验状态
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw new RuntimeException("技能不存在: " + skillId);
        }
        if (skill.getStatus() == null || skill.getStatus() != 1) {
            throw new RuntimeException("技能已禁用: " + skill.getName());
        }

        // 2. 校验 endpoint 不为空
        String endpoint = skill.getEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            throw new RuntimeException("技能 endpoint 为空: " + skill.getName());
        }

        // 3. 构建请求参数
        Map<String, Object> params = request.getParams();
        if (params == null) {
            params = new HashMap<>();
        }

        // 4. 调用 LLM API
        AiExecuteResult result;
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
        String modelName = config != null ? config.getChatModel() : null;

        try {
            result = AiExecuteHelper.callLLMWithParams(endpoint, params, request.getUserMessage(), restTemplate, aiProperties);
        } catch (Exception e) {
            // 记录失败执行日志
            SkillExecutionLog failLog = new SkillExecutionLog();
            failLog.setSkillId(skill.getId());
            failLog.setSkillCode(skill.getCode());
            failLog.setModelName(modelName);
            try {
                failLog.setInputParams(objectMapper.writeValueAsString(params));
            } catch (Exception jsonEx) {
                failLog.setInputParams(params.toString());
            }
            failLog.setStatus("fail");
            failLog.setErrorMessage(e.getMessage());
            failLog.setCreatedBy(userId);
            logMapper.insert(failLog);
            throw new RuntimeException("AI 技能执行失败: " + e.getMessage(), e);
        }

        // 5. 计算总 token 和估算费用
        int tokensTotal = result.getTokensPrompt() + result.getTokensCompletion();
        BigDecimal costYuan = calculateCost(tokensTotal);

        // 6. 记录成功执行日志
        SkillExecutionLog log = new SkillExecutionLog();
        log.setSkillId(skill.getId());
        log.setSkillCode(skill.getCode());
        log.setModelName(modelName);
        try {
            log.setInputParams(objectMapper.writeValueAsString(params));
        } catch (Exception jsonEx) {
            log.setInputParams(params.toString());
        }
        log.setOutputResult(result.getOutput());
        log.setTokensPrompt(result.getTokensPrompt());
        log.setTokensCompletion(result.getTokensCompletion());
        log.setTokensTotal(tokensTotal);
        log.setCostYuan(costYuan);
        log.setDurationMs(result.getDurationMs());
        log.setStatus("success");
        log.setCreatedBy(userId);
        logMapper.insert(log);

        // 6. 增加技能使用次数
        skillMapper.incrementUseCount(skill.getId());

        return result;
    }

    /**
     * 分页查询技能执行日志
     * <p>按创建时间倒序排列，支持多条件筛选。</p>
     */
    public PageInfo<SkillExecutionLog> getLogList(int pageNum, int pageSize, Long skillId, String skillCode,
                                                   String modelName, String moduleKey, Long sceneId, Long agentId,
                                                   String status, Long createdBy) {
        PageHelper.startPage(pageNum, pageSize);
        List<SkillExecutionLog> list = logMapper.selectList(skillId, skillCode, modelName, moduleKey, sceneId, agentId, status, createdBy);
        return new PageInfo<>(list);
    }

    /** 根据日志 ID 查询执行详情 */
    public SkillExecutionLog getLogById(Long id) {
        return logMapper.selectById(id);
    }

    /**
     * 根据 token 总数估算费用（元）
     * <p>按 GPT-4o-mini 价格估算：输入 0.15元/百万token，输出 0.60元/百万token。
     * 实际费用需根据具体模型和提供商调整。</p>
     *
     * @param tokensTotal 总 token 数
     * @return 估算费用（元），保留4位小数
     */
    private BigDecimal calculateCost(int tokensTotal) {
        // 简化估算：假设输入输出各占50%，按混合价格 0.375元/百万token 计算
        // 实际应用中应根据具体模型配置不同费率
        double cost = tokensTotal * 0.375 / 1_000_000.0;
        return BigDecimal.valueOf(cost).setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    // ===== 调试执行（含完整请求/响应记录） =====

    /**
     * 调试执行技能（含完整链路记录）
     * <p>与 execute 类似，但记录完整的 LLM 请求体、响应体、原始模板、渲染后 Prompt 等调试信息。</p>
     *
     * @param skillId 技能 ID
     * @param request 调试请求（含参数和用户补充消息）
     * @param userId  当前用户 ID
     * @return 调试响应（含完整调试信息和日志 ID）
     */
    @Transactional
    public SkillDebugResponse debug(Long skillId, SkillDebugRequest request, Long userId) {
        long totalStart = System.currentTimeMillis();

        // 1. 加载技能并校验状态
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw new RuntimeException("技能不存在: " + skillId);
        }
        if (skill.getStatus() == null || skill.getStatus() != 1) {
            throw new RuntimeException("技能已禁用: " + skill.getName());
        }

        // 2. 校验 endpoint 不为空
        String endpoint = skill.getEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            throw new RuntimeException("技能 endpoint 为空: " + skill.getName());
        }

        // 3. 构建请求参数
        Map<String, Object> params = request.getParams();
        if (params == null) {
            params = new HashMap<>();
        }

        // 4. 获取 AI 配置并构建请求体
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
        if (config == null) {
            throw new RuntimeException("AI 配置错误：未找到提供商 [" + providerName + "]");
        }

        String llmModel = config.getChatModel();
        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

        Map<String, Object> llmRequestBody = new HashMap<>();
        llmRequestBody.put("model", llmModel);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");

        // 构建用户消息内容
        StringBuilder contentBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            contentBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        if (request.getUserMessage() != null && !request.getUserMessage().isBlank()) {
            contentBuilder.append(request.getUserMessage());
        }
        userMsg.put("content", contentBuilder.toString().trim());
        messages.add(userMsg);
        llmRequestBody.put("messages", messages);
        llmRequestBody.put("stream", false);

        // 5. 发送 LLM 请求
        String output = "";
        Map<String, Object> llmResponseBody = null;
        int promptTokens = 0;
        int completionTokens = 0;
        long llmStart = System.currentTimeMillis();
        long llmDuration = 0;

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            org.springframework.http.HttpEntity<Map<String, Object>> entity =
                    new org.springframework.http.HttpEntity<>(llmRequestBody, headers);

            llmResponseBody = restTemplate.postForObject(url, entity, Map.class);
            llmDuration = System.currentTimeMillis() - llmStart;

            if (llmResponseBody != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) llmResponseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    if (message != null && message.get("content") != null) {
                        output = message.get("content");
                    }
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> usage = (Map<String, Object>) llmResponseBody.get("usage");
                if (usage != null) {
                    promptTokens = usage.get("prompt_tokens") != null ? (Integer) usage.get("prompt_tokens") : 0;
                    completionTokens = usage.get("completion_tokens") != null ? (Integer) usage.get("completion_tokens") : 0;
                }
            }

        } catch (Exception e) {
            llmDuration = System.currentTimeMillis() - llmStart;
            long totalDuration = System.currentTimeMillis() - totalStart;

            // 保存失败调试日志
            SkillDebugLog failLog = buildDebugLog(skill, request, userId,
                    contentBuilder.toString(), llmRequestBody, null, llmModel,
                    promptTokens, completionTokens,
                    (int) totalDuration, (int) llmDuration, null, "fail", e.getMessage());
            debugLogMapper.insert(failLog);

            throw new RuntimeException("调试执行失败: " + e.getMessage(), e);
        }

        long totalDuration = System.currentTimeMillis() - totalStart;

        // 6. 保存成功调试日志
        SkillDebugLog debugLog = buildDebugLog(skill, request, userId,
                contentBuilder.toString(), llmRequestBody, llmResponseBody, llmModel,
                promptTokens, completionTokens,
                (int) totalDuration, (int) llmDuration, output, "success", null);
        debugLogMapper.insert(debugLog);

        // 7. 增加使用次数
        skillMapper.incrementUseCount(skill.getId());

        // 8. 组装响应
        SkillDebugResponse response = new SkillDebugResponse();
        response.setOutput(output);
        response.setRenderedPrompt(contentBuilder.toString());
        response.setRawPromptTemplate(null);
        response.setLlmRequestBody(llmRequestBody);
        response.setLlmResponseBody(llmResponseBody);
        response.setLlmModel(llmModel);
        response.setTokensPrompt(promptTokens);
        response.setTokensCompletion(completionTokens);
        response.setDurationMs((int) totalDuration);
        response.setDurationLlmMs((int) llmDuration);
        response.setDebugLogId(debugLog.getId());
        return response;
    }

    /** 构建调试日志实体 */
    private SkillDebugLog buildDebugLog(Skill skill, SkillDebugRequest request, Long userId,
                                        String renderedPrompt,
                                        Map<String, Object> llmRequestBody, Map<String, Object> llmResponseBody,
                                        String llmModel, int promptTokens, int completionTokens,
                                        int durationMs, int durationLlmMs, String output,
                                        String status, String errorMessage) {
        SkillDebugLog log = new SkillDebugLog();
        log.setSkillId(skill.getId());
        log.setSkillName(skill.getName());
        log.setSkillCode(skill.getCode());
        try {
            log.setInputParams(objectMapper.writeValueAsString(request.getParams()));
        } catch (Exception ex) {
            log.setInputParams(request.getParams() != null ? request.getParams().toString() : "{}");
        }
        log.setUserMessage(request.getUserMessage());
        log.setRawPromptTemplate(null);
        log.setRenderedPrompt(renderedPrompt);
        try {
            log.setLlmRequestBody(objectMapper.writeValueAsString(llmRequestBody));
        } catch (Exception ex) {
            log.setLlmRequestBody(llmRequestBody != null ? llmRequestBody.toString() : "{}");
        }
        try {
            log.setLlmResponseBody(objectMapper.writeValueAsString(llmResponseBody));
        } catch (Exception ex) {
            log.setLlmResponseBody(llmResponseBody != null ? llmResponseBody.toString() : "{}");
        }
        log.setLlmModel(llmModel);
        log.setTokensPrompt(promptTokens);
        log.setTokensCompletion(completionTokens);
        log.setDurationMs(durationMs);
        log.setDurationLlmMs(durationLlmMs);
        log.setOutputResult(output);
        log.setStatus(status);
        log.setErrorMessage(errorMessage);
        log.setCreatedBy(userId);
        return log;
    }

    // ===== 调试日志查询 =====

    /** 分页查询调试日志 */
    public PageInfo<SkillDebugLog> getDebugLogList(int pageNum, int pageSize, Long skillId, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<SkillDebugLog> list = debugLogMapper.selectList(skillId, status);
        return new PageInfo<>(list);
    }

    /** 查询调试日志详情 */
    public SkillDebugLog getDebugLogById(Long id) {
        return debugLogMapper.selectById(id);
    }

    /** 删除调试日志 */
    @Transactional
    public int deleteDebugLog(Long id) {
        return debugLogMapper.deleteById(id);
    }
}
