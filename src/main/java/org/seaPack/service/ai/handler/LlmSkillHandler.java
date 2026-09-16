package org.seaPack.service.ai.handler;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.ai.Skill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大模型技能执行器
 * <p>调用 LLM（如 DeepSeek、通义千问等）执行对话/生成任务。</p>
 * <p>参数约定：</p>
 * <ul>
 *   <li>prompt (String, 必填): 用户提示词/指令</li>
 *   <li>system_prompt (String, 可选): 系统提示词，覆盖默认</li>
 *   <li>temperature (Number, 可选): 温度参数</li>
 * </ul>
 */
@Slf4j
@Component
public class LlmSkillHandler implements SkillHandler {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Override
    public String getSkillType() {
        return "llm";
    }

    @Override
    public SkillExecutionResult execute(Skill skill, Map<String, Object> params,
                                         String authToken, SseEmitter emitter) {
        long start = System.currentTimeMillis();

        // 提取参数
        String prompt = getParam(params, "prompt", "");
        String systemPrompt = getParam(params, "system_prompt",
                "你是一个专业的AI助手，请根据用户的需求提供准确、详细的回答。");

        if (prompt.isBlank()) {
            return SkillExecutionResult.json(400, "LLM", skill.getEndpoint() != null ? skill.getEndpoint() : skill.getCode(),
                    System.currentTimeMillis() - start, Map.of("error", "prompt 参数不能为空"));
        }

        // 构建消息列表
        List<ChatMessage> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new SystemMessage(systemPrompt));
        }
        messages.add(new UserMessage(prompt));

        // 调用 LLM
        log.info("技能[{}] 调用 LLM: prompt长度={}", skill.getName(), prompt.length());
        var response = chatLanguageModel.generate(messages);
        String responseText = response.content().text();

        long durationMs = System.currentTimeMillis() - start;
        log.info("技能[{}] LLM 响应完成: 耗时={}ms, 响应长度={}", skill.getName(), durationMs, responseText.length());

        String endpoint = skill.getEndpoint() != null ? skill.getEndpoint() : "LLM:" + skill.getCode();
        return SkillExecutionResult.streamText("LLM", endpoint, durationMs, responseText);
    }

    private String getParam(Map<String, Object> params, String key, String defaultValue) {
        if (params == null) return defaultValue;
        Object val = params.get(key);
        return val != null ? val.toString() : defaultValue;
    }
}
