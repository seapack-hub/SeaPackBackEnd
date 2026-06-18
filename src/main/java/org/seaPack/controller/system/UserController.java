package org.seaPack.controller.system;

import com.github.pagehelper.PageInfo; // MyBatis 分页信息
import org.seaPack.model.system.User;
import org.seaPack.service.system.SysUserRoleService;
import org.seaPack.service.system.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 * 提供用户管理的分页查询、新增、修改和删除接口。
 */
@RestController // 标识为 RESTful 控制器
@RequestMapping("/user") // 请求基础路径
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    /**
     * 根据用户 ID 查询用户详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * 分页查询用户列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keywords 关键字搜索
     * @param status 状态筛选
     * @param deptId 部门 ID
     * @param startTime 起始时间
     * @param endTime 截止时间
     */
    @GetMapping("/page/list")
    public PageInfo<User> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return userService.getUserList(pageNum, pageSize, keywords, status, deptId, startTime, endTime);
    }

    /**
     * 新增用户
     * @param user 用户实体
     * @return 影响行数
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody User user) {
        return ResponseEntity.ok(userService.insertUser(user));
    }

    /**
     * 修改用户信息
     * @param user 用户实体（需含 id）
     * @return 影响行数
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(user));
    }

    /**
     * 删除用户
     * @param id 用户 ID
     * @return 影响行数
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    /**
     * 批量删除用户
     * @param ids 用户 ID 列表
     * @return 影响行数
     */
    @DeleteMapping("/batchDelete")
    public ResponseEntity<Integer> batchDelete(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(userService.batchDeleteUsers(ids));
    }

    /**
     * 重置用户密码
     * @param param 包含 id 和 newPassword 的 JSON 对象
     * @return 影响行数
     */
    @PutMapping("/resetPassword")
    public ResponseEntity<Integer> resetPassword(@RequestBody Map<String, Object> param) {
        Long id = Long.valueOf(param.get("id").toString());
        String newPassword = (String) param.get("newPassword");
        return ResponseEntity.ok(userService.resetPassword(id, newPassword));
    }

    /**
     * 为用户分配角色（全量覆盖）
     * <p>在用户管理页面中通过"分配角色"弹窗调用，先清除旧绑定，再批量插入新角色。</p>
     *
     * @param id   用户 ID
     * @param body JSON 体 { roleIds: [1, 2, 3] }
     */
    @PutMapping("/{id}/roles")
    public ResponseEntity<Void> assignRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        sysUserRoleService.assignRoles(id, body.get("roleIds"));
        return ResponseEntity.ok().build();
    }

    /**
     * 查询用户已绑定的角色 ID 列表（用于回显分配角色弹窗的勾选状态）
     */
    @GetMapping("/{id}/roles")
    public ResponseEntity<List<Long>> getRoleIds(@PathVariable Long id) {
        return ResponseEntity.ok(sysUserRoleService.getRoleIdsByUserId(id));
    }
}
