package org.seaPack.controller;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.TokenStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.components.Assistant;
import org.seaPack.dto.AgentContext;
import org.seaPack.service.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/agent")
@Slf4j
@CrossOrigin(origins = "*")
public class AgentController {

    private final Assistant assistant;
    private final ProgressService progressService; // 注入服务

    // 直接注入被 @AiService 注解的接口，Spring 会自动完成所有底层组装
    @Autowired
    public AgentController(Assistant assistant, ProgressService progressService) {
        this.assistant = assistant;
        this.progressService = progressService;
    }

    /**
     * agent智能体入口
     * @param task
     * @param response
     * @return
     */
    @GetMapping(value="/run-agent")
    public ResponseBodyEmitter runAgent(@RequestParam String task, HttpServletResponse response) {

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        // 禁用缓存，防止浏览器缓存流数据
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        // 1. 创建一个 emitter，显式设置超时时间（5分钟 = 300000毫秒），避免连接无限期挂起
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(300000L);

        // 2.使用 AtomicBoolean 来手动维护连接的生命周期状态
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        // 1. 创建上下文
        AgentContext context = new AgentContext(emitter, isCompleted);
        // 2. 绑定到当前线程（Service 层会用到）
        progressService.setContext(context);

        // 3. 注册连接关闭后的清理回调，一旦触发，将状态标记为 true
        emitter.onCompletion(() -> {
            isCompleted.set(true);
            progressService.clearContext(); // 清理
            log.info("🔗 SSE 连接正常关闭");
        });

        emitter.onError((e) -> {
            isCompleted.set(true);
            progressService.clearContext(); // 清理
            // 如果是连接中止的异常，打印 warn；其他未知异常打印 error
            log.error("连接发生错误", e);
        });

        new Thread(() -> {
            try{
                // 4. 使用安全发送方法发送开始信号
                safeSend(emitter, isCompleted, "data: {\"status\": \"start\", \"message\": \"事件: 任务开始...\"}\n\n");
                safeSend(emitter, isCompleted, "data: {\"status\": \"start\", \"message\": \"状态: 正在理解需求...\"}\n\n");

                // 简单判断：如果任务包含“生成”、“文件”、“报告”等关键词，使用非流式
                // 1. 增强关键词判断：只要包含这些词，一律走非流式（因为流式不支持工具）
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
                    // --- 模式 A：显式的非流式调用 ---
                    handleNonStreaming(task, emitter, isCompleted);
                }else{
                    // --- 模式 B：尝试流式调用 ---
                    try {
                        handleStreaming(task, emitter, isCompleted);
                    } catch (IllegalArgumentException e) {
                        // 2. 捕获不支持流式工具的异常
                        if (e.getMessage() != null && e.getMessage().contains("Tools are currently not supported")) {
                            log.warn("⚠️ 检测到不支持流式工具，自动降级为非流式模式...");
                            safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"message\": \"当前模型不支持流式生成，已自动切换为普通模式...\"}\n\n");

                            // 3. 关键修改：直接调用非流式处理方法，不要重新进入 if/else 逻辑
                            handleNonStreaming(task, emitter, isCompleted);
                        } else {
                            throw e; // 其他异常继续抛出
                        }
                    }
                }
            } catch (Exception e) {
                // 捕获所有未处理的异常并发送给前端
                safeSend(emitter, isCompleted, "data: {\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}\n\n");
                emitter.completeWithError(e);
            } finally {
                // 4. 【关键】任务结束后清理上下文，防止内存泄漏和线程污染
                progressService.clearContext();
                if (!isCompleted.get()) {
                    emitter.complete();
                }
            };
        }).start();
        return emitter;
    }

    /**
     * 安全发送方法：在发送前检查自定义的连接状态标记
     * @param emitter
     * @param isCompleted
     * @param data
     */
    private void safeSend(ResponseBodyEmitter emitter, AtomicBoolean isCompleted, String data) {
        // 核心修改：检查我们自己维护的布尔值标记
        if (isCompleted.get()) {
            // 如果已经标记为完成，直接抛出取消异常，触发 tokenStream.onError
            throw new CancellationException("客户端连接已断开");
        }
        try {
            // 发送数据
            emitter.send(data, MediaType.TEXT_EVENT_STREAM);
        } catch (IOException e) {
            // 捕获 IO 异常（如连接被客户端强制重置）
            log.warn("客户端连接已断开，停止发送数据: {}", e.getMessage());
            // 发生 IO 异常说明连接已经断了，更新状态并尝试安全地标记为错误完成
            isCompleted.set(true);
            throw new CancellationException("客户端连接已断开");
        }
    }

    /**
     * 简单的 JSON 转义，防止文本中的引号破坏 JSON 结构
     * @param s
     * @return
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }


    /**
     * 处理非流式逻辑
     * @param task
     * @param emitter
     * @param isCompleted
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
     * 处理流式逻辑
     * @param task
     * @param emitter
     * @param isCompleted
     */
    private void handleStreaming(String task, ResponseBodyEmitter emitter, AtomicBoolean isCompleted) {
        TokenStream tokenStream = assistant.chatStream(task);

        tokenStream.onNext(token -> {
            if (token != null && !token.trim().isEmpty()) {
                safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"text\": \"" + escapeJson(token) + "\"}\n\n");
            }
        });

        tokenStream.onRetrieved(contents -> {
            // contents 包含了工具返回的所有信息
            StringBuilder sb = new StringBuilder();
            for (Content content : contents) {
                sb.append(content.textSegment().text());
            }
            // 向前端发送进度更新
            safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"text\": \"资料搜索中...\"}\n\n");
            safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"text\": \"" + sb.toString() + "\"}\n\n");
        });

        tokenStream.onComplete(response -> {
            safeSend(emitter, isCompleted, "data: {\"status\": \"complete\", \"message\": \"任务完成\"}\n\n");
            if (!isCompleted.get()) emitter.complete();
        });

        tokenStream.onError(throwable -> {
            //向前端发送错误信息
            safeSend(emitter, isCompleted, "data: {\"status\": \"error\", \"message\": \"" + throwable.getMessage() + "\"}\n\n");
            // 3. 核心修改：如果是被我们主动取消的异常，静默处理即可，不需要报错
            if (throwable instanceof CancellationException) {
                log.info("🛑 任务已被用户主动取消/连接已断开");
                emitter.complete();
                return;
            }
            if (!isCompleted.get()) {
                emitter.completeWithError(throwable);
            }
        });

        // 这里可能会抛出 IllegalArgumentException
        tokenStream.start();
    }
}
