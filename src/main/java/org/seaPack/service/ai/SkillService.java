package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.SkillExecuteRequest;
import org.seaPack.dto.ai.SkillExecuteResponse;
import org.seaPack.mapper.ai.SkillExecutionLogMapper;
import org.seaPack.mapper.ai.SkillMapper;
import org.seaPack.mapper.ai.SkillModuleBindingMapper;
import org.seaPack.mapper.ai.SkillParamMapper;
import org.seaPack.model.ai.Skill;
import org.seaPack.model.ai.SkillExecutionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public SkillExecuteResponse execute(Long skillId, SkillExecuteRequest request, Long userId) {
        long startTime = System.currentTimeMillis();
        SkillExecuteResponse response = new SkillExecuteResponse();

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
        String filledPrompt = replacePlaceholders(promptTemplate, params);
        if (request.getUserMessage() != null && !request.getUserMessage().isBlank()) {
            filledPrompt += "\n\n用户补充信息：\n" + request.getUserMessage();
        }

        // 4. 获取 AI 提供商配置
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
        if (config == null) {
            throw new RuntimeException("AI 配置错误：未找到提供商 [" + providerName + "]");
        }

        // 5. 构建 LLM API 请求（OpenAI 兼容格式，非流式）
        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getChatModel());

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", filledPrompt);
        messages.add(systemMsg);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        // 6. 设置可选的 LLM 参数
        if (skill.getTemperature() != null) {
            requestBody.put("temperature", skill.getTemperature());
        }
        if (skill.getMaxTokens() != null) {
            requestBody.put("max_tokens", skill.getMaxTokens());
        }

        // 7. 发送 HTTP 请求
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> apiResponse = restTemplate.postForObject(url, entity, Map.class);
            long durationMs = System.currentTimeMillis() - startTime;

            // 8. 解析 AI 响应
            String output = "";
            Integer promptTokens = 0;
            Integer completionTokens = 0;

            if (apiResponse != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    if (message != null && message.get("content") != null) {
                        output = message.get("content");
                    }
                }

                Map<String, Object> usage = (Map<String, Object>) apiResponse.get("usage");
                if (usage != null) {
                    promptTokens = usage.get("prompt_tokens") != null ? (Integer) usage.get("prompt_tokens") : 0;
                    completionTokens = usage.get("completion_tokens") != null ? (Integer) usage.get("completion_tokens") : 0;
                }
            }

            // 9. 记录成功执行日志
            SkillExecutionLog log = new SkillExecutionLog();
            log.setSkillId(skill.getId());
            log.setSkillCode(skill.getCode());
            log.setModuleKey(skill.getModuleKey());
            log.setInputParams(objectMapper.writeValueAsString(params));
            log.setOutputResult(output);
            log.setTokensPrompt(promptTokens);
            log.setTokensCompletion(completionTokens);
            log.setDurationMs((int) durationMs);
            log.setStatus("success");
            log.setCreatedBy(userId);
            logMapper.insert(log);

            // 10. 增加技能使用次数
            skillMapper.incrementUseCount(skill.getId());

            // 11. 组装响应
            response.setSkillId(skill.getId());
            response.setSkillCode(skill.getCode());
            response.setOutput(output);
            response.setTokensPrompt(promptTokens);
            response.setTokensCompletion(completionTokens);
            response.setDurationMs((int) durationMs);
            response.setLogId(log.getId());

            return response;

        } catch (Exception e) {
            // 12. 记录失败执行日志
            long durationMs = System.currentTimeMillis() - startTime;

            SkillExecutionLog log = new SkillExecutionLog();
            log.setSkillId(skill.getId());
            log.setSkillCode(skill.getCode());
            log.setModuleKey(skill.getModuleKey());
            log.setInputParams(params.toString());
            log.setStatus("fail");
            log.setErrorMessage(e.getMessage());
            log.setDurationMs((int) durationMs);
            log.setCreatedBy(userId);
            logMapper.insert(log);

            throw new RuntimeException("AI 技能执行失败: " + e.getMessage(), e);
        }
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

    /**
     * 替换模板中的 {{variable}} 占位符
     * <p>支持 {{ variable }} 带空格格式，将匹配到的变量名从 params 中取值替换。</p>
     */
    private String replacePlaceholders(String template, Map<String, Object> params) {
        Pattern pattern = Pattern.compile("\\{\\{\\s*(\\w+)\\s*}}");
        Matcher matcher = pattern.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = params.get(key);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
