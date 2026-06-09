package org.seaPack.service.system;

import com.github.pagehelper.PageInfo;
import org.seaPack.model.system.User;

public interface UserService {

    /**
     * 分页查询用户列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keywords 关键字搜索
     * @param status 状态筛选
     * @param deptId 部门 ID
     * @param startTime 起始时间
     * @param endTime 截止时间
     * @return 分页结果
     */
    PageInfo<User> getUserList(int pageNum, int pageSize, String keywords, String status, Long deptId, String startTime, String endTime);

    /**
     * 根据用户名查询用户
     * @param userName 用户名
     * @return 用户实体
     */
    User selectUserByName(String userName);
}