package org.seapack.service;

import org.seapack.model.Department;

import java.util.List;

public interface DepartmentService {

    List<Department> getDepartmentTree();

    void buildTree(Department parent, List<Department> allDept);

    Department getSubTree(Integer deptId);
}
