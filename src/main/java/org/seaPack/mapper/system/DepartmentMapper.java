package org.seaPack.mapper.system;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.system.Department;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    /**
     * 查询所有部门（用于在内存中构建部门树）
     * @return 全部部门列表
     */
    List<Department> selectAllDepartments();

    /**
     * 多条件分页查询部门列表
     * @param keyword 关键字（部门名称模糊匹配）
     * @param deptLevel 部门层级筛选
     * @param parentDeptId 父部门 ID 筛选
     * @return 部门列表
     */
    List<Department> selectDeptList(@Param("keyword") String keyword,
                                    @Param("deptLevel") Integer deptLevel,
                                    @Param("parentDeptId") Long parentDeptId);

    /**
     * 根据部门ID按层级路径查询子树
     * @param deptId 部门ID
     * @return 部门及其子树
     */
    Department selectSubTreeByPath(@Param("deptId") int deptId);

    /**
     * 根据 ID 查询部门
     */
    Department selectById(@Param("deptId") Long deptId);

    /**
     * 新增部门（返回自增 ID）
     */
    int insertDept(Department dept);

    /**
     * 修改部门
     */
    int updateDept(Department dept);

    /**
     * 更新部门路径
     */
    int updateDeptPath(@Param("deptId") Long deptId, @Param("deptPath") String deptPath, @Param("deptLevel") Integer deptLevel);

    /**
     * 删除部门
     */
    int deleteDept(@Param("deptId") Long deptId);
}