package org.seaPack.model.system.permission;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.io.Serializable;

/**
 * 角色-权限关联实体
 * <p>对应 sys_role_permission 表，使用 @IdClass 处理复合主键。
 * 外键约束已由建表 SQL 声明（ON DELETE CASCADE）。</p>
 */
@Entity
@Data
@IdClass(SysRolePermission.SysRolePermissionId.class)
@Table(name = "sys_role_permission")
public class SysRolePermission {

    @Id
    @Column(name = "role_id")
    @Comment("角色ID")
    private Long roleId;

    @Id
    @Column(name = "permission_id")
    @Comment("权限ID")
    private Long permissionId;

    @Data
    public static class SysRolePermissionId implements Serializable {
        private Long roleId;
        private Long permissionId;
    }
}
