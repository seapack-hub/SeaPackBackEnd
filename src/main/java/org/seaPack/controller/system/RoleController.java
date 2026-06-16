package org.seaPack.controller.system;

import com.github.pagehelper.PageInfo;
import org.seaPack.model.system.permission.SysRole;
import org.seaPack.service.system.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 * <p>提供角色的分页查询、新增、编辑、删除以及角色-权限分配接口。</p>
 */
@RestController
@RequestMapping("/sys/roles")
public class RoleController {

    @Autowired
    private SysRoleService roleService;

    /**
     * 分页查询角色列表
     *
     * @param roleName 角色名称（可选，模糊搜索）
     * @param status   状态（可选，1=正常 0=停用）
     */
    @GetMapping
    public PageInfo<SysRole> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Integer status) {
        return roleService.getRoleList(pageNum, pageSize, roleName, status);
    }

    /**
     * 新增角色
     */
    @PostMapping
    public ResponseEntity<Integer> insert(@RequestBody SysRole role) {
        return ResponseEntity.ok(roleService.insertRole(role));
    }

    /**
     * 编辑角色
     */
    @PutMapping("/{id}")
    public ResponseEntity<Integer> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        return ResponseEntity.ok(roleService.updateRole(role));
    }

    /**
     * 删除角色（关联的角色-权限记录由外键级联删除）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.deleteRoleById(id));
    }

    /**
     * 为角色分配权限（全量覆盖）
     * <p>前端 <el-tree> 勾选的权限 ID 数组，后端先清后插。</p>
     *
     * @param id   角色 ID
     * @param body JSON 体 { permissionIds: [1, 2, 3] }
     */
    @PutMapping("/{id}/permissions")
    public ResponseEntity<Void> assignPermissions(
            @PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        roleService.assignPermissions(id, body.get("permissionIds"));
        return ResponseEntity.ok().build();
    }

    /**
     * 查询角色已分配的权限 ID 列表（用于回显前端 <el-tree> 勾选状态）
     */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<List<Long>> getPermissionIds(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getPermissionIdsByRoleId(id));
    }
}
