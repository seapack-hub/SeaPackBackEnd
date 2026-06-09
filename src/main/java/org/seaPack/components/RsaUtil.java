package org.seaPack.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 解密工具
 * <p>使用配置中的 RSA 私钥（PKCS8 格式 PEM）解密前端经公钥加密的敏感数据（如登录密码）。</p>
 */
@Component
public class RsaUtil {

    @Value("${rsa.private-key}")
    private String privateKeyPem;

    /**
     * 解析 PKCS8 格式的 PEM 私钥
     */
    private PrivateKey getPrivateKey() throws Exception {
        String privateKeyContent = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    /**
     * 解密 RSA 加密数据
     * @param encryptedData Base64 编码的密文
     * @return 解密后的 UTF-8 明文
     */
    public String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decrypted, java.nio.charset.StandardCharsets.UTF_8);
    }
}