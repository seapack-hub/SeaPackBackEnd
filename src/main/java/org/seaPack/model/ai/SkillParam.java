package org.seaPack.model.ai;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 技能输入参数实体
 * <p>对应 ai_skill_param 表，定义技能执行时需要用户填写的表单字段。
 * 前端根据这些参数定义动态渲染输入组件。</p>
 */
@Entity
@Data
@Table(name = "ai_skill_param")
public class SkillParam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "skill_id")
    @Comment("所属技能ID")
    private Long skillId;

    @Column(name = "param_name")
    @Comment("参数名，对应 prompt_template 中的变量名")
    private String paramName;

    @Column(name = "label")
    @Comment("参数标签，如股票代码")
    private String label;

    @Column(name = "param_type")
    @Comment("参数类型：string / number / boolean / select")
    private String paramType;

    @Column(name = "required")
    @Comment("是否必填：1是 0否")
    private Integer required;

    @Column(name = "default_value")
    @Comment("默认值")
    private String defaultValue;

    @Column(name = "options", columnDefinition = "JSON")
    @Comment("select类型的选项列表 [{label, value}]")
    private String options;

    @Column(name = "placeholder")
    @Comment("输入提示")
    private String placeholder;

    @Column(name = "sort_order")
    @Comment("排序号")
    private Integer sortOrder;

    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
