package org.seaPack.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "email")
    private String email;
    @Column(name = "mobile")
    private String mobile;
    @Column(name = "nick_name")
    private String nickName;
    @Column(name = "gender")
    private String gender;
    @Column(name = "status")
    private String status;
    @Column(name = "dept_id")
    private Long deptId;
    @Column(name = "dept_name")
    private String deptName;
    @Column(name = "create_time")
    private String createTime;
    @Column(name = "password")
    private String password;
}
