package org.seaPack.controller.ai;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.ai.AiDialogRequest;
import org.seaPack.service.ai.AiDialogService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.Executor;

/**
 * 统一 AI 对话控制器
 * <p>提供 4 种对话模式的统一管理：流式 LLM、非流式 LLM、Agent 流式、编排，
 * 以及统一的取消功能。</p>
 *
 * <pre>
 * POST /ai/dialog/stream         流式 LLM 对话（streaming_llm）
 * POST /ai/dialog/chat           非流式 LLM 对话（llm_chat）
 * POST /ai/dialog/agent-stream   Agent 流式对话（agent_stream）
 * POST /ai/dialog/orchestration  编排对话（orchestration）
 * POST /ai/dialog/cancel         取消所有正在进行的对话
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/ai/dialog")
@CrossOrigin(origins = "${cors.allowed-origins:*}")
@RequiredArgsConstructor
public class AiDialogController {

    private final AiDialogService dialogService;
    private final Executor sseExecutor;

    // ========================================================================
    //  流式 LLM 对话
    // ========================================================================

    /**
     * 流式 LLM 对话（SSE 流式返回，含 token 统计和执行记录）
     *
     * @param request  统一对话请求（mode=streaming_llm）
     * @param response HTTP 响应对象
     * @return SSE 发射器
     */
    @PostMapping("/stream")
    public SseEmitter stream(@RequestBody AiDialogRequest request,
                             @RequestHeader("Authorization") String authHeader,
                             HttpServletResponse response) {
        // 设置 SSE 响应头
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");

        SseEmitter emitter = new SseEmitter(600000L);
        Long userId = getCurrentUserId();

        // 使用公共线程池异步执行
        sseExecutor.execute(() -> {
            try {
                dialogService.handleStream(request, userId, null, emitter, response);
            } catch (Exception e) {
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                }
            }
        });

        registerEmitterCallbacks(emitter);
        return emitter;
    }

    // ========================================================================
    //  非流式 LLM 对话
    // ========================================================================

    /**
     * 非流式 LLM 对话（同步返回完整结果）
     *
     * @param request 统一对话请求（mode=llm_chat）
     * @return JSON 响应（含 content / tokensPrompt / tokensCompletion / model）
     */
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody AiDialogRequest request) {
        try {
            Long userId = getCurrentUserId();
            Object result = dialogService.handleSync(request, userId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========================================================================
    //  Agent 流式对话
    // ========================================================================

    /**
     * Agent 流式对话（SSE 流式返回，含链路追踪）
     *
     * @param request   统一对话请求（mode=agent_stream）
     * @param authHeader Authorization 请求头
     * @param response  HTTP 响应对象
     * @return SSE 发射器
     */
    @PostMapping("/agent-stream")
    public SseEmitter agentStream(@RequestBody AiDialogRequest request,
                                   @RequestHeader("Authorization") String authHeader,
                                   HttpServletResponse response) {
        // 设置 SSE 响应头
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");

        SseEmitter emitter = new SseEmitter(600000L);
        Long userId = getCurrentUserId();

        // 使用公共线程池异步执行
        sseExecutor.execute(() -> {
            try {
                dialogService.handleStream(request, userId, authHeader, emitter, response);
            } catch (Exception e) {
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                }
            }
        });

        registerEmitterCallbacks(emitter);
        return emitter;
    }

    // ========================================================================
    //  编排对话
    // ========================================================================

    /**
     * 编排对话（SSE 流式返回，含 4 级降级策略）
     * <pre>
     * 优先级  条件                执行方式
     * 1      有编排 + 有步骤      编排执行
     * 2      有编排 + 无步骤      降级到场景 Agent
     * 3      无编排 + 有 Agent    Agent 对话
     * 4      无编排 + 无 Agent    通用 LLM 对话
     * </pre>
     *
     * @param request  统一对话请求（mode=orchestration）
     * @param authHeader Authorization 请求头
     * @param response HTTP 响应对象
     * @return SSE 发射器
     */
    @PostMapping("/orchestration")
    public SseEmitter orchestration(@RequestBody AiDialogRequest request,
                                    @RequestHeader("Authorization") String authHeader,
                                    HttpServletResponse response) {
        // 设置 SSE 响应头
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");

        SseEmitter emitter = new SseEmitter(600000L);
        Long userId = getCurrentUserId();

        // 使用公共线程池异步执行
        sseExecutor.execute(() -> {
            try {
                dialogService.handleStream(request, userId, authHeader, emitter, response);
            } catch (Exception e) {
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                }
            }
        });

        registerEmitterCallbacks(emitter);
        return emitter;
    }

    // ========================================================================
    //  执行记录查询
    // ========================================================================

    /**
     * 按消息ID查询单条执行记录（点击消息气泡查看完整链路）
     * <p>request_id 为前端生成的每轮唯一消息ID。</p>
     */
    @GetMapping("/sessions/request/{requestId}")
    public ResponseEntity<?> sessionByRequestId(@PathVariable String requestId) {
        Object session = dialogService.getSessionByRequestId(requestId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    /**
     * 按对话ID查询该会话的所有轮次（对话历史回显，按时间升序）
     * <p>conversation_id 为进入对话界面时生成，同一会话的所有轮次共享。</p>
     */
    @GetMapping("/sessions/conversation/{conversationId}")
    public ResponseEntity<?> sessionsByConversationId(@PathVariable String conversationId) {
        return ResponseEntity.ok(dialogService.getSessionsByConversationId(conversationId));
    }

    // ========================================================================
    //  取消对话
    // ========================================================================

    /**
     * 取消所有正在进行的 AI 对话
     * <p>通知后端 AI 服务线程优雅终止流式对话。</p>
     *
     * @return 204 No Content
     */
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel() {
        Long userId = getCurrentUserId();
        log.info("用户请求终止所有对话, userId={}", userId);
        dialogService.cancelStream(userId);
        return ResponseEntity.ok().build();
    }

    // ========================================================================
    //  辅助方法
    // ========================================================================

    /**
     * 注册 SSE 发射器的生命周期回调
     */
    private void registerEmitterCallbacks(SseEmitter emitter) {
        emitter.onCompletion(() -> log.debug("SSE 连接正常关闭"));
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时");
            try { emitter.complete(); } catch (Exception ignored) {}
        });
        emitter.onError((e) -> log.error("SSE 连接发生错误", e));
    }

    /**
     * 从 SecurityContext 中获取当前登录用户 ID
     *
     * @return 用户 ID，未登录返回 null
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
