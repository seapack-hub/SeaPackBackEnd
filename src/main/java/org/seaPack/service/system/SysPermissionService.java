package org.seaPack.service.system;

import org.seaPack.dto.system.PermissionTreeNode;
import org.seaPack.mapper.system.SysPermissionMapper;
import org.seaPack.mapper.system.SysRolePermissionMapper;
import org.seaPack.model.system.permission.SysPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限/菜单管理服务
 * <p>提供权限节点的 CRUD、树形结构组装以及递归删除子节点。</p>
 */
@Service
public class SysPermissionService {

    @Autowired
    private SysPermissionMapper permissionMapper;

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    /**
     * 获取权限树形结构
     * <p>将扁平的权限数据按 parent_id 分组，递归组装为嵌套的树形 JSON。</p>
     */
    public List<PermissionTreeNode> getPermissionTree() {
        List<SysPermission> all = permissionMapper.selectAllPermissions();
        Map<Long, List<SysPermission>> parentMap = all.stream()
                .collect(Collectors.groupingBy(p -> p.getParentId() != null ? p.getParentId() : 0L));
        List<PermissionTreeNode> tree = new ArrayList<>();
        for (SysPermission p : parentMap.getOrDefault(0L, new ArrayList<>())) {
            tree.add(buildNode(p, parentMap));
        }
        return tree;
    }

    /**
     * 递归构建权限树节点
     *
     * @param p         当前权限
     * @param parentMap 按 parentId 分组的权限映射
     */
    private PermissionTreeNode buildNode(SysPermission p, Map<Long, List<SysPermission>> parentMap) {
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

    /**
     * 根据 ID 查询权限
     */
    public SysPermission getPermissionById(Long id) {
        return permissionMapper.selectPermissionById(id);
    }

    /**
     * 新增权限节点
     */
    @Transactional
    public int insertPermission(SysPermission permission) {
        return permissionMapper.insertPermission(permission);
    }

    /**
     * 更新权限节点
     */
    @Transactional
    public int updatePermission(SysPermission permission) {
        return permissionMapper.updatePermission(permission);
    }

    /**
     * 删除权限节点（递归删除子节点 + 清理关联的角色-权限记录）
     */
    @Transactional
    public int deletePermissionById(Long id) {
        List<SysPermission> children = permissionMapper.selectChildrenByParentId(id);
        if (children != null && !children.isEmpty()) {
            for (SysPermission child : children) {
                deletePermissionById(child.getId());
            }
        }
        rolePermissionMapper.deleteByPermissionId(id);
        return permissionMapper.deletePermissionById(id);
    }
}
