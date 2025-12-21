package org.seaPack.controller;

import org.seaPack.model.Department;
import org.seaPack.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dept")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/tree")
    public List<Department> getDeptTree(){
        return departmentService.getDepartmentTree();
    }

    // 获取指定节点的子树
    @GetMapping("/subtree/{deptId}")
    public Department getSubTree(@PathVariable Integer deptId) {
        return departmentService.getSubTree(deptId);
    }

}
