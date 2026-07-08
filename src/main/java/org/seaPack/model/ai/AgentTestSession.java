package org.seaPack.model.ai;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * Agent 测试会话实体
 * <p>对应 ai_agent_test_session 表，记录每次测试对话的输入输出及完整调用链路追踪。</p>
 */
@Entity
@Data
@Table(name = "ai_agent_test_session")
public class AgentTestSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键")
    private Long id;

    @Column(name = "agent_id")
    @Comment("测试的 Agent ID")
    private Long agentId;

    @Column(name = "agent_name")
    @Comment("Agent 名称（冗余）")
    private String agentName;

    @Column(name = "user_message", columnDefinition = "TEXT")
    @Comment("用户输入消息")
    private String userMessage;

    @Column(name = "history_messages", columnDefinition = "JSON")
    @Comment("对话历史（JSON数组）")
    private String historyMessages;

    @Column(name = "agent_reply", columnDefinition = "TEXT")
    @Comment("Agent 回复内容")
    private String agentReply;

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

    @Column(name = "model_name")
    @Comment("使用的模型")
    private String modelName;

    @Column(name = "status")
    @Comment("状态：success/fail/timeout")
    private String status;

    @Column(name = "error_message")
    @Comment("错误信息")
    private String errorMessage;

    @Column(name = "created_by")
    @Comment("测试人")
    private Long createdBy;

    @Column(name = "created_at")
    @Comment("测试时间")
    private Date createdAt;
}
