package org.seaPack.service.ai.handler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 技能执行统一结果
 * <p>所有 Handler 执行完成后返回此对象，由 SkillService 统一序列化为 SSE 事件。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillExecutionResult {

    /** 输出类型：json / file / stream_text / markdown */
    private String outputType;

    /** 状态码（HTTP 调用时为 HTTP 状态码，其他场景为 200 或自定义码） */
    private int statusCode;

    /** 请求方式描述（如 GET、POST、LLM、SCRIPT 等） */
    private String httpMethod;

    /** 请求的端点或标识 */
    private String url;

    /** 执行耗时（毫秒） */
    private long durationMs;

    /**
     * 响应数据（根据 outputType 不同而不同）
     * <ul>
     *   <li>json: Map&lt;String, Object&gt; 响应体</li>
     *   <li>file: Map 包含 url、fileName、fileSize</li>
     *   <li>stream_text / markdown: String 文本内容</li>
     * </ul>
     */
    private Object body;

    /** 便捷构建：JSON 类型结果 */
    public static SkillExecutionResult json(int statusCode, String method, String url,
                                            long durationMs, Object body) {
        return new SkillExecutionResult("json", statusCode, method, url, durationMs, body);
    }

    /** 便捷构建：文件类型结果 */
    public static SkillExecutionResult file(String url, String fileName, long fileSize, long durationMs) {
        Map<String, Object> body = Map.of("url", url, "fileName", fileName, "fileSize", fileSize);
        return new SkillExecutionResult("file", 200, "FILE", url, durationMs, body);
    }

    /** 便捷构建：流式文本结果 */
    public static SkillExecutionResult streamText(String method, String url, long durationMs, String text) {
        return new SkillExecutionResult("stream_text", 200, method, url, durationMs, text);
    }
}
