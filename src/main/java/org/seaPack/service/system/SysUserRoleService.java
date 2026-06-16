package org.seaPack.service.system;

import org.seaPack.mapper.system.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户-角色分配服务
 * <p>管理用户的角色绑定关系，支持全量覆盖更新。</p>
 */
@Service
public class SysUserRoleService {

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    /**
     * 为用户分配角色（全量覆盖）
     * <p>先清除用户现有角色绑定，再批量插入新的绑定关系。</p>
     *
     * @param userId  用户 ID
     * @param roleIds 角色 ID 数组
     */
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            userRoleMapper.insertBatch(userId, roleIds);
        }
    }

    /**
     * 查询用户已绑定的角色 ID 列表
     */
    public List<Long> getRoleIdsByUserId(Long userId) {
        return userRoleMapper.selectRolesByUserId(userId).stream()
                .map(r -> r.getId())
                .collect(Collectors.toList());
    }
}
