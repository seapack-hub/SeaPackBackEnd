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

    // 插入操作
    int insertUser(User user);

    // 更新操作
    int updateUser(User user);
}
