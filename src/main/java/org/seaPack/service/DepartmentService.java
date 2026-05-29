package org.seaPack.service;

import org.seaPack.model.Department;

import java.util.List;

public interface DepartmentService {

    /**
     * 获取完整的部门树形结构
     * @return 部门树（根节点列表）
     */
    List<Department> getDepartmentTree();

    /**
     * 递归构建部门子树
     * @param parent 父部门节点
     * @param allDept 全部部门列表
     */
    void buildTree(Department parent, List<Department> allDept);

    /**
     * 根据部门ID获取子树
     * @param deptId 部门ID
     * @return 部门及其子树
     */
    Department getSubTree(Integer deptId);
}
