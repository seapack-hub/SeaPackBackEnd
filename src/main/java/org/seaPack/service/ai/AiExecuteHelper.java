package org.seaPack.service.ai;

import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.AiExecuteResult;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

/**
 * AI 执行公共工具类
 * <p>提供模板变量替换和 LLM API 调用等通用方法，供 SkillService、PromptTemplateService 等复用。</p>
 */
public class AiExecuteHelper {

    private AiExecuteHelper() {
    }

    /**
     * 替换模板中的 {{variable}} 占位符
     * <p>支持 {{ variable }} 带空格格式，将匹配到的变量名从 params 中取值替换。</p>
     *
     * @param template 含占位符的模板文本
     * @param params   变量键值对
     * @return 替换后的文本
     */
    public static String replacePlaceholders(String template, Map<String, Object> params) {
        if (template == null || params == null) {
            return template;
        }
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

    /**
     * 调用 LLM API（非流式）
     * <p>构建 OpenAI 兼容格式请求，发送到当前激活的 AI 提供商并解析响应。</p>
     *
     * @param filledPrompt   渲染后的完整 Prompt
     * @param temperature    温度参数（可为 null，使用默认值）
     * @param maxTokens      最大输出 Token 数（可为 null，使用默认值）
     * @param restTemplate   HTTP 客户端
     * @param aiProperties   AI 提供商配置
     * @return 执行结果（含输出内容、Token 统计、耗时）
     */
    public static AiExecuteResult callLLM(String filledPrompt,
                                           BigDecimal temperature,
                                           Integer maxTokens,
                                           RestTemplate restTemplate,
                                           AIProperties aiProperties) {
        long startTime = System.currentTimeMillis();

        // 获取 AI 提供商配置
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
        if (config == null) {
            throw new RuntimeException("AI 配置错误：未找到提供商 [" + providerName + "]");
        }

        // 构建请求体
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

        if (temperature != null) {
            requestBody.put("temperature", temperature);
        }
        if (maxTokens != null) {
            requestBody.put("max_tokens", maxTokens);
        }

        // 发送请求
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        Map<String, Object> apiResponse = restTemplate.postForObject(url, entity, Map.class);
        long durationMs = System.currentTimeMillis() - startTime;

        // 解析响应
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

        // 组装结果
        AiExecuteResult result = new AiExecuteResult();
        result.setRenderedPrompt(filledPrompt);
        result.setOutput(output);
        result.setTokensPrompt(promptTokens);
        result.setTokensCompletion(completionTokens);
        result.setDurationMs((int) durationMs);

        return result;
    }

    /**
     * 调用 LLM API（非流式，简化版）
     * <p>使用用户提供的 endpoint 和参数直接调用 LLM，适用于新版本 Skill。</p>
     *
     * @param endpoint       技能调用端点（API地址）
     * @param params         用户输入参数键值对
     * @param userMessage    用户补充消息（可为 null）
     * @param restTemplate   HTTP 客户端
     * @param aiProperties   AI 提供商配置
     * @return 执行结果（含输出内容、Token 统计、耗时）
     */
    public static AiExecuteResult callLLMWithParams(String endpoint,
                                                     Map<String, Object> params,
                                                     String userMessage,
                                                     RestTemplate restTemplate,
                                                     AIProperties aiProperties) {
        long startTime = System.currentTimeMillis();

        // 获取 AI 提供商配置
        String providerName = aiProperties.getActiveProvider();
        AIProperties.ProviderConfig config = aiProperties.getProviders().get(providerName);
        if (config == null) {
            throw new RuntimeException("AI 配置错误：未找到提供商 [" + providerName + "]");
        }

        // 构建请求体
        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getChatModel());

        // 构建用户消息内容
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");

        StringBuilder contentBuilder = new StringBuilder();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                contentBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        if (userMessage != null && !userMessage.isBlank()) {
            contentBuilder.append(userMessage);
        }
        userMsg.put("content", contentBuilder.toString().trim());
        messages.add(userMsg);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        // 发送请求
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        Map<String, Object> apiResponse = restTemplate.postForObject(url, entity, Map.class);
        long durationMs = System.currentTimeMillis() - startTime;

        // 解析响应
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

        // 组装结果
        AiExecuteResult result = new AiExecuteResult();
        result.setRenderedPrompt(contentBuilder.toString());
        result.setOutput(output);
        result.setTokensPrompt(promptTokens);
        result.setTokensCompletion(completionTokens);
        result.setDurationMs((int) durationMs);

        return result;
    }
}
