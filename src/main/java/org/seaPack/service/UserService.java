package org.seaPack.service;

import com.github.pagehelper.PageInfo;
import org.seaPack.model.User;

public interface UserService {

    /**
     * 分页查询用户列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keywords 关键字（用户名模糊匹配）
     * @param status 用户状态
     * @param deptId 部门ID
     * @param startTime 创建时间-起始
     * @param endTime 创建时间-结束
     * @return 分页结果
     */
    PageInfo<User> getUserList(int pageNum, int pageSize, String keywords, String status, Long deptId, String startTime, String endTime);

    /**
     * 根据用户名查询用户（用于登录验证）
     * @param userName 用户名
     * @return 用户信息
     */
    User selectUserByName(String userName);
}
