package org.seaPack.service.system;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.system.UserMapper;
import org.seaPack.model.system.User;
import org.seaPack.service.system.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 分页查询用户列表
     */
    public PageInfo<User> getUserList(int pageNum, int pageSize, String keywords, String status, Long deptId, String startTime, String endTime) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> users = userMapper.selectUserList(keywords, status, deptId, startTime, endTime);
        return new PageInfo<>(users);
    }

    /**
     * 根据用户名查询用户
     */
    public User selectUserByName(String userName){
        return userMapper.selectUserByName(userName);
    }
}