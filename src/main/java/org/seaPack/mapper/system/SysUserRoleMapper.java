package org.seaPack.mapper.system;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.system.permission.SysRole;

import java.util.List;

/**
 * 用户-角色关联 Mapper
 * <p>操作 sys_user_role 中间表，管理用户与角色的多对多关系。</p>
 */
@Mapper
public interface SysUserRoleMapper {

    /**
     * 查询用户拥有的角色列表（联表 sys_role）
     */
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 删除用户的所有角色绑定
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 批量插入用户-角色绑定关系
     */
    int insertBatch(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}
