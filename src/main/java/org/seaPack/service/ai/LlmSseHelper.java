package org.seaPack.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * LLM 流式调用工具类
 * <p>封装了通用的 HttpURLConnection 创建、SSE 流式读取、同步调用等逻辑，
 * 消除 LLMTestChatService / AgentTestChatService / OrchestrationExecuteService 中的重复代码。</p>
 */
@Slf4j
@Component
public class LlmSseHelper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建流式 HTTP POST 连接
     *
     * @param url        LLM API URL
     * @param apiKey     API 密钥
     * @param requestBody 请求体
     * @return 已写入请求体的 HttpURLConnection
     */
    public HttpURLConnection createConnection(String url, String apiKey, Map<String, Object> requestBody) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(300000); // 5 分钟读取超时

        byte[] body = objectMapper.writeValueAsBytes(requestBody);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(body);
            os.flush();
        }
        return connection;
    }

    /**
     * 流式读取 SSE 响应，逐 chunk 回调
     * <p>内部封装了 BufferedReader + while 循环 + cancelFlag 检查 + data: 行解析。</p>
     *
     * @param conn       HTTP 连接
     * @param cancelFlag 取消标志（可为 null）
     * @param onChunk    chunk 回调：每收到一个非 [DONE] 的 data 行即回调，
     *                   参数为 Chunk 对象（含 deltaContent / promptTokens / completionTokens / done 标记）
     */
    public void readChunks(HttpURLConnection conn, AtomicBoolean cancelFlag, Consumer<Chunk> onChunk) throws Exception {
        // 先检查 HTTP 响应码，非 200 时读取错误流并抛出异常
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String errorBody = new String(
                    conn.getErrorStream() != null
                            ? conn.getErrorStream().readAllBytes()
                            : new byte[0],
                    StandardCharsets.UTF_8);
            throw new RuntimeException("LLM API 返回错误: HTTP " + responseCode + ", body=" + errorBody);
        }

        long readStart = System.currentTimeMillis();
        int chunkCount = 0;
        log.info("LLM 流式连接建立成功: HTTP 200, 开始读取响应流");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 检查取消
                if (cancelFlag != null && cancelFlag.get()) {
                    log.info("LLM 流式调用被取消");
                    break;
                }

                // 兼容 "data: {...}" 与 "data:{...}"（无空格）两种格式
                if (line.startsWith("data:")) {
                    String data = line.substring(5).trim();
                    if (data.isEmpty()) {
                        continue;
                    }
                    if ("[DONE]".equals(data)) {
                        log.info("LLM 流式响应收到 [DONE]，结束读取");
                        Chunk doneChunk = new Chunk();
                        doneChunk.setDone(true);
                        onChunk.accept(doneChunk);
                        break;
                    }

                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> chunk = objectMapper.readValue(data, Map.class);
                        // 检测流式响应中的错误（OpenAI 兼容格式：data: {"error": {...}}）
                        if (chunk.containsKey("error")) {
                            throw new RuntimeException("LLM API 流式响应包含错误: " + data);
                        }
                        Chunk result = parseChunk(chunk);
                        if (result != null) {
                            chunkCount++;
                            onChunk.accept(result);
                        }
                    } catch (RuntimeException re) {
                        throw re;
                    } catch (Exception e) {
                        log.warn("解析 LLM 响应块失败: {}", e.getMessage());
                    }
                }
            }
        }
        log.info("LLM 流式响应读取结束: 有效chunk数={}, 耗时={}ms", chunkCount, System.currentTimeMillis() - readStart);
    }

    /**
     * 发起非流式同步调用（stream=false）
     *
     * @param url        LLM API URL
     * @param apiKey     API 密钥
     * @param requestBody 请求体
     * @return LLM API 的完整 JSON 响应
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> callSync(String url, String apiKey, Map<String, Object> requestBody) throws Exception {
        HttpURLConnection connection = createConnection(url, apiKey, requestBody);
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                String errorBody = new String(
                        connection.getErrorStream() != null
                                ? connection.getErrorStream().readAllBytes()
                                : new byte[0],
                        StandardCharsets.UTF_8);
                throw new RuntimeException("LLM API 返回错误: HTTP " + responseCode + ", body=" + errorBody);
            }
            byte[] responseBytes = connection.getInputStream().readAllBytes();
            return objectMapper.readValue(responseBytes, Map.class);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 解析单个 SSE data chunk
     *
     * @param chunk 反序列化的 JSON Map
     * @return Chunk 对象，若无有用数据返回 null
     */
    @SuppressWarnings("unchecked")
    private Chunk parseChunk(Map<String, Object> chunk) {
        Chunk result = new Chunk();

        // 提取 delta content
        java.util.List<Map<String, Object>> choices =
                (java.util.List<Map<String, Object>>) chunk.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> choice = choices.get(0);
            Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
            if (delta != null && delta.get("content") != null) {
                result.setDeltaContent(delta.get("content").toString());
            }
        }

        // 提取 usage
        Map<String, Object> usage = (Map<String, Object>) chunk.get("usage");
        if (usage != null) {
            result.setPromptTokens(usage.get("prompt_tokens") != null
                    ? ((Number) usage.get("prompt_tokens")).intValue() : null);
            result.setCompletionTokens(usage.get("completion_tokens") != null
                    ? ((Number) usage.get("completion_tokens")).intValue() : null);
        }

        // 如果既没有 delta content 也没有 usage，视为无效 chunk
        if (result.getDeltaContent() == null && result.getPromptTokens() == null) {
            return null;
        }
        return result;
    }

    /**
     * SSE 数据块，封装了一次 data: 行的解析结果
     */
    @Getter
    public static class Chunk {
        /** 本次增量内容（可为 null，如 usage chunk 无 delta） */
        private String deltaContent;
        /** 提示词 token 数（仅在最后一个 chunk 有值） */
        private Integer promptTokens;
        /** 补全 token 数（仅在最后一个 chunk 有值） */
        private Integer completionTokens;
        /** 是否收到 [DONE] 标记 */
        private boolean done;

        public void setDeltaContent(String deltaContent) {
            this.deltaContent = deltaContent;
        }

        public void setPromptTokens(Integer promptTokens) {
            this.promptTokens = promptTokens;
        }

        public void setCompletionTokens(Integer completionTokens) {
            this.completionTokens = completionTokens;
        }

        public void setDone(boolean done) {
            this.done = done;
        }

        public boolean hasDeltaContent() {
            return deltaContent != null && !deltaContent.isEmpty();
        }

        public boolean hasUsage() {
            return promptTokens != null || completionTokens != null;
        }
    }
}
