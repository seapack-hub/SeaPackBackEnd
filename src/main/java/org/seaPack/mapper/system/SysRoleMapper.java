package org.seaPack.mapper.system;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.system.permission.SysRole;

import java.util.List;

/**
 * 角色 Mapper
 * <p>提供 sys_role 表的基础 CRUD 操作。</p>
 */
@Mapper
public interface SysRoleMapper {

    /**
     * 分页查询角色列表，支持按角色名和状态过滤
     */
    List<SysRole> selectRoleList(@Param("roleName") String roleName,
                                  @Param("status") Integer status);

    /**
     * 根据主键查询角色
     */
    SysRole selectRoleById(@Param("id") Long id);

    /**
     * 新增角色
     */
    int insertRole(SysRole role);

    /**
     * 更新角色信息
     */
    int updateRole(SysRole role);

    /**
     * 根据主键删除角色（外键级联删除关联的角色-权限记录）
     */
    int deleteRoleById(@Param("id") Long id);
}
