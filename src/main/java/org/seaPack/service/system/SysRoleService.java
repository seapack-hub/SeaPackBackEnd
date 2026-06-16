package org.seaPack.service.system;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.system.SysRoleMapper;
import org.seaPack.mapper.system.SysRolePermissionMapper;
import org.seaPack.model.system.permission.SysRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色管理服务
 * <p>提供角色的分页查询、CRUD 以及角色-权限分配（全量覆盖）。</p>
 */
@Service
public class SysRoleService {

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    /**
     * 分页查询角色列表
     *
     * @param roleName 角色名称（模糊匹配）
     * @param status   状态过滤
     */
    public PageInfo<SysRole> getRoleList(int pageNum, int pageSize, String roleName, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        List<SysRole> list = roleMapper.selectRoleList(roleName, status);
        return new PageInfo<>(list);
    }

    /**
     * 根据 ID 查询角色
     */
    public SysRole getRoleById(Long id) {
        return roleMapper.selectRoleById(id);
    }

    /**
     * 新增角色
     */
    @Transactional
    public int insertRole(SysRole role) {
        return roleMapper.insertRole(role);
    }

    /**
     * 更新角色信息
     */
    @Transactional
    public int updateRole(SysRole role) {
        return roleMapper.updateRole(role);
    }

    /**
     * 删除角色（关联的 sys_role_permission 由外键级联删除）
     */
    @Transactional
    public int deleteRoleById(Long id) {
        return roleMapper.deleteRoleById(id);
    }

    /**
     * 为角色分配权限（全量覆盖）
     * <p>先清除该角色所有权限绑定，再批量插入新的绑定关系。</p>
     *
     * @param roleId        角色 ID
     * @param permissionIds 权限 ID 数组（由前端 <el-tree> 勾选生成）
     */
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.deleteByRoleId(roleId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            rolePermissionMapper.insertBatch(roleId, permissionIds);
        }
    }

    /**
     * 查询角色已分配的权限 ID 列表
     */
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }
}
