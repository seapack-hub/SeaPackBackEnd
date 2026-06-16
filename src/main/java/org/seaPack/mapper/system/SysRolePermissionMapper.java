package org.seaPack.mapper.system;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.system.permission.SysPermission;

import java.util.List;

/**
 * 角色-权限关联 Mapper
 * <p>操作 sys_role_permission 中间表，管理角色与权限的多对多关系。</p>
 */
@Mapper
public interface SysRolePermissionMapper {

    /**
     * 查询角色已分配的权限 ID 列表
     */
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询角色已分配的权限详情列表（联表 sys_permission）
     */
    List<SysPermission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色所有权限绑定
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入角色-权限绑定关系
     */
    int insertBatch(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    /**
     * 删除指定权限的所有角色绑定（权限删除时级联清理）
     */
    int deleteByPermissionId(@Param("permissionId") Long permissionId);
}
