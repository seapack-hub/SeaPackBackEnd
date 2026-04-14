package org.seaPack.controller;

import org.seaPack.components.RsaUtil;
import org.seaPack.model.User;
import org.seaPack.service.CaptchaService;
import org.seaPack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private UserService userService;

    @Autowired
    private RsaUtil rsaUtil;

    @GetMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam(defaultValue = "" ) String token,
            @RequestParam(defaultValue = "0" ) double sliderX,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(defaultValue = "true" ) boolean isVerify
    ) throws Exception {
        // 1. 先验证滑块
        if (!isVerify && !captchaService.verifyCaptcha(token, sliderX)) {
            return ResponseEntity.status(200).body("滑块验证失败");
        }

        // 2. 验证用户名密码
        if(!username.isEmpty() && !password.isEmpty()){
            //查询账户
            User user = userService.selectUserByName(username);
            if (user == null) return ResponseEntity.status(401).body("用户名错误");

            // 解密密码
            String rawPassword = rsaUtil.decrypt(password);
            if(rawPassword.equals(user.getPassword())){
                return ResponseEntity.ok("登录成功");
            }
        }
        return ResponseEntity.status(401).body("用户名或密码错误");
    }
}
