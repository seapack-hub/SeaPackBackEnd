package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * AI 执行会话记录实体
 * <p>对应 ai_execution_session 表，通用的 AI 执行记录，支持 Agent、Skill、Prompt、Scene、Knowledge 等多种业务类型。</p>
 */
@Entity
@Data
@Table(name = "ai_execution_session")
public class ExecutionSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键")
    private Long id;

    @Column(name = "biz_type", nullable = false)
    @Comment("业务类型：agent/skill/prompt/scene/knowledge")
    private String bizType;

    @Column(name = "biz_id", nullable = false)
    @Comment("业务ID（关联具体实体）")
    private Long bizId;

    @Column(name = "biz_name")
    @Comment("业务名称（冗余，方便查询）")
    private String bizName;

    @Column(name = "session_id")
    @Comment("会话ID，用于关联多轮对话")
    private String sessionId;

    @Column(name = "request_id")
    @Comment("外部请求幂等键，防止重试导致的重复记录/扣费")
    private String requestId;

    @Column(name = "retry_count")
    @Comment("重试次数")
    private Integer retryCount;

    @Column(name = "user_message", columnDefinition = "TEXT")
    @Comment("用户输入消息")
    private String userMessage;

    @Column(name = "history_messages", columnDefinition = "JSON")
    @Comment("对话历史（JSON数组）")
    private String historyMessages;

    @Column(name = "output_result", columnDefinition = "TEXT")
    @Comment("输出结果")
    private String outputResult;

    @Column(name = "trace_snapshot", columnDefinition = "JSON")
    @Comment("完整调用链路快照")
    private String traceSnapshot;

    @Column(name = "total_duration_ms")
    @Comment("总耗时（毫秒）")
    private Integer totalDurationMs;

    @Column(name = "tokens_prompt")
    @Comment("提示词 Token 数")
    private Integer tokensPrompt;

    @Column(name = "tokens_completion")
    @Comment("补全 Token 数")
    private Integer tokensCompletion;

    @Column(name = "tokens_total")
    @Comment("总 Token 数")
    private Integer tokensTotal;

    @Column(name = "model_name")
    @Comment("使用的模型")
    private String modelName;

    @Column(name = "status")
    @Comment("状态：success/fail/timeout")
    private String status;

    @Column(name = "is_deleted")
    @Comment("逻辑删除：0-未删除，1-已删除")
    private Integer isDeleted;

    @Column(name = "error_message", columnDefinition = "TEXT")
    @Comment("错误信息")
    private String errorMessage;

    @Column(name = "created_by")
    @Comment("操作人")
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("操作时间")
    private Date createdAt;
}
