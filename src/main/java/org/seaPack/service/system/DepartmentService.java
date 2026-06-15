package org.seaPack.service.system;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.system.DepartmentMapper;
import org.seaPack.model.system.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务
 * 提供部门树形结构查询、增删改功能，自动维护 dept_path 与 dept_level。
 */
@Slf4j
@Service
public class DepartmentService {

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
    public Department getSubTree(Integer deptId) {
        return departmentMapper.selectSubTreeByPath(deptId);
    }

    /**
     * 分页查询部门列表
     */
    public PageInfo<Department> getDeptList(int pageNum, int pageSize, String keyword, Integer deptLevel, Long parentDeptId) {
        PageHelper.startPage(pageNum, pageSize);
        List<Department> list = departmentMapper.selectDeptList(keyword, deptLevel, parentDeptId);
        return new PageInfo<>(list);
    }

    /**
     * 查询单个部门
     */
    public Department getById(Long deptId) {
        Department dept = departmentMapper.selectById(deptId);
        if (dept == null) {
            throw new RuntimeException("部门不存在");
        }
        return dept;
    }

    /**
     * 新增部门，自动计算 dept_path 与 dept_level
     */
    @Transactional
    public Department insertDept(Department dept) {
        departmentMapper.insertDept(dept);
        Long deptId = dept.getDeptId();

        if (dept.getParentDeptId() == null) {
            departmentMapper.updateDeptPath(deptId, String.valueOf(deptId), 1);
        } else {
            Department parent = departmentMapper.selectById(dept.getParentDeptId());
            if (parent == null) {
                throw new RuntimeException("父部门不存在");
            }
            String deptPath = parent.getDeptPath() + "/" + deptId;
            int deptLevel = Integer.parseInt(parent.getDeptLevel()) + 1;
            departmentMapper.updateDeptPath(deptId, deptPath, deptLevel);
        }

        return departmentMapper.selectById(deptId);
    }

    /**
     * 修改部门信息
     */
    @Transactional
    public Department updateDept(Department dept) {
        Department existing = departmentMapper.selectById(dept.getDeptId());
        if (existing == null) {
            throw new RuntimeException("部门不存在");
        }
        departmentMapper.updateDept(dept);
        return departmentMapper.selectById(dept.getDeptId());
    }

    /**
     * 删除部门（数据库级联删除子部门）
     */
    @Transactional
    public void deleteDept(Long deptId) {
        Department existing = departmentMapper.selectById(deptId);
        if (existing == null) {
            throw new RuntimeException("部门不存在");
        }
        departmentMapper.deleteDept(deptId);
    }
}
