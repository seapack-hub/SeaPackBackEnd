package org.seaPack.model.system.permission;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.io.Serializable;

/**
 * 用户-角色关联实体
 * <p>对应 sys_user_role 表，使用 @IdClass 处理复合主键。
 * 外键约束已由建表 SQL 声明（ON DELETE CASCADE）。</p>
 */
@Entity
@Data
@IdClass(SysUserRole.SysUserRoleId.class)
@Table(name = "sys_user_role")
public class SysUserRole {

    @Id
    @Column(name = "user_id")
    @Comment("用户ID")
    private Long userId;

    @Id
    @Column(name = "role_id")
    @Comment("角色ID")
    private Long roleId;

    @Data
    public static class SysUserRoleId implements Serializable {
        private Long userId;
        private Long roleId;
    }
}
