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

import java.util.*;

/**
 * AI 技能核心服务
 * <p>提供技能的 CRUD、AI 执行调用、执行日志查询等功能。
 * 执行时自动替换 prompt_template 中的 {{variable}} 插值并调用 LLM API。</p>
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
     * @param moduleKey  模块标识（可选）
     * @param status     状态（可选，1启用 0禁用）
     * @param keyword    名称/编码关键词（可选）
     */
    public PageInfo<Skill> getList(int pageNum, int pageSize, Long categoryId, String moduleKey, Integer status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<Skill> list = skillMapper.selectList(categoryId, moduleKey, status, keyword);
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
        return skillMapper.insert(skill);
    }

    /** 更新技能 */
    @Transactional
    public int update(Skill skill) {
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
     * <p>核心流程：加载技能 → 替换 prompt 模板变量 → 调用 LLM API → 记录执行日志 → 增加使用次数。</p>
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

        // 2. 校验 prompt_template 不为空
        String promptTemplate = skill.getPromptTemplate();
        if (promptTemplate == null || promptTemplate.isBlank()) {
            throw new RuntimeException("技能 prompt_template 为空: " + skill.getName());
        }

        // 3. 替换模板中的 {{variable}} 占位符
        Map<String, Object> params = request.getParams();
        if (params == null) {
            params = new HashMap<>();
        }
        String filledPrompt = AiExecuteHelper.replacePlaceholders(promptTemplate, params);
        if (request.getUserMessage() != null && !request.getUserMessage().isBlank()) {
            filledPrompt += "\n\n用户补充信息：\n" + request.getUserMessage();
        }

        // 4. 调用 LLM API
        AiExecuteResult result;
        try {
            result = AiExecuteHelper.callLLM(filledPrompt, skill.getTemperature(), skill.getMaxTokens(), restTemplate, aiProperties);
        } catch (Exception e) {
            // 记录失败执行日志
            SkillExecutionLog failLog = new SkillExecutionLog();
            failLog.setSkillId(skill.getId());
            failLog.setSkillCode(skill.getCode());
            failLog.setModuleKey(skill.getModuleKey());
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

        // 5. 记录成功执行日志
        SkillExecutionLog log = new SkillExecutionLog();
        log.setSkillId(skill.getId());
        log.setSkillCode(skill.getCode());
        log.setModuleKey(skill.getModuleKey());
        try {
            log.setInputParams(objectMapper.writeValueAsString(params));
        } catch (Exception jsonEx) {
            log.setInputParams(params.toString());
        }
        log.setOutputResult(result.getOutput());
        log.setTokensPrompt(result.getTokensPrompt());
        log.setTokensCompletion(result.getTokensCompletion());
        log.setDurationMs(result.getDurationMs());
        log.setStatus("success");
        log.setCreatedBy(userId);
        logMapper.insert(log);

        // 6. 增加技能使用次数
        skillMapper.incrementUseCount(skill.getId());

        return result;
    }

    /**
     * 分页查询当前用户的技能执行日志
     * <p>按创建时间倒序排列，仅返回当前登录用户的日志。</p>
     */
    public PageInfo<SkillExecutionLog> getLogList(int pageNum, int pageSize, Long skillId, String skillCode, String moduleKey, String status, Long createdBy) {
        PageHelper.startPage(pageNum, pageSize);
        List<SkillExecutionLog> list = logMapper.selectList(skillId, skillCode, moduleKey, status, createdBy);
        return new PageInfo<>(list);
    }

    /** 根据日志 ID 查询执行详情 */
    public SkillExecutionLog getLogById(Long id) {
        return logMapper.selectById(id);
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

        // 2. 校验 prompt_template 不为空
        String rawPromptTemplate = skill.getPromptTemplate();
        if (rawPromptTemplate == null || rawPromptTemplate.isBlank()) {
            throw new RuntimeException("技能 prompt_template 为空: " + skill.getName());
        }

        // 3. 替换模板中的 {{variable}} 占位符
        Map<String, Object> params = request.getParams();
        if (params == null) {
            params = new HashMap<>();
        }
        String renderedPrompt = AiExecuteHelper.replacePlaceholders(rawPromptTemplate, params);
        if (request.getUserMessage() != null && !request.getUserMessage().isBlank()) {
            renderedPrompt += "\n\n用户补充信息：\n" + request.getUserMessage();
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
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", renderedPrompt);
        messages.add(systemMsg);
        llmRequestBody.put("messages", messages);
        llmRequestBody.put("stream", false);
        if (skill.getTemperature() != null) {
            llmRequestBody.put("temperature", skill.getTemperature());
        }
        if (skill.getMaxTokens() != null) {
            llmRequestBody.put("max_tokens", skill.getMaxTokens());
        }

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
            SkillDebugLog failLog = buildDebugLog(skill, request, userId, rawPromptTemplate, renderedPrompt,
                    llmRequestBody, null, llmModel, promptTokens, completionTokens,
                    (int) totalDuration, (int) llmDuration, null, "fail", e.getMessage());
            debugLogMapper.insert(failLog);

            throw new RuntimeException("调试执行失败: " + e.getMessage(), e);
        }

        long totalDuration = System.currentTimeMillis() - totalStart;

        // 6. 保存成功调试日志
        SkillDebugLog debugLog = buildDebugLog(skill, request, userId, rawPromptTemplate, renderedPrompt,
                llmRequestBody, llmResponseBody, llmModel, promptTokens, completionTokens,
                (int) totalDuration, (int) llmDuration, output, "success", null);
        debugLogMapper.insert(debugLog);

        // 7. 增加使用次数
        skillMapper.incrementUseCount(skill.getId());

        // 8. 组装响应
        SkillDebugResponse response = new SkillDebugResponse();
        response.setOutput(output);
        response.setRenderedPrompt(renderedPrompt);
        response.setRawPromptTemplate(rawPromptTemplate);
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
                                        String rawPromptTemplate, String renderedPrompt,
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
        log.setRawPromptTemplate(rawPromptTemplate);
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
