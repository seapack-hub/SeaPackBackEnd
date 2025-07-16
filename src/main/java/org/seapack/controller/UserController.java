package org.seapack.controller;

import com.github.pagehelper.PageInfo;
import org.seapack.model.User;
import org.seapack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/page/list")
    public PageInfo<User> getUserList(int pageNum,int pageSize,String keywords, String status, Long deptId,String startTime, String endTime){
        return userService.getUserList(pageNum,pageSize,keywords,status,deptId,startTime,endTime);
    }

}
