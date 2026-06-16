package org.seaPack.mapper.system;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.system.permission.SysPermission;

import java.util.List;

/**
 * 权限/菜单 Mapper
 * <p>提供 sys_permission 表的 CRUD 及子节点查询，用于组装权限树。</p>
 */
@Mapper
public interface SysPermissionMapper {

    /**
     * 查询所有权限记录，按 sort_order、id 升序排列
     */
    List<SysPermission> selectAllPermissions();

    /**
     * 根据主键查询权限
     */
    SysPermission selectPermissionById(@Param("id") Long id);

    /**
     * 新增权限节点
     */
    int insertPermission(SysPermission permission);

    /**
     * 更新权限节点信息
     */
    int updatePermission(SysPermission permission);

    /**
     * 根据主键删除权限
     */
    int deletePermissionById(@Param("id") Long id);

    /**
     * 根据父级 ID 查询直接子节点（用于级联删除判断）
     */
    List<SysPermission> selectChildrenByParentId(@Param("parentId") Long parentId);
}
