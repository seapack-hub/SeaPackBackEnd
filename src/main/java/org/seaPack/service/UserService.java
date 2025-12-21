package org.seaPack.service;

import com.github.pagehelper.PageInfo;
import org.seaPack.model.User;

public interface UserService {
    PageInfo<User> getUserList(int pageNum, int pageSize, String keywords, String status, Long deptId, String startTime, String endTime);

    User selectUserByName(String userName);
}
