package org.seaPack.controller;
// RsaController.java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/rsa")
public class RsaController {

    @Value("${rsa.public-key}")
    private String publicKey;

    @GetMapping("/auth/publicKey")
    public Map<String, String> getPublicKey() {
        Map<String, String> map = new HashMap<>();
        map.put("publicKey", publicKey);
        return map;
    }
}
