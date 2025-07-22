package org.seapack.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seapack.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {

    // XML方式（复杂SQL）
    List<User> selectUserList(@Param("keywords") String keywords,
                                 @Param("status") String status,
                                 @Param("deptId") Long deptId,
                                 @Param("startTime") String startTime,
                                 @Param("endTime") String endTime);

    /**
     * 注册登录验证，根据用户密码查询用户是否存在
     * @param userName 用户名
     * @return 数量
     */
    User selectUserByName(@Param("userName") String userName);
    // 插入操作
    int insertUser(User user);

    // 更新操作
    int updateUser(User user);
}
