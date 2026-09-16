package org.seaPack.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.ai.SkillMapper;
import org.seaPack.mapper.ai.SkillParamMapper;
import org.seaPack.model.ai.Skill;
import org.seaPack.service.ai.handler.SkillExecutionResult;
import org.seaPack.service.ai.handler.SkillHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

/**
 * AI 技能服务
 * <p>提供技能的 CRUD、查询等基础操作。</p>
 */
@Slf4j
@Service
public class SkillService {

    @Autowired
    private SkillMapper skillMapper;

    @Autowired
    private SkillParamMapper paramMapper;

    /** Spring 自动收集所有 SkillHandler 实现，key 为 Bean 名称 */
    @Autowired
    private List<SkillHandler> handlerList;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** skillType → SkillHandler 快速查找表 */
    private Map<String, SkillHandler> handlerMap;

    @jakarta.annotation.PostConstruct
    public void initHandlerMap() {
        handlerMap = new HashMap<>();
        for (SkillHandler h : handlerList) {
            handlerMap.put(h.getSkillType(), h);
        }
        log.info("已注册技能执行器: {}", handlerMap.keySet());
    }

    /** 查询全量技能列表（不分页） */
    public List<Skill> getAll(Integer status) {
        return skillMapper.selectAll(status);
    }

    /** 分页查询技能列表 */
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

    /** 删除技能（级联删除参数） */
    @Transactional
    public int deleteById(Long id) {
        paramMapper.deleteBySkillId(id);
        return skillMapper.deleteById(id);
    }

    // ========================================================================
    //  技能调试（SSE 流式）
    // ========================================================================

    /**
     * 调试执行单个技能（SSE 流式返回）
     * <p>根据技能的 skillType 分发到对应的 Handler 执行。</p>
     */
    public void testSkillStream(Long skillId, Map<String, Object> params,
                                String authToken, SseEmitter emitter) {
        long start = System.currentTimeMillis();

        try {
            // 1. 查询技能
            sendSseEvent(emitter, "phase", Map.of("phase", "loading", "message", "正在加载技能配置..."));
            Skill skill = skillMapper.selectById(skillId);
            if (skill == null) {
                sendSseEvent(emitter, "error", Map.of("message", "技能不存在: id=" + skillId));
                emitter.complete();
                return;
            }

            // 2. 校验 handler 是否存在
            String skillType = skill.getSkillType() != null ? skill.getSkillType() : "http";
            SkillHandler handler = handlerMap.get(skillType);
            if (handler == null) {
                sendSseEvent(emitter, "error", Map.of("message",
                        "不支持的技能类型: " + skillType + "，可用类型: " + handlerMap.keySet()));
                emitter.complete();
                return;
            }

            // 3. http 类型需要校验 endpoint
            if ("http".equals(skillType)
                    && (skill.getEndpoint() == null || skill.getEndpoint().isBlank())) {
                sendSseEvent(emitter, "error", Map.of("message",
                        "技能 [" + skill.getName() + "] 未配置 endpoint"));
                emitter.complete();
                return;
            }

            // 4. 执行
            sendSseEvent(emitter, "phase", Map.of("phase", "calling",
                    "message", "正在通过 " + skillType.toUpperCase() + " 执行器调用..."));

            SkillExecutionResult result = handler.execute(skill, params, authToken, emitter);

            // 5. 发送结果
            Map<String, Object> resultMap = new LinkedHashMap<>();
            resultMap.put("type", "result");
            resultMap.put("outputType", result.getOutputType());
            resultMap.put("statusCode", result.getStatusCode());
            resultMap.put("httpMethod", result.getHttpMethod());
            resultMap.put("url", result.getUrl());
            resultMap.put("durationMs", result.getDurationMs());
            resultMap.put("body", result.getBody());

            emitter.send(SseEmitter.event().name("message")
                    .data(objectMapper.writeValueAsString(resultMap), MediaType.APPLICATION_JSON));

            log.info("技能调试[{}] 完成: type={}, status={}, method={}, duration={}ms",
                    skill.getName(), skillType, result.getStatusCode(), result.getHttpMethod(), result.getDurationMs());

        } catch (Exception e) {
            log.error("技能调试异常: {}", e.getMessage(), e);
            sendSseEvent(emitter, "error", Map.of("message", "执行异常: " + e.getMessage()));
        } finally {
            try { emitter.complete(); } catch (Exception ignored) {}
        }
    }

    private void sendSseEvent(SseEmitter emitter, String type, Map<String, Object> data) {
        try {
            Map<String, Object> event = new HashMap<>(data);
            event.put("type", type);
            emitter.send(SseEmitter.event().name("message")
                    .data(objectMapper.writeValueAsString(event), MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            log.warn("发送SSE事件失败: {}", e.getMessage());
        }
    }
}
