package org.seapack.service.impl;

import org.seapack.mapper.UserMapper;
import org.seapack.model.User;
import org.seapack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    public List<User> getUserList(String userName, String email, LocalDateTime startTime, LocalDateTime endTime) {
        return userMapper.selectUserList(userName,email,startTime,endTime);
    }
}
