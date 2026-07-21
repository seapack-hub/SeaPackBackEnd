package org.seaPack.service.system;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.system.UserMapper;
import org.seaPack.model.system.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务
 * 提供用户管理的分页查询、新增、修改和删除操作。
 */
@Slf4j
@Service
@Transactional
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 分页查询用户列表
     */
    @Transactional(readOnly = true)
    public PageInfo<User> getUserList(int pageNum, int pageSize, String keywords, String status, Long deptId, String startTime, String endTime) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectUserList(keywords, status, deptId, startTime, endTime);
        return new PageInfo<>(list);
    }

    /**
     * 根据 ID 查询用户
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userMapper.selectUserById(id);
    }

    /**
     * 根据用户名查询用户
     */
    @Transactional(readOnly = true)
    public User selectUserByName(String userName) {
        return userMapper.selectUserByName(userName);
    }

    /**
     * 新增用户，校验用户名唯一性
     */
    public int insertUser(User user) {
        User existing = userMapper.selectUserByName(user.getUserName());
        if (existing != null) {
            throw new RuntimeException("用户名 " + user.getUserName() + " 已存在");
        }
        return userMapper.insertUser(user);
    }

    /**
     * 修改用户信息，校验用户名不与其他用户冲突
     */
    public int updateUser(User user) {
        User existing = userMapper.selectUserByName(user.getUserName());
        if (existing != null && !existing.getId().equals(user.getId())) {
            throw new RuntimeException("用户名 " + user.getUserName() + " 已被其他用户使用");
        }
        return userMapper.updateUser(user);
    }

    /**
     * 删除用户，校验用户是否存在
     */
    public int deleteUser(Long id) {
        User existing = userMapper.selectUserById(id);
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }
        return userMapper.deleteUser(id);
    }

    /**
     * 批量删除用户
     */
    public int batchDeleteUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return userMapper.batchDeleteUsers(ids);
    }

    /**
     * 重置用户密码为默认密码
     */
    public int resetPassword(Long id, String newPassword) {
        User existing = userMapper.selectUserById(id);
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }
        return userMapper.resetPassword(id, newPassword);
    }
}
