package org.seaPack.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "industry_sector")
public class IndustrySector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("自增主键ID")
    private Long id;

    @Column(name = "code")
    @Comment("业务编码（如: 01, 0101）")
    private String code;

    @Column(name = "label")
    @Comment("行业名称（如: 科技, 半导体）")
    private String label;

    @Column(name = "parent_id")
    @Comment("父节点ID（引用本表id），顶级节点为NULL")
    private Long parentId;

    @Column(name = "node_level")
    @Comment("层级深度（1:一级, 2:二级）")
    private Integer nodeLevel;

    @Column(name = "sort_order")
    @Comment("排序权重")
    private Integer sortOrder;

    @Column(name = "is_deleted")
    @Comment("逻辑删除标记 (0:正常, 1:已删除)")
    private Integer isDeleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updated_at")
    @Comment("最后更新时间")
    private Date updatedAt;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Comment("子节点列表（树形结构）")
    private List<IndustrySector> children;
}
