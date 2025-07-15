package org.seapack.controller;

import org.seapack.model.User;
import org.seapack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public List<User> getUserList(String userName, String email, LocalDateTime startTime, LocalDateTime endTime){
        return userService.getUserList(userName,email,startTime,endTime);
    }

}
