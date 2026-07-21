package org.seaPack.controller.ai;

import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.seaPack.dto.ai.*;
import org.seaPack.model.ai.*;
import org.seaPack.service.ai.AgentChatService;
import org.seaPack.service.ai.AgentService;
import org.seaPack.service.ai.AgentTestChatService;
import org.seaPack.service.ai.AgentTestSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

/**
 * AI Agent 管理控制器
 * <p>提供 Agent 的增删改查、复制、启停管理、关联管理（提示词模板/技能/知识库）及对话接口。</p>
 */
@Slf4j
@RestController
@RequestMapping("/ai/agents")
public class AgentManageController {

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentChatService agentChatService;

    @Autowired
    private AgentTestChatService agentTestChatService;

    @Autowired
    private AgentTestSessionService agentTestSessionService;

    // ===== Agent 主体 CRUD =====

    /** 分页查询 Agent 列表 */
    @GetMapping("/page/list")
    public PageInfo<Agent> pageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return agentService.getList(pageNum, pageSize, status, keyword);
    }

    /** 全量 Agent 列表（下拉选择用） */
    @GetMapping("/all")
    public List<Agent> all() {
        return agentService.getAll();
    }

    /** 查询 Agent 详情 */
    @GetMapping("/detail/{id}")
    public ResponseEntity<Agent> detail(@PathVariable Long id) {
        Agent agent = agentService.getById(id);
        if (agent == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(agent);
    }

    /** 新增 Agent */
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody Agent agent) {
        if (agentService.isCodeDuplicate(agent.getCode(), null)) {
            return ResponseEntity.badRequest().body("Agent 编码已存在: " + agent.getCode());
        }
        agent.setCreatedBy(getCurrentUserId());
        agentService.insert(agent);
        return ResponseEntity.ok(agent);
    }

    /** 编辑 Agent */
    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody Agent agent) {
        if (agent.getId() == null) {
            return ResponseEntity.badRequest().body("Agent ID 不能为空");
        }
        if (agent.getCode() != null && agentService.isCodeDuplicate(agent.getCode(), agent.getId())) {
            return ResponseEntity.badRequest().body("Agent 编码已存在: " + agent.getCode());
        }
        agentService.update(agent);
        return ResponseEntity.ok(agent);
    }

    /** 删除 Agent */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        agentService.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    /** 复制 Agent */
    @PostMapping("/copy/{id}")
    public ResponseEntity<?> copy(@PathVariable Long id) {
        try {
            Agent copy = agentService.copy(id);
            return ResponseEntity.ok(copy);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 启停切换 */
    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ResponseEntity.badRequest().body("状态值无效，仅支持 0（禁用）或 1（启用）");
        }
        agentService.updateStatus(id, status);
        return ResponseEntity.ok("操作成功");
    }

    /** 增加使用次数 */
    @PutMapping("/incrementUse/{id}")
    public ResponseEntity<?> incrementUse(@PathVariable Long id) {
        agentService.incrementUseCount(id);
        return ResponseEntity.ok("操作成功");
    }

    // ===== 关联管理：提示词模板 =====

    /** 获取 Agent 关联的提示词模板列表 */
    @GetMapping("/{agentId}/prompts")
    public List<AgentPrompt> getPrompts(@PathVariable Long agentId) {
        return agentService.getPrompts(agentId);
    }

    /** 添加关联提示词模板 */
    @PostMapping("/{agentId}/prompts")
    public ResponseEntity<?> addPrompt(@PathVariable Long agentId, @RequestBody AgentPrompt agentPrompt) {
        try {
            agentPrompt.setAgentId(agentId);
            agentService.addPrompt(agentPrompt);
            return ResponseEntity.ok(agentPrompt);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 更新关联提示词模板 */
    @PutMapping("/{agentId}/prompts/{id}")
    public ResponseEntity<?> updatePrompt(@PathVariable Long agentId, @PathVariable Long id, @RequestBody AgentPrompt agentPrompt) {
        agentPrompt.setId(id);
        agentPrompt.setAgentId(agentId);
        agentService.updatePrompt(agentPrompt);
        return ResponseEntity.ok(agentPrompt);
    }

    /** 删除关联提示词模板 */
    @DeleteMapping("/{agentId}/prompts/{id}")
    public ResponseEntity<?> deletePrompt(@PathVariable Long agentId, @PathVariable Long id) {
        agentService.deletePrompt(id);
        return ResponseEntity.ok().build();
    }

    // ===== 关联管理：技能 =====

    /** 获取 Agent 关联的技能列表 */
    @GetMapping("/{agentId}/skills")
    public List<AgentSkill> getSkills(@PathVariable Long agentId) {
        return agentService.getSkills(agentId);
    }

    /** 添加关联技能 */
    @PostMapping("/{agentId}/skills")
    public ResponseEntity<?> addSkill(@PathVariable Long agentId, @RequestBody AgentSkill agentSkill) {
        try {
            agentSkill.setAgentId(agentId);
            agentService.addSkill(agentSkill);
            return ResponseEntity.ok(agentSkill);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 更新关联技能 */
    @PutMapping("/{agentId}/skills/{id}")
    public ResponseEntity<?> updateSkill(@PathVariable Long agentId, @PathVariable Long id, @RequestBody AgentSkill agentSkill) {
        agentSkill.setId(id);
        agentSkill.setAgentId(agentId);
        agentService.updateSkill(agentSkill);
        return ResponseEntity.ok(agentSkill);
    }

    /** 删除关联技能 */
    @DeleteMapping("/{agentId}/skills/{id}")
    public ResponseEntity<?> deleteSkill(@PathVariable Long agentId, @PathVariable Long id) {
        agentService.deleteSkill(id);
        return ResponseEntity.ok().build();
    }

    // ===== 关联管理：知识库 =====

    /** 获取 Agent 关联的知识库列表 */
    @GetMapping("/{agentId}/knowledge")
    public List<AgentKnowledge> getKnowledgeList(@PathVariable Long agentId) {
        return agentService.getKnowledgeList(agentId);
    }

    /** 添加关联知识库 */
    @PostMapping("/{agentId}/knowledge")
    public ResponseEntity<?> addKnowledge(@PathVariable Long agentId, @RequestBody AgentKnowledge agentKnowledge) {
        try {
            agentKnowledge.setAgentId(agentId);
            agentService.addKnowledge(agentKnowledge);
            return ResponseEntity.ok(agentKnowledge);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 更新关联知识库 */
    @PutMapping("/{agentId}/knowledge/{id}")
    public ResponseEntity<?> updateKnowledge(@PathVariable Long agentId, @PathVariable Long id, @RequestBody AgentKnowledge agentKnowledge) {
        agentKnowledge.setId(id);
        agentKnowledge.setAgentId(agentId);
        agentService.updateKnowledge(agentKnowledge);
        return ResponseEntity.ok(agentKnowledge);
    }

    /** 删除关联知识库 */
    @DeleteMapping("/{agentId}/knowledge/{id}")
    public ResponseEntity<?> deleteKnowledge(@PathVariable Long agentId, @PathVariable Long id) {
        agentService.deleteKnowledge(id);
        return ResponseEntity.ok().build();
    }

    // ===== 测试对话 =====

    /** 发送对话消息 */
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody AgentChatRequest request) {
        try {
            AgentChatResponse response = agentChatService.chat(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 测试对话（SSE 流式返回，含链路追踪） */
    @PostMapping("/test-chat")
    public SseEmitter testChat(@RequestBody AgentTestChatRequest request,
                                @RequestHeader("Authorization") String authHeader,
                                HttpServletResponse response) {
        // 设置 SSE 响应头
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        // 创建 SSE 发射器，超时时间 10 分钟
        SseEmitter emitter = new SseEmitter(600000L);

        // 获取当前用户 ID
        Long userId = getCurrentUserId();

        // 使用线程池异步执行，避免阻塞 Servlet 线程
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                agentTestChatService.testChatStream(request, userId, emitter, authHeader);
            } catch (Exception e) {
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                }
            } finally {
                executor.shutdown();
            }
        });

        // 注册回调：客户端断开连接时
        emitter.onCompletion(() -> {
            log.info("SSE 连接正常关闭");
        });

        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时");
            emitter.complete();
        });

        emitter.onError((e) -> {
            log.error("SSE 连接发生错误", e);
        });

        return emitter;
    }

    /** 测试会话历史列表 */
    @GetMapping("/{agentId}/test-sessions")
    public PageInfo<ExecutionSession> getTestSessions(
            @PathVariable Long agentId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return agentTestSessionService.getTestSessions(agentId, pageNum, pageSize);
    }

    /** 测试会话详情 */
    @GetMapping("/{agentId}/test-sessions/{sessionId}")
    public ResponseEntity<ExecutionSession> getTestSessionDetail(
            @PathVariable Long agentId,
            @PathVariable Long sessionId) {
        ExecutionSession session = agentTestSessionService.getTestSessionDetail(agentId, sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    /** 删除测试会话 */
    @PostMapping("/{agentId}/test-sessions/delete/{sessionId}")
    public ResponseEntity<?> deleteTestSession(
            @PathVariable Long agentId,
            @PathVariable Long sessionId) {
        agentTestSessionService.deleteTestSession(agentId, sessionId);
        return ResponseEntity.ok("删除成功");
    }

    /**
     * 从 SecurityContext 中获取当前登录用户 ID
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
