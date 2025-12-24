package org.seaPack.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.seaPack.components.TreeNode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "dim_industry", indexes = {
        @Index(name = "idx_parent_code", columnList = "parent_code"),
        @Index(name = "idx_industry_state", columnList = "industry_state"),
        @Index(name = "idx_industry_code", columnList = "industry_code")
})
@EqualsAndHashCode(of = "industryCode")
public class DimIndustry implements TreeNode<DimIndustry, String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "industry_id")
    @Comment("行业唯一标识符")
    private Integer industryId;

    @Column(name = "industry_code", nullable = false, unique = true, length = 20)
    @Comment("国家标准行业代码，例如 A, 01, 011")
    private String industryCode;

    @Column(name = "industry_name", nullable = false, length = 100)
    @Comment("行业标准名称，例如'农、林、牧、渔业'")
    private String industryName;

    @Column(name = "parent_code", length = 20)
    @Comment("父级行业代码，用于构建层级关系。顶级行业的此字段为NULL")
    private String parentCode;

    @Column(name = "industry_state")
    @Comment("是否启用(1.适用,0.未适用)")
    private Integer industryState;

    @Column(name = "description", columnDefinition = "TEXT")
    @Comment("行业的详细描述或范围说明")
    private String description;

    @Column(name = "create_time", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Comment("记录创建时间")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Comment("记录最后更新时间")
    private LocalDateTime updateTime;

    // 子节点列表，为空时不序列化
    @Transient
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<DimIndustry> children = new ArrayList<>();

    // 实现TreeNode接口的方法
    @Override
    public String getId() {
        return industryCode;
    }

    @Override
    public String getParentId() {
        return parentCode;
    }

    @Override
    public List<DimIndustry> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<DimIndustry> children) {
        this.children = children;
    }


    // 业务方法
    public boolean isEnabled() {
        return industryState != null && industryState == 1;
    }

    public boolean isRoot() {
        return parentCode == null || parentCode.isEmpty();
    }
}
