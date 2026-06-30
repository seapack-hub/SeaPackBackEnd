package org.seaPack.model.ai;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 技能-模块绑定实体
 * <p>对应 ai_skill_module_binding 表，控制技能出现在前端哪个模块的哪个位置。</p>
 */
@Entity
@Data
@Table(name = "ai_skill_module_binding")
public class SkillModuleBinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "skill_id")
    @Comment("技能ID")
    private Long skillId;

    @Column(name = "module_key")
    @Comment("模块标识，对应前端 config/modules.ts 的 key")
    private String moduleKey;

    @Column(name = "position")
    @Comment("模块内位置标识，如 toolbar / sidebar / menu")
    private String position;

    @Column(name = "config", columnDefinition = "JSON")
    @Comment("模块内展示配置，如按钮文案、图标等")
    private String config;

    @Column(name = "status")
    @Comment("状态：1启用 0禁用")
    private Integer status;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
