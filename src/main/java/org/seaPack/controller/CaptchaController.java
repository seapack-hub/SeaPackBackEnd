package org.seaPack.controller;

import org.seaPack.dto.CaptchaDTO;
import org.seaPack.dto.VerifyDTO;
import org.seaPack.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {
    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/generate")
    public ResponseEntity<CaptchaDTO> generate() throws IOException {
        return ResponseEntity.ok(captchaService.generateCaptcha());
    }

    @PostMapping("/verify")
    public ResponseEntity<Boolean> verify(@RequestBody VerifyDTO dto) {
        boolean isValid = captchaService.verifyCaptcha(dto.getToken(), dto.getUserX());
        return ResponseEntity.ok(isValid);
    }
}
