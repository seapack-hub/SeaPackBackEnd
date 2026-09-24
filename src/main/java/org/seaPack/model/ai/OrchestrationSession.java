package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 编排执行会话实体
 * <p>对应 ai_orchestration_session 表，记录一次多 Agent 编排执行的完整生命周期。</p>
 */
@Entity
@Data
@Table(name = "ai_orchestration_session")
public class OrchestrationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键")
    private Long id;

    @Column(name = "scene_id")
    @Comment("场景ID")
    private Long sceneId;

    @Column(name = "orchestration_id")
    @Comment("编排ID（静态编排时有值，动态规划时为NULL）")
    private Long orchestrationId;

    @Column(name = "user_id")
    @Comment("用户ID")
    private Long userId;

    @Column(name = "conversation_id")
    @Comment("会话ID（同一轮对话的所有消息共享）")
    private String conversationId;

    @Column(name = "request_id")
    @Comment("请求ID（精确到某一次交互）")
    private String requestId;

    @Column(name = "user_message", columnDefinition = "TEXT")
    @Comment("用户原始输入")
    private String userMessage;

    @Column(name = "strategy")
    @Comment("执行策略：sequential / parallel / supervisor / crew / dynamic")
    private String strategy;

    @Column(name = "status")
    @Comment("状态：running-执行中 | success-成功 | failed-失败 | cancelled-已取消")
    private String status;

    @Column(name = "current_round")
    @Comment("当前执行轮次")
    private Integer currentRound;

    @Column(name = "max_rounds")
    @Comment("最大轮次")
    private Integer maxRounds;

    @Column(name = "total_duration_ms")
    @Comment("总耗时（毫秒）")
    private Long totalDurationMs;

    @Column(name = "tokens_prompt")
    @Comment("累计输入Token数")
    private Integer tokensPrompt;

    @Column(name = "tokens_completion")
    @Comment("累计输出Token数")
    private Integer tokensCompletion;

    @Column(name = "final_output", columnDefinition = "MEDIUMTEXT")
    @Comment("最终输出结果")
    private String finalOutput;

    @Column(name = "error_message", columnDefinition = "TEXT")
    @Comment("失败时的错误信息")
    private String errorMessage;

    @Column(name = "metadata", columnDefinition = "JSON")
    @Comment("扩展元数据")
    private String metadata;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
