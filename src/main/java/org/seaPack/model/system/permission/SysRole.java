package org.seaPack.model.system.permission;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 系统角色实体
 * <p>对应 sys_role 表，定义系统内的身份类型（如超级管理员、普通用户）。</p>
 */
@Entity
@Data
@Table(name = "sys_role")
public class SysRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("角色ID")
    private Long id;

    @Column(name = "role_name")
    @Comment("角色名称")
    private String roleName;

    @Column(name = "role_code")
    @Comment("角色编码")
    private String roleCode;

    @Column(name = "description")
    @Comment("角色描述")
    private String description;

    @Column(name = "status")
    @Comment("状态 (1正常 0停用)")
    private Integer status;

    @Column(name = "create_time")
    @Comment("创建时间")
    private Date createTime;
}
