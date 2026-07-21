package org.seaPack.service.ai;

import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.components.Assistant;
import org.seaPack.service.common.ProgressService;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Agent 异步任务服务
 * <p>负责 SSE 流式任务的异步执行，供 AgentController 调用。</p>
 */
@Slf4j
@Service
public class AgentAsyncService {

    /**
     * 异步执行 Agent 任务（SSE 流式）
     * <p>根据任务内容自动判断走流式/非流式模式。</p>
     */
    @Async
    public void executeAgentTaskAsync(String task, Assistant assistant,
                                       ResponseBodyEmitter emitter, AtomicBoolean isCompleted,
                                       ProgressService progressService) {
        try {
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
                handleNonStreaming(task, assistant, emitter, isCompleted);
            } else {
                try {
                    handleStreaming(task, assistant, emitter, isCompleted);
                } catch (IllegalArgumentException e) {
                    if (e.getMessage() != null && e.getMessage().contains("Tools are currently not supported")) {
                        log.warn("检测到不支持流式工具，自动降级为非流式模式...");
                        safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"message\": \"当前模型不支持流式生成，已自动切换为普通模式...\"}\n\n");
                        handleNonStreaming(task, assistant, emitter, isCompleted);
                    } else {
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            safeSend(emitter, isCompleted, "data: {\"status\": \"error\", \"message\": \"任务执行失败，请稍后重试\"}\n\n");
            emitter.completeWithError(e);
        } finally {
            progressService.clearContext();
            if (!isCompleted.get()) {
                emitter.complete();
            }
        }
    }

    /**
     * 安全发送 SSE 数据，连接断开时自动取消
     */
    public void safeSend(ResponseBodyEmitter emitter, AtomicBoolean isCompleted, String data) {
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
     * 非流式模式：调用带工具的 AI Agent，等待完整结果后一次性返回
     */
    private void handleNonStreaming(String task, Assistant assistant,
                                     ResponseBodyEmitter emitter, AtomicBoolean isCompleted) {
        try {
            safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"message\": \"正在调用工具处理...\"}\n\n");
            String finalResult = assistant.chat(task);
            safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"message\": \"" + escapeJson(finalResult) + "\"}\n\n");
            safeSend(emitter, isCompleted, "data: {\"status\": \"complete\", \"message\": \"任务完成\"}\n\n");
            if (!isCompleted.get()) emitter.complete();
        } catch (Exception e) {
            safeSend(emitter, isCompleted, "data: {\"status\": \"error\", \"message\": \"处理失败，请稍后重试\"}\n\n");
        }
    }

    /**
     * 流式模式：逐 token 推送 AI 回复
     */
    private void handleStreaming(String task, Assistant assistant,
                                  ResponseBodyEmitter emitter, AtomicBoolean isCompleted) {
        TokenStream tokenStream = assistant.chatStream(task);

        tokenStream.onNext(token -> {
            if (token != null && !token.trim().isEmpty()) {
                safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"text\": \"" + escapeJson(token) + "\"}\n\n");
            }
        });

        tokenStream.onComplete(response -> {
            safeSend(emitter, isCompleted, "data: {\"status\": \"complete\", \"message\": \"任务完成\"}\n\n");
            if (!isCompleted.get()) emitter.complete();
        });

        tokenStream.onError(throwable -> {
            safeSend(emitter, isCompleted, "data: {\"status\": \"error\", \"message\": \"流式处理失败，请稍后重试\"}\n\n");
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

    /**
     * 转义 JSON 特殊字符
     */
    public String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
