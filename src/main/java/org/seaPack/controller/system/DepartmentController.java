package org.seaPack.controller.system;

import com.github.pagehelper.PageInfo; // MyBatis 分页信息
import org.seaPack.model.system.Department; // 部门实体
import org.seaPack.service.system.DepartmentService; // 部门服务
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.http.ResponseEntity; // HTTP 响应实体
import org.springframework.web.bind.annotation.*; // Spring Web MVC 注解

import java.util.List; // List 集合

/**
 * 部门控制器
 * 提供部门树形结构查询、增删改查接口。
 */
@RestController // 标识为 RESTful 控制器
@RequestMapping("/dept") // 请求基础路径
public class DepartmentController {

    @Autowired // 注入部门服务
    private DepartmentService departmentService;

    /**
     * 分页查询部门列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 关键字（部门名称模糊匹配）
     * @param deptLevel 部门层级筛选
     * @param parentDeptId 父部门 ID 筛选
     */
    @GetMapping("/page/list")
    public PageInfo<Department> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer deptLevel,
            @RequestParam(required = false) Long parentDeptId) {
        return departmentService.getDeptList(pageNum, pageSize, keyword, deptLevel, parentDeptId);
    }

    /**
     * 获取完整部门树
     */
    @GetMapping("/tree")
    public List<Department> getDeptTree(){
        return departmentService.getDepartmentTree();
    }

    /**
     * 获取指定部门的子树
     * @param deptId 部门 ID
     */
    @GetMapping("/subtree/{deptId}")
    public Department getSubTree(@PathVariable int deptId) {
        return departmentService.getSubTree(deptId);
    }

    /**
     * 查询单个部门
     * @param deptId 部门 ID
     */
    @GetMapping("/{deptId}")
    public ResponseEntity<Department> getById(@PathVariable Long deptId) {
        return ResponseEntity.ok(departmentService.getById(deptId));
    }

    /**
     * 新增部门
     * @param dept 部门实体
     */
    @PostMapping("/insert")
    public ResponseEntity<Department> insert(@RequestBody Department dept) {
        return ResponseEntity.ok(departmentService.insertDept(dept));
    }

    /**
     * 修改部门
     * @param dept 部门实体（需含 deptId）
     */
    @PutMapping("/update")
    public ResponseEntity<Department> update(@RequestBody Department dept) {
        return ResponseEntity.ok(departmentService.updateDept(dept));
    }

    /**
     * 删除部门（级联删除子部门）
     * @param deptId 部门 ID
     */
    @DeleteMapping("/delete/{deptId}")
    public ResponseEntity<Void> delete(@PathVariable Long deptId) {
        departmentService.deleteDept(deptId);
        return ResponseEntity.ok().build();
    }

}