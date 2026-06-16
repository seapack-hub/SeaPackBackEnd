package org.seaPack.model.system.permission;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

/**
 * 系统权限/菜单实体
 * <p>对应 sys_permission 表，构成树形结构的目录、菜单和按钮资源。</p>
 */
@Entity
@Data
@Table(name = "sys_permission")
public class SysPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("权限ID")
    private Long id;

    @Column(name = "parent_id")
    @Comment("父级权限ID (0表示顶级)")
    private Long parentId;

    @Column(name = "name")
    @Comment("权限/菜单名称")
    private String name;

    @Column(name = "perm_key")
    @Comment("权限标识符")
    private String permKey;

    @Column(name = "type")
    @Comment("资源类型 (1-目录, 2-菜单, 3-按钮)")
    private Integer type;

    @Column(name = "path")
    @Comment("前端路由路径")
    private String path;

    @Column(name = "component")
    @Comment("前端组件路径")
    private String component;

    @Column(name = "sort_order")
    @Comment("显示排序")
    private Integer sortOrder;

    @Column(name = "status")
    @Comment("状态 (1正常 0停用)")
    private Integer status;
}
