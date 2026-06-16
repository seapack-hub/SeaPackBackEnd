package org.seaPack.controller.system;

import org.seaPack.dto.system.PermissionTreeNode;
import org.seaPack.model.system.permission.SysPermission;
import org.seaPack.service.system.SysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限/菜单管理控制器
 * <p>提供权限树查询、新增/编辑/删除权限节点接口。</p>
 */
@RestController
@RequestMapping("/sys/permissions")
public class PermissionController {

    @Autowired
    private SysPermissionService permissionService;

    /**
     * 获取权限树形结构
     * <p>将扁平权限数据组装为嵌套树形 JSON，供前端 <el-table> 或 <el-tree> 渲染。</p>
     */
    @GetMapping("/tree")
    public ResponseEntity<List<PermissionTreeNode>> tree() {
        return ResponseEntity.ok(permissionService.getPermissionTree());
    }

    /**
     * 新增权限节点（目录、菜单或按钮）
     *
     * @param permission 需传入 parentId 确定层级关系
     */
    @PostMapping
    public ResponseEntity<Integer> insert(@RequestBody SysPermission permission) {
        return ResponseEntity.ok(permissionService.insertPermission(permission));
    }

    /**
     * 编辑权限节点
     */
    @PutMapping("/{id}")
    public ResponseEntity<Integer> update(@PathVariable Long id, @RequestBody SysPermission permission) {
        permission.setId(id);
        return ResponseEntity.ok(permissionService.updatePermission(permission));
    }

    /**
     * 删除权限节点
     * <p>递归删除所有子节点，并清理 role_permission 关联表。</p>
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.deletePermissionById(id));
    }
}
