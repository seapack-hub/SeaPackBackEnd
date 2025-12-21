package org.seaPack.service;

import org.seaPack.model.Department;

import java.util.List;

public interface DepartmentService {

    List<Department> getDepartmentTree();

    void buildTree(Department parent, List<Department> allDept);

    Department getSubTree(Integer deptId);
}
