package org.seapack.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seapack.mapper.UserMapper;
import org.seapack.model.User;
import org.seapack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    public PageInfo<User> getUserList(int pageNum,int pageSize,String keywords, String status, Long deptId, String startTime, String endTime) {
        PageHelper.startPage(pageNum,pageSize); // 关键：紧邻查询方法
        List<User> users = userMapper.selectUserList(keywords,status,deptId,startTime,endTime);
        return new PageInfo<>(users); // 封装分页信息
    }
}
