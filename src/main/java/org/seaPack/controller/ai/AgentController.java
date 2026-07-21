package org.seaPack.controller.ai;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.components.Assistant;
import org.seaPack.dto.ai.AgentContext;
import org.seaPack.service.ai.AgentAsyncService;
import org.seaPack.service.common.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI 智能体控制器
 * <p>通过 SSE 流式执行 AI Agent 任务，根据任务类型自动选择流式/非流式模式，
 * 支持任务进度推送与工具调用。</p>
 */
@RestController
@RequestMapping("/agent")
@Slf4j
@CrossOrigin(origins = "${cors.allowed-origins:*}")
public class AgentController {

    private final Assistant assistant;
    private final ProgressService progressService;
    private final AgentAsyncService agentAsyncService;

    @Autowired
    public AgentController(Assistant assistant, ProgressService progressService, AgentAsyncService agentAsyncService) {
        this.assistant = assistant;
        this.progressService = progressService;
        this.agentAsyncService = agentAsyncService;
    }

    /**
     * 运行 AI Agent 任务（SSE 流式返回）
     * <p>根据任务内容自动判断是否需要工具调用：
     * 需要文件/报告等生成能力时走非流式模式，纯对话走流式模式。
     * 流式模式若不支持工具调用则自动降级。</p>
     * @param task     任务描述文本
     * @param response HTTP 响应对象
     * @return SSE 事件流发射器
     */
    @GetMapping(value="/run-agent")
    public ResponseBodyEmitter runAgent(@RequestParam String task, HttpServletResponse response) {

        response.setContentType("text/event-stream"); // 设置 SSE 内容类型
        response.setCharacterEncoding("UTF-8"); // 使用 UTF-8 编码
        response.setHeader("Cache-Control", "no-cache"); // 禁止客户端缓存 SSE 数据
        response.setHeader("Connection", "keep-alive"); // 保持长连接

        ResponseBodyEmitter emitter = new ResponseBodyEmitter(300000L); // 创建 SSE 发射器，超时 5 分钟

        AtomicBoolean isCompleted = new AtomicBoolean(false); // 线程安全的完成状态标记

        AgentContext context = new AgentContext(emitter, isCompleted); // 创建 Agent 上下文
        progressService.setContext(context); // 将上下文注册到进度服务，供工具调用时推送进度

        emitter.onCompletion(() -> { // 注册 SSE 正常完成回调
            isCompleted.set(true); // 标记任务完成
            progressService.clearContext(); // 清除进度上下文
            log.info("SSE 连接正常关闭");
        });

        emitter.onError((e) -> { // 注册 SSE 异常回调
            isCompleted.set(true); // 标记任务完成
            progressService.clearContext(); // 清除进度上下文
            log.error("SSE 连接发生错误", e);
        });

        // 使用 Spring @Async 替代原始 Thread，由线程池管理
        agentAsyncService.executeAgentTaskAsync(task, assistant, emitter, isCompleted, progressService);
        return emitter; // 返回 SSE 发射器给客户端
    }
}