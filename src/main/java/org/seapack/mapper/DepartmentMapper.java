package org.seapack.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seapack.model.Department;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    // 查询所有部门（用于内存构建树）
    List<Department> selectAllDepartments();

    // 按层级路径查询子树（高效递归）
    Department selectSubTreeByPath(@Param("deptId") int deptId);
}
