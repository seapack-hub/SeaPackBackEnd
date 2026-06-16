package org.seaPack.service.auth;

import org.seaPack.dto.system.PermissionTreeNode;
import org.seaPack.dto.system.UserInfoVO;
import org.seaPack.mapper.system.SysPermissionMapper;
import org.seaPack.mapper.system.SysRolePermissionMapper;
import org.seaPack.mapper.system.SysUserRoleMapper;
import org.seaPack.mapper.system.UserMapper;
import org.seaPack.model.system.User;
import org.seaPack.model.system.permission.SysPermission;
import org.seaPack.model.system.permission.SysRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 认证与鉴权服务
 * <p>提供当前登录用户的角色、权限标识符查询，以及动态菜单树构建。</p>
 */
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    @Autowired
    private SysPermissionMapper permissionMapper;

    /**
     * 获取当前用户信息及权限
     * <p>返回用户基本信息、角色列表（roleCode）和权限标识符集合（permKey）。</p>
     *
     * @param userId 用户 ID
     * @return UserInfoVO 包含 roles 和 permissions，用户不存在时返回 null
     */
    public UserInfoVO getUserInfo(Long userId) {
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            return null;
        }
        List<SysRole> roles = userRoleMapper.selectRolesByUserId(userId);

        // 聚合所有角色下的权限标识符（去重）
        Set<SysPermission> permSet = new HashSet<>();
        for (SysRole role : roles) {
            List<SysPermission> perms = rolePermissionMapper.selectPermissionsByRoleId(role.getId());
            permSet.addAll(perms);
        }
        return UserInfoVO.of(userId, user.getUserName(), roles, new ArrayList<>(permSet));
    }

    /**
     * 获取当前用户的动态菜单树
     * <p>仅返回 type=1(目录) 和 type=2(菜单) 的节点，且根据用户角色过滤。
     * 补全父节点链以确保树形完整。</p>
     *
     * @param userId 用户 ID
     * @return 菜单树形结构
     */
    public List<PermissionTreeNode> getUserMenus(Long userId) {
        List<SysRole> roles = userRoleMapper.selectRolesByUserId(userId);

        // 收集用户所有角色下的权限 ID
        Set<Long> permIds = new HashSet<>();
        for (SysRole role : roles) {
            List<Long> ids = rolePermissionMapper.selectPermissionIdsByRoleId(role.getId());
            permIds.addAll(ids);
        }

        // 过滤出菜单/目录类型，并补全父节点链
        List<SysPermission> allPerms = permissionMapper.selectAllPermissions();
        Set<Long> visibleIds = new HashSet<>();
        for (SysPermission p : allPerms) {
            if (permIds.contains(p.getId()) && (p.getType() == 1 || p.getType() == 2)) {
                visibleIds.add(p.getId());
                addAncestors(p, allPerms, visibleIds);
            }
        }

        List<SysPermission> filtered = allPerms.stream()
                .filter(p -> visibleIds.contains(p.getId()))
                .collect(java.util.stream.Collectors.toList());

        return buildMenuTree(filtered);
    }

    /**
     * 递归补全父节点 ID 到可见集合（确保树形结构完整）
     */
    private void addAncestors(SysPermission p, List<SysPermission> allPerms, Set<Long> visibleIds) {
        if (p.getParentId() == null || p.getParentId() == 0) return;
        visibleIds.add(p.getParentId());
        allPerms.stream()
                .filter(a -> a.getId().equals(p.getParentId()))
                .findFirst()
                .ifPresent(parent -> addAncestors(parent, allPerms, visibleIds));
    }

    /**
     * 构建菜单树
     */
    private List<PermissionTreeNode> buildMenuTree(List<SysPermission> perms) {
        java.util.Map<Long, List<SysPermission>> parentMap = perms.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getParentId() != null ? p.getParentId() : 0L));
        List<PermissionTreeNode> tree = new ArrayList<>();
        for (SysPermission p : parentMap.getOrDefault(0L, new ArrayList<>())) {
            tree.add(buildNode(p, parentMap));
        }
        return tree;
    }

    /**
     * 递归构建菜单树节点
     */
    private PermissionTreeNode buildNode(SysPermission p,
                                          java.util.Map<Long, List<SysPermission>> parentMap) {
        PermissionTreeNode node = new PermissionTreeNode();
        node.setId(p.getId());
        node.setParentId(p.getParentId());
        node.setName(p.getName());
        node.setPermKey(p.getPermKey());
        node.setType(p.getType());
        node.setPath(p.getPath());
        node.setComponent(p.getComponent());
        node.setSortOrder(p.getSortOrder());
        node.setStatus(p.getStatus());
        List<SysPermission> children = parentMap.getOrDefault(p.getId(), new ArrayList<>());
        for (SysPermission child : children) {
            node.getChildren().add(buildNode(child, parentMap));
        }
        return node;
    }
}
