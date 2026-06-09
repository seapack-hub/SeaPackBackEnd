package org.seaPack.mapper.system;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.system.User;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 多条件分页查询用户列表
     * @param keywords 关键字（用户名模糊匹配）
     * @param status 用户状态
     * @param deptId 部门ID
     * @param startTime 创建时间-起始
     * @param endTime 创建时间-结束
     * @return 用户列表
     */
    List<User> selectUserList(@Param("keywords") String keywords,
                                  @Param("status") String status,
                                  @Param("deptId") Long deptId,
                                  @Param("startTime") String startTime,
                                  @Param("endTime") String endTime);

    /**
     * 根据用户名查询用户（用于登录验证）
     * @param userName 用户名
     * @return 用户信息
     */
    User selectUserByName(@Param("userName") String userName);

    /**
     * 新增用户
     * @param user 用户信息
     * @return 影响行数
     */
    int insertUser(User user);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 影响行数
     */
    int updateUser(User user);
}