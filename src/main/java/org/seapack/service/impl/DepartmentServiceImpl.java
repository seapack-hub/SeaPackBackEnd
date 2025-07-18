package org.seapack.service.impl;

import org.seapack.mapper.DepartmentMapper;
import org.seapack.model.Department;
import org.seapack.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    public List<Department> getDepartmentTree() {
        // 1. 查询所有部门
        List<Department> allDept = departmentMapper.selectAllDepartments();

        // 2. 构建根节点（parent_dept_id为null）
        List<Department> rootDept = allDept.stream()
                .filter(dept -> dept.getParentDeptId() == null)
                .collect(Collectors.toList());

        // 3. 递归构建子树
        rootDept.forEach(root -> buildTree(root, allDept));
        return rootDept;
    }

    public void buildTree(Department parent, List<Department> allDepts) {
        // 查找直接子节点
        List<Department> children = allDepts.stream()
                .filter(dept -> parent.getDeptId().equals(dept.getParentDeptId()))
                .collect(Collectors.toList());

        parent.setChildren(children);

        // 递归处理子节点
        children.forEach(child -> buildTree(child, allDepts));
    }

    public Department getSubTree(Integer deptId){
        return departmentMapper.selectSubTreeByPath(deptId);
    }


}
