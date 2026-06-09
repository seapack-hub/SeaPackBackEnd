package org.seaPack.controller.system;

import org.seaPack.model.system.Department; // 部门实体
import org.seaPack.service.system.DepartmentService; // 部门服务
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.web.bind.annotation.*; // Spring Web MVC 注解

import java.util.List; // List 集合

/**
 * 部门控制器
 * 提供部门树形结构查询接口。
 */
@RestController // 标识为 RESTful 控制器
@RequestMapping("/dept") // 请求基础路径
public class DepartmentController {

    @Autowired // 注入部门服务
    private DepartmentService departmentService;

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
    public Department getSubTree(@PathVariable Integer deptId) {
        return departmentService.getSubTree(deptId);
    }

}