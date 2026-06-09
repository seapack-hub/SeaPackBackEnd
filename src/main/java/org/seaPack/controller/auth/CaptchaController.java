package org.seaPack.controller.auth;

import org.seaPack.dto.auth.CaptchaDTO;
import org.seaPack.dto.auth.VerifyDTO;
import org.seaPack.service.auth.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 滑块验证码控制器
 * <p>提供滑块验证码的生成与校验功能，用于登录、注册等场景的人机验证。</p>
 */
@RestController
@RequestMapping("/captcha")
public class CaptchaController {
    @Autowired
    private CaptchaService captchaService;

    /**
     * 生成滑块验证码
     * <p>返回包含背景图、滑块图及 token 的 DTO，前端据此渲染滑块验证组件。</p>
     */
    @GetMapping("/generate")
    public ResponseEntity<CaptchaDTO> generate() throws IOException {
        return ResponseEntity.ok(captchaService.generateCaptcha());
    }

    /**
     * 校验滑块验证码
     * <p>验证用户拖拽滑块的位置偏差是否在允许误差范围内。</p>
     * @param dto 包含 token 和用户拖拽 x 坐标
     */
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verify(@RequestBody VerifyDTO dto) {
        boolean isValid = captchaService.verifyCaptcha(dto.getToken(), dto.getUserX());
        return ResponseEntity.ok(isValid);
    }
}