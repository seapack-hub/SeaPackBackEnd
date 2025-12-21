package org.seaPack.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deptId")
    private Long deptId;
    @Column(name = "deptName")
    private String deptName;
    @Column(name = "parentDeptId")
    private Long parentDeptId;
    @Column(name = "deptLevel")
    private String deptLevel;
    @Column(name = "deptPath")
    private String deptPath;
    @Column(name = "seq")
    private String seq;
    @Column(name = "createTime")
    private String createTime;
    @Column(name = "updateTime")
    private String updateTime;

    private List<Department> children;  // 子部门列表（树形结构关键）
}
