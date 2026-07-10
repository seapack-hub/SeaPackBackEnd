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
@CrossOrigin(origins = "${cors.allowed-origins:*}")
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

        new Thread(() -> { // 异步线程执行 AI 任务，不阻塞 HTTP 请求线程
            try{
                safeSend(emitter, isCompleted, "data: {\"status\": \"start\", \"message\": \"事件: 任务开始...\"}\n\n"); // 推送任务开始事件
                safeSend(emitter, isCompleted, "data: {\"status\": \"start\", \"message\": \"状态: 正在理解需求...\"}\n\n"); // 推送需求理解状态

                boolean needTool = task.contains("生成") // 判断任务是否需要调用工具
                        || task.contains("文件") // 包含"文件"关键词
                        || task.contains("报告") // 包含"报告"关键词
                        || task.contains("写") // 包含"写"关键词
                        || task.contains("表格") // 包含"表格"关键词
                        || task.contains("Excel") // 包含"Excel"关键词
                        || task.contains("PDF") // 包含"PDF"关键词
                        || task.contains("整理") // 包含"整理"关键词
                        || task.contains("文档"); // 包含"文档"关键词

                if (needTool) { // 需要工具调用时走非流式模式
                    handleNonStreaming(task, emitter, isCompleted);
                }else{ // 纯对话走流式模式
                    try {
                        handleStreaming(task, emitter, isCompleted);
                    } catch (IllegalArgumentException e) { // 捕获流式不支持工具的异常
                        if (e.getMessage() != null && e.getMessage().contains("Tools are currently not supported")) { // 确认为工具不支持
                            log.warn("检测到不支持流式工具，自动降级为非流式模式...");
                            safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"message\": \"当前模型不支持流式生成，已自动切换为普通模式...\"}\n\n"); // 通知客户端降级
                            handleNonStreaming(task, emitter, isCompleted); // 降级为非流式模式
                        } else { // 其他异常则向上抛出
                            throw e;
                        }
                    }
                }
            } catch (Exception e) { // 捕获所有未处理异常
                safeSend(emitter, isCompleted, "data: {\"status\": \"error\", \"message\": \"任务执行失败，请稍后重试\"}\n\n"); // 推送通用错误信息
                emitter.completeWithError(e); // 以异常状态结束 SSE
            } finally { // 无论成功或异常，最终清理
                progressService.clearContext(); // 清理进度上下文
                if (!isCompleted.get()) { // 如果尚未标记完成
                    emitter.complete(); // 正常结束 SSE 连接
                }
            };
        }).start(); // 启动异步线程
        return emitter; // 返回 SSE 发射器给客户端
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
            safeSend(emitter, isCompleted, "data: {\"status\": \"error\", \"message\": \"处理失败，请稍后重试\"}\n\n");
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
}