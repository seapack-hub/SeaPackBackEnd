package org.seaPack.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("用户ID")
    private Long id;

    @Column(name = "user_name")
    @Comment("用户名")
    private String userName;

    @Column(name = "email")
    @Comment("邮箱")
    private String email;

    @Column(name = "mobile")
    @Comment("手机号")
    private String mobile;

    @Column(name = "nick_name")
    @Comment("昵称")
    private String nickName;

    @Column(name = "gender")
    @Comment("性别")
    private String gender;

    @Column(name = "status")
    @Comment("状态")
    private String status;

    @Column(name = "dept_id")
    @Comment("部门ID")
    private Long deptId;

    @Column(name = "dept_name")
    @Comment("部门名称")
    private String deptName;

    @Column(name = "create_time")
    @Comment("创建时间")
    private String createTime;

    @Column(name = "password")
    @Comment("密码")
    private String password;
}
