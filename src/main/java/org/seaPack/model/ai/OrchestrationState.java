package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 编排运行时共享状态实体
 * <p>对应 ai_orchestration_state 表，存储多 Agent 协作过程中的共享上下文数据。
 * 通过乐观锁（version 字段）防止并发写冲突。</p>
 */
@Entity
@Data
@Table(name = "ai_orchestration_state")
public class OrchestrationState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键")
    private Long id;

    @Column(name = "session_id")
    @Comment("所属执行会话ID")
    private Long sessionId;

    @Column(name = "state_key")
    @Comment("状态键，如 plan / agent_outputs / user_context / execution_log")
    private String stateKey;

    @Column(name = "state_value", columnDefinition = "JSON")
    @Comment("状态值（JSON结构）")
    private String stateValue;

    @Column(name = "version")
    @Comment("版本号（乐观锁，防止并发写冲突）")
    private Integer version;

    @Column(name = "updated_by")
    @Comment("最后更新的Agent ID")
    private Long updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
