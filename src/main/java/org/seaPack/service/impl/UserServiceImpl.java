package org.seaPack.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.UserMapper;
import org.seaPack.model.User;
import org.seaPack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 分页查询用户列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keywords 关键字
     * @param status 状态
     * @param deptId 部门ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    public PageInfo<User> getUserList(int pageNum, int pageSize, String keywords, String status, Long deptId, String startTime, String endTime) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> users = userMapper.selectUserList(keywords, status, deptId, startTime, endTime);
        return new PageInfo<>(users);
    }

    /**
     * 根据用户名查询用户
     * @param userName 用户名
     * @return 用户信息
     */
    public User selectUserByName(String userName){
        return userMapper.selectUserByName(userName);
    }
}
