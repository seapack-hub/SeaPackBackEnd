package org.seaPack.service.system;

import org.seaPack.mapper.system.DepartmentMapper;
import org.seaPack.model.system.Department;
import org.seaPack.service.system.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    /**
     * 获取完整部门树（根节点递归构建）
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
     */
    public void buildTree(Department parent, List<Department> allDepts) {
        List<Department> children = allDepts.stream()
                .filter(dept -> parent.getDeptId().equals(dept.getParentDeptId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        children.forEach(child -> buildTree(child, allDepts));
    }

    /**
     * 根据部门 ID 获取子树
     */
    public Department getSubTree(Integer deptId){
        return departmentMapper.selectSubTreeByPath(deptId);
    }
}