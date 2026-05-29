package org.seaPack.service.impl;

import org.seaPack.mapper.DepartmentMapper;
import org.seaPack.model.Department;
import org.seaPack.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    /**
     * 获取完整的部门树形结构
     * @return 部门树（根节点列表）
     */
    public List<Department> getDepartmentTree() {
        List<Department> allDept = departmentMapper.selectAllDepartments();
        List<Department> rootDept = allDept.stream()
                .filter(dept -> dept.getParentDeptId() == null)
                .collect(Collectors.toList());
        rootDept.forEach(root -> buildTree(root, allDept));
        return rootDept;
    }

    /**
     * 递归构建部门子树
     * @param parent 父部门节点
     * @param allDepts 全部部门列表
     */
    public void buildTree(Department parent, List<Department> allDepts) {
        List<Department> children = allDepts.stream()
                .filter(dept -> parent.getDeptId().equals(dept.getParentDeptId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        children.forEach(child -> buildTree(child, allDepts));
    }

    /**
     * 根据部门ID获取子树
     * @param deptId 部门ID
     * @return 部门及其子树
     */
    public Department getSubTree(Integer deptId){
        return departmentMapper.selectSubTreeByPath(deptId);
    }
}
