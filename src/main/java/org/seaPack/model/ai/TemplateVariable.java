package org.seaPack.model.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.util.Date;
import java.util.List;

/**
 * 提示词模板变量定义实体
 * <p>对应 ai_template_variable 表，定义模板中 {{var_name}} 占位符的输入参数。</p>
 */
@Entity
@Data
@Table(name = "ai_template_variable")
public class TemplateVariable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "template_id")
    @Comment("所属模板ID")
    private Long templateId;

    @Column(name = "var_name")
    @Comment("变量名，对应 content 中的 {{var_name}}")
    private String varName;

    @Column(name = "label")
    @Comment("显示标签，如股票代码")
    private String label;

    @Column(name = "var_type")
    @Comment("变量类型：string/number/boolean/select/date")
    private String varType;

    @Column(name = "required")
    @Comment("1必填 0选填")
    private Integer required;

    @Column(name = "default_value")
    @Comment("默认值")
    private String defaultValue;

    @Setter(AccessLevel.NONE)
    @Column(name = "options", columnDefinition = "JSON")
    @Comment("select类型选项 [{label,value}]")
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

    private static final ObjectMapper OPTIONS_MAPPER = new ObjectMapper();

    public void setOptions(Object options) {
        if (options == null) {
            this.options = null;
        } else if (options instanceof String) {
            this.options = (String) options;
        } else {
            try {
                this.options = OPTIONS_MAPPER.writeValueAsString(options);
            } catch (JsonProcessingException e) {
                this.options = options.toString();
            }
        }
    }
}
