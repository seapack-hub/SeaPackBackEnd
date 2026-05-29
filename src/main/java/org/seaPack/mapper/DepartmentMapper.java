package org.seaPack.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.Department;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    /**
     * 查询所有部门（用于在内存中构建部门树）
     * @return 全部部门列表
     */
    List<Department> selectAllDepartments();

    /**
     * 根据部门ID按层级路径查询子树
     * @param deptId 部门ID
     * @return 部门及其子树
     */
    Department selectSubTreeByPath(@Param("deptId") int deptId);
}
