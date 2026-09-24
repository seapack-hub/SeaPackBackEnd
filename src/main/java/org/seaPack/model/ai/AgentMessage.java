package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * Agent 间消息实体
 * <p>对应 ai_agent_message 表，记录多 Agent 协作过程中的所有通信消息。</p>
 */
@Entity
@Data
@Table(name = "ai_agent_message")
public class AgentMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键")
    private Long id;

    @Column(name = "session_id")
    @Comment("所属执行会话ID")
    private Long sessionId;

    @Column(name = "round_index")
    @Comment("对话轮次（第几轮Agent间交互）")
    private Integer roundIndex;

    @Column(name = "sender_type")
    @Comment("发送者类型：supervisor-总控 | worker-工作Agent | system-系统 | user-用户")
    private String senderType;

    @Column(name = "sender_agent_id")
    @Comment("发送者Agent ID")
    private Long senderAgentId;

    @Column(name = "receiver_type")
    @Comment("接收者类型：supervisor-回传总控 | worker-发给指定Agent | broadcast-广播")
    private String receiverType;

    @Column(name = "receiver_agent_id")
    @Comment("接收者Agent ID（broadcast时为NULL）")
    private Long receiverAgentId;

    @Column(name = "message_type")
    @Comment("消息类型：instruction-任务指令 | report-结果报告 | question-提问求助 | handoff-任务交接 | summary-汇总摘要 | feedback-反馈评价")
    private String messageType;

    @Column(name = "content", columnDefinition = "TEXT")
    @Comment("消息内容（结构化JSON或纯文本）")
    private String content;

    @Column(name = "metadata", columnDefinition = "JSON")
    @Comment("元数据：置信度、标签、引用来源等")
    private String metadata;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;
}
