package org.seapack.controller;

import org.seapack.dto.LoginRequest;
import org.seapack.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam String token,
            @RequestParam double sliderX,
            @RequestParam String username,
            @RequestParam String password
    ) {
        // 1. 先验证滑块
        if (!captchaService.verifyCaptcha(token, sliderX)) {
            return ResponseEntity.status(401).body("滑块验证失败");
        }

        // 2. 验证用户名密码
//        if (username.equals(request.getUsername()) && password.equals(request.getPassword())) {
//            return ResponseEntity.ok("登录成功");
//        }
        return ResponseEntity.status(401).body("用户名或密码错误");
    }
}
