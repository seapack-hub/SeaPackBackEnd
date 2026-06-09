package org.seaPack.controller.auth;

import org.seaPack.components.RsaUtil;
import org.seaPack.model.system.User;
import org.seaPack.service.auth.CaptchaService;
import org.seaPack.service.system.UserService;
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
        if (!isVerify && !captchaService.verifyCaptcha(token, sliderX)) {
            return ResponseEntity.status(200).body("滑块验证失败");
        }

        if(!username.isEmpty() && !password.isEmpty()){
            User user = userService.selectUserByName(username);
            if (user == null) return ResponseEntity.status(401).body("用户名错误");

            String rawPassword = rsaUtil.decrypt(password);
            if(rawPassword.equals(user.getPassword())){
                return ResponseEntity.ok("登录成功");
            }
        }
        return ResponseEntity.status(401).body("用户名或密码错误");
    }
}