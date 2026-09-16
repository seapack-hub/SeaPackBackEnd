package org.seaPack.service.ai.handler;

import org.seaPack.model.ai.Skill;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * 技能执行器策略接口
 * <p>每种 skill_type 对应一个 Handler 实现，通过 Spring 自动注册。</p>
 */
public interface SkillHandler {

    /**
     * 返回此 Handler 支持的 skill_type 值
     */
    String getSkillType();

    /**
     * 执行技能
     *
     * @param skill     技能配置
     * @param params    用户输入参数
     * @param authToken 认证 Token（内部 API 调用时需要）
     * @param emitter   SSE 发射器（可用于流式推送进度）
     * @return 执行结果
     */
    SkillExecutionResult execute(Skill skill, Map<String, Object> params,
                                 String authToken, SseEmitter emitter);
}
