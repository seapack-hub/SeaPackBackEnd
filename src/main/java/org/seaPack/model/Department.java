package org.seaPack.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.List;

@Data
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deptId")
    @Comment("部门ID")
    private Long deptId;

    @Column(name = "deptName")
    @Comment("部门名称")
    private String deptName;

    @Column(name = "parentDeptId")
    @Comment("父部门ID")
    private Long parentDeptId;

    @Column(name = "deptLevel")
    @Comment("部门层级")
    private String deptLevel;

    @Column(name = "deptPath")
    @Comment("部门路径")
    private String deptPath;

    @Column(name = "seq")
    @Comment("排序号")
    private String seq;

    @Column(name = "createTime")
    @Comment("创建时间")
    private String createTime;

    @Column(name = "updateTime")
    @Comment("更新时间")
    private String updateTime;

    @Comment("子部门列表（树形结构）")
    private List<Department> children;
}
