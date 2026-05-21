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
                boolean needTool = task.contains("生成") || task.contains("文件") || task.contains("报告") || task.contains("写");

                if (needTool) {
                    // --- 模式 A：非流式调用（支持工具） ---
                    log.info("检测到工具调用需求，切换到非流式模式...");
                    safeSend(emitter, isCompleted, "data: {\"status\": \"processing\", \"message\": \"正在调用工具处理...\"}\n\n");

                    // 直接调用非流式方法，这会阻塞直到工具执行完毕并返回最终结果
                    String finalResult = assistant.chat(task);

                    // 将最终结果发送给前端
                    safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"message\": \"" + escapeJson(finalResult) + "\"}\n\n");
                    safeSend(emitter, isCompleted, "data: {\"status\": \"complete\", \"message\": \"任务完成\"}\n\n");

                    emitter.complete();
                }else{
                    // 5. 启动流式对话
                    TokenStream tokenStream = assistant.chatStream(task);

                    // 6. 处理文本生成
                    tokenStream.onNext(token -> {
                        // 过滤掉空 token，防止前端报错
                        if (token != null && !token.trim().isEmpty()) {
                            safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"text\": \"" + escapeJson(token) + "\"}\n\n");
                        }
                    });

                    //7.监听工具执行情况
                    tokenStream.onRetrieved((contents) -> {
                        // contents 包含了工具返回的所有信息
                        // 你可以遍历它，或者直接提示前端“资料搜集完成”
                        StringBuilder sb = new StringBuilder();
                        for (Content content : contents) {
                            sb.append(content.textSegment().text());
                        }
                        // 向前端发送进度更新
                        safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"text\": \"资料搜索中...\"}\n\n");
                        safeSend(emitter, isCompleted, "data: {\"type\": \"content\", \"text\": \"" + sb.toString() + "\"}\n\n");
                    });

                    //8.任务完成
                    tokenStream.onComplete(responsed -> {
                        safeSend(emitter, isCompleted, "data: {\"status\": \"complete\", \"message\": \"任务完成\"}\n\n");
                        safeSend(emitter, isCompleted, "data: {\"status\": \"complete\", \"message\": \"文档已生成，准备下载。\"}\n\n");
                        // 正常结束连接
                        if (!isCompleted.get()) {
                            emitter.complete();
                        }
                    });

                    //6.错误异常
                    tokenStream.onError(throwable -> {
                        // 3. 核心修改：如果是被我们主动取消的异常，静默处理即可，不需要报错
                        if (throwable instanceof CancellationException) {
                            log.info("🛑 任务已被用户主动取消/连接已断开");
                            return;
                        }
                        if (!isCompleted.get()) {
                            safeSend(emitter, isCompleted, "data: {\"status\": \"error\", \"message\": \"" + throwable.getMessage() + "\"}\n\n");
                            emitter.completeWithError(throwable);
                        }
                    });

                    // 7. 启动流 (这一步最重要，不调用 start() 什么都不会发生)
                    tokenStream.start();
                }
            } catch (Exception e) {
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

    // 简单的 JSON 转义，防止文本中的引号破坏 JSON 结构
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
