package org.seaPack.controller.ai;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.TokenStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.components.Assistant;
import org.seaPack.dto.ai.AgentContext;
import org.seaPack.service.common.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI 智能体控制器
 * <p>通过 SSE 流式执行 AI Agent 任务，根据任务类型自动选择流式/非流式模式，
 * 支持任务进度推送与工具调用。</p>
 */
@RestController
@RequestMapping("/agent")
@Slf4j
@CrossOrigin(origins = "*")
public class AgentController {

    private final Assistant assistant;
    private final ProgressService progressService;

    @Autowired
    public AgentController(Assistant assistant, ProgressService progressService) {
        this.assistant = assistant;
        this.progressService = progressService;
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

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        ResponseBodyEmitter emitter = new ResponseBodyEmitter(300000L);

        AtomicBoolean isCompleted = new AtomicBoolean(false);

        AgentContext context = new AgentContext(emitter, isCompleted);
        progressService.setContext(context);

        emitter.onCompletion(() -> {
            isCompleted.set(true);
            progressService.clearContext();
            log.info("SSE 连接正常关闭");
        });

        emitter.onError((e) -> {
            isCompleted.set(true);
            progressService.clearContext();
            log.error("SSE 连接发生错误", e);
        });

        new Thread(() -> {
            try{
                safeSend(emitter, isCompleted, "data: {\"status\": \"start\", \"message\": \"事件: 任务开始...\"}\n\n");
                safeSend(emitter, isCompleted, "data: {\"status\": \"start\", \"message\": \"状态: 正在理解需求...\"}\n\n");

                boolean needTool = task.contains("生成")
                        || task.contains("文件")
                        || task.contains("报告")
                        || task.contains("写")
                        || task.contains("表格")
                        || task.contains("Excel")
                        || task.contains("PDF")
                        || task.contains("整理")
                        || task.contains("文档");

                if (needTool) {
                    handleNonStreaming(task, emitter, isCompleted);
                }else{
                    try {
                        handleStreaming(task, emitter, isCompleted);
                    } catch (IllegalArgumentException e) {
                        if (e.getMessage() != null && e.getMessage().contains("Tools are currently not supported")) {
                            log.warn("检测到不支持流式工具，自动降级为非流式模式...");
                            safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"message\": \"当前模型不支持流式生成，已自动切换为普通模式...\"}\n\n");
                            handleNonStreaming(task, emitter, isCompleted);
                        } else {
                            throw e;
                        }
                    }
                }
            } catch (Exception e) {
                safeSend(emitter, isCompleted, "data: {\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}\n\n");
                emitter.completeWithError(e);
            } finally {
                progressService.clearContext();
                if (!isCompleted.get()) {
                    emitter.complete();
                }
            };
        }).start();
        return emitter;
    }

    /**
     * 安全发送 SSE 数据，连接断开时自动取消
     */
    private void safeSend(ResponseBodyEmitter emitter, AtomicBoolean isCompleted, String data) {
        if (isCompleted.get()) {
            throw new CancellationException("客户端连接已断开");
        }
        try {
            emitter.send(data, MediaType.TEXT_EVENT_STREAM);
        } catch (IOException e) {
            log.warn("客户端连接已断开，停止发送数据: {}", e.getMessage());
            isCompleted.set(true);
            throw new CancellationException("客户端连接已断开");
        }
    }

    /**
     * 转义 JSON 特殊字符
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /**
     * 非流式模式：调用带工具的 AI Agent，等待完整结果后一次性返回
     */
    private void handleNonStreaming(String task, ResponseBodyEmitter emitter, AtomicBoolean isCompleted) {
        try {
            safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"message\": \"正在调用工具处理...\"}\n\n");
            String finalResult = assistant.chat(task);
            safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"message\": \"" + escapeJson(finalResult) + "\"}\n\n");
            safeSend(emitter, isCompleted, "data: {\"status\": \"complete\", \"message\": \"任务完成\"}\n\n");
            if (!isCompleted.get()) emitter.complete();
        } catch (Exception e) {
            safeSend(emitter, isCompleted, "data: {\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}\n\n");
        }
    }

    /**
     * 流式模式：逐 token 推送 AI 回复，同时推送检索到的知识库内容
     */
    private void handleStreaming(String task, ResponseBodyEmitter emitter, AtomicBoolean isCompleted) {
        TokenStream tokenStream = assistant.chatStream(task);

        tokenStream.onNext(token -> {
            if (token != null && !token.trim().isEmpty()) {
                safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"text\": \"" + escapeJson(token) + "\"}\n\n");
            }
        });

        tokenStream.onRetrieved(contents -> {
            StringBuilder sb = new StringBuilder();
            for (Content content : contents) {
                sb.append(content.textSegment().text());
            }
            safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"text\": \"资料搜索中...\"}\n\n");
            safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"text\": \"" + sb.toString() + "\"}\n\n");
        });

        tokenStream.onComplete(response -> {
            safeSend(emitter, isCompleted, "data: {\"status\": \"complete\", \"message\": \"任务完成\"}\n\n");
            if (!isCompleted.get()) emitter.complete();
        });

        tokenStream.onError(throwable -> {
            safeSend(emitter, isCompleted, "data: {\"status\": \"error\", \"message\": \"" + throwable.getMessage() + "\"}\n\n");
            if (throwable instanceof CancellationException) {
                log.info("任务已被用户主动取消/连接已断开");
                emitter.complete();
                return;
            }
            if (!isCompleted.get()) {
                emitter.completeWithError(throwable);
            }
        });

        tokenStream.start();
    }
}