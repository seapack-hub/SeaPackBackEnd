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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        // 提取 choices
        List<Map<String, Object>> choices =
                (List<Map<String, Object>>) chunk.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> choice = choices.get(0);

            // 提取 finish_reason
            if (choice.containsKey("finish_reason") && choice.get("finish_reason") != null) {
                result.setFinishReason(choice.get("finish_reason").toString());
            }

            // 提取 delta
            Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
            if (delta != null) {
                // 提取 content
                if (delta.get("content") != null) {
                    result.setDeltaContent(delta.get("content").toString());
                }
                // 提取 tool_calls（流式增量）
                if (delta.containsKey("tool_calls") && delta.get("tool_calls") != null) {
                    List<Map<String, Object>> toolCallsDelta =
                            (List<Map<String, Object>>) delta.get("tool_calls");
                    result.setToolCallsDelta(toolCallsDelta);
                }
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

        // 如果既没有 delta content 也没有 usage 也没有 tool_calls，视为无效 chunk
        if (result.getDeltaContent() == null && result.getPromptTokens() == null
                && result.getToolCallsDelta() == null) {
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
        /** 完成原因：stop / tool_calls / length 等 */
        private String finishReason;
        /** 本次增量的 tool_calls 片段（流式累积用） */
        private List<Map<String, Object>> toolCallsDelta;

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

        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }

        public void setToolCallsDelta(List<Map<String, Object>> toolCallsDelta) {
            this.toolCallsDelta = toolCallsDelta;
        }

        public boolean hasDeltaContent() {
            return deltaContent != null && !deltaContent.isEmpty();
        }

        public boolean hasUsage() {
            return promptTokens != null || completionTokens != null;
        }

        public boolean hasToolCalls() {
            return toolCallsDelta != null && !toolCallsDelta.isEmpty();
        }

        /** 是否为工具调用完成（finish_reason = tool_calls） */
        public boolean isToolCallFinish() {
            return "tool_calls".equals(finishReason);
        }
    }

    /**
     * 流式 tool_calls 累积器
     * <p>OpenAI 流式响应中 tool_calls 是增量发送的（每次只返回一部分 arguments），
     * 需要按 index 累积拼接才能得到完整的 function call。</p>
     */
    public static class ToolCallAccumulator {
        private final Map<Integer, Map<String, Object>> accumulated = new HashMap<>();
        private final Map<Integer, StringBuilder> argumentsBuffers = new HashMap<>();

        /**
         * 累积一次增量的 tool_calls 片段
         */
        public void addDelta(List<Map<String, Object>> deltas) {
            if (deltas == null) return;
            for (Map<String, Object> delta : deltas) {
                int index = delta.containsKey("index")
                        ? ((Number) delta.get("index")).intValue() : 0;
                log.debug("[ToolCallAccumulator] addDelta: index={}, id={}, type={}, function={}",
                        index, delta.get("id"), delta.get("type"),
                        delta.containsKey("function") ? ((Map<?, ?>) delta.get("function")).get("name") : null);

                // 首次出现该 index 时，保存 id 和 function 基础信息
                if (!accumulated.containsKey(index)) {
                    Map<String, Object> entry = new HashMap<>();
                    if (delta.containsKey("id") && delta.get("id") != null) {
                        entry.put("id", delta.get("id"));
                    }
                    if (delta.containsKey("type") && delta.get("type") != null) {
                        entry.put("type", delta.get("type"));
                    }
                    // function 基础信息（name 等）
                    if (delta.containsKey("function") && delta.get("function") != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> fnDelta = (Map<String, Object>) delta.get("function");
                        Map<String, Object> fnEntry = new HashMap<>();
                        if (fnDelta.containsKey("name") && fnDelta.get("name") != null) {
                            fnEntry.put("name", fnDelta.get("name"));
                        }
                        entry.put("function", fnEntry);
                    }
                    accumulated.put(index, entry);
                    argumentsBuffers.put(index, new StringBuilder());
                }

                // 累积 arguments 片段
                if (delta.containsKey("function") && delta.get("function") != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> fnDelta = (Map<String, Object>) delta.get("function");
                    if (fnDelta.containsKey("arguments") && fnDelta.get("arguments") != null) {
                        argumentsBuffers.get(index).append(fnDelta.get("arguments"));
                    }
                }
            }
        }

        /**
         * 获取累积完成的 tool_calls 列表（含完整的 arguments）
         */
        public List<Map<String, Object>> getToolCalls() {
            List<Map<String, Object>> result = new ArrayList<>();
            // 按 index 排序
            List<Integer> sortedIndexes = new ArrayList<>(accumulated.keySet());
            sortedIndexes.sort(Integer::compareTo);

            for (int idx : sortedIndexes) {
                Map<String, Object> entry = accumulated.get(idx);
                String arguments = argumentsBuffers.get(idx).toString();

                @SuppressWarnings("unchecked")
                Map<String, Object> function = (Map<String, Object>) entry.get("function");
                if (function == null) {
                    function = new HashMap<>();
                    entry.put("function", function);
                }
                function.put("arguments", arguments);

                result.add(entry);
            }
            return result;
        }

        /**
         * 是否累积到了任何 tool_calls
         */
        public boolean hasToolCalls() {
            return !accumulated.isEmpty();
        }
    }
}
