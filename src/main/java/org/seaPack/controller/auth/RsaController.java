package org.seaPack.controller.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * RSA 密钥控制器
 * <p>提供 RSA 公钥获取接口，前端使用该公钥加密密码后传输，防止明文泄露。</p>
 */
@RestController
@RequestMapping("/rsa")
public class RsaController {

    @Value("${rsa.public-key}")
    private String publicKey;

    /**
     * 获取 RSA 公钥（用于前端加密登录密码）
     * @return 包含 base64 公钥字符串的 map
     */
    @GetMapping("/auth/publicKey")
    public Map<String, String> getPublicKey() {
        Map<String, String> map = new HashMap<>();
        map.put("publicKey", publicKey);
        return map;
    }
}