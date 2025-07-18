package org.seapack.service;

import com.github.pagehelper.PageInfo;
import org.seapack.model.User;

public interface UserService {
    PageInfo<User> getUserList(int pageNum, int pageSize, String keywords, String status, Long deptId, String startTime, String endTime);
}
