package org.seaPack.components;

// RsaUtil.java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
public class RsaUtil {

    @Value("${rsa.private-key}")
    private String privateKeyPem;

    private PrivateKey getPrivateKey() throws Exception {
        // 2. 清理 PEM 字符串：移除头尾标记和换行符/空格
        String privateKeyContent = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", ""); // 移除所有空白字符（包括换行符）

        // 3. 现在对清理后的 Base64 字符串进行解码
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    public String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decrypted, java.nio.charset.StandardCharsets.UTF_8);
    }
}