package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 场景部署配置实体
 * <p>对应 ai_scene_deployment 表，一个场景可部署到多个模块的多个位置，
 * 替代原 ai_scene 表的 module_key + position 字段。</p>
 */
@Entity
@Data
@Table(name = "ai_scene_deployment")
public class SceneDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "scene_id")
    @Comment("场景ID")
    private Long sceneId;

    @Column(name = "module_key")
    @Comment("前端模块标识")
    private String moduleKey;

    @Column(name = "position_key")
    @Comment("位置标识")
    private String positionKey;

    @Column(name = "config", columnDefinition = "JSON")
    @Comment("部署配置")
    private String config;

    @Column(name = "is_default")
    @Comment("是否该位置的默认场景")
    private Integer isDefault;

    @Column(name = "sort_order")
    @Comment("排序号")
    private Integer sortOrder;

    @Column(name = "status")
    @Comment("状态：1-启用 0-禁用")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
