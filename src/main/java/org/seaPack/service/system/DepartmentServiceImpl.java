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

    public List<Department> getDepartmentTree() {
        List<Department> allDept = departmentMapper.selectAllDepartments();
        List<Department> rootDept = allDept.stream()
                .filter(dept -> dept.getParentDeptId() == null)
                .collect(Collectors.toList());
        rootDept.forEach(root -> buildTree(root, allDept));
        return rootDept;
    }

    public void buildTree(Department parent, List<Department> allDepts) {
        List<Department> children = allDepts.stream()
                .filter(dept -> parent.getDeptId().equals(dept.getParentDeptId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        children.forEach(child -> buildTree(child, allDepts));
    }

    public Department getSubTree(Integer deptId){
        return departmentMapper.selectSubTreeByPath(deptId);
    }
}