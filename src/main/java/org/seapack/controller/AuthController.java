package org.seapack.controller;

import org.seapack.dto.LoginRequest;
import org.seapack.model.User;
import org.seapack.service.CaptchaService;
import org.seapack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam String token,
            @RequestParam double sliderX,
            @RequestParam String username,
            @RequestParam String password
    ) {
        // 1. 先验证滑块
        if (!captchaService.verifyCaptcha(token, sliderX)) {
            return ResponseEntity.status(200).body("滑块验证失败");
        }

        // 2. 验证用户名密码
        if(!username.isEmpty() && !password.isEmpty()){
            //查询账户
            User user = userService.selectUserByName(username);
            if (user == null) return ResponseEntity.status(401).body("用户名错误");
            //验证密码
            if(passwordEncoder.matches(password, user.getPassword())){
                return ResponseEntity.ok("登录成功");
            }
        }
        return ResponseEntity.status(401).body("用户名或密码错误");
    }
}
