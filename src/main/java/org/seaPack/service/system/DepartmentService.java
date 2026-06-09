package org.seaPack.service.system;

import org.seaPack.model.system.Department;

import java.util.List;

public interface DepartmentService {

    List<Department> getDepartmentTree();

    void buildTree(Department parent, List<Department> allDept);

    Department getSubTree(Integer deptId);
}