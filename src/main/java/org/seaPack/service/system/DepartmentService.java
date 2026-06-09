package org.seaPack.service.system;

import org.seaPack.model.system.Department; // 部门实体

import java.util.List; // List 集合

/**
 * 部门服务接口
 * 定义部门树形结构查询方法。
 */
public interface DepartmentService {

    /**
     * 获取完整部门树（根节点列表）
     * @return 部门树（包含层级 children）
     */
    List<Department> getDepartmentTree();

    /**
     * 递归构建子树
     * @param parent 父节点
     * @param allDept 全量部门列表
     */
    void buildTree(Department parent, List<Department> allDept);

    /**
     * 获取指定部门的子树
     * @param deptId 部门 ID
     * @return 该部门及其下级子树
     */
    Department getSubTree(Integer deptId);
}