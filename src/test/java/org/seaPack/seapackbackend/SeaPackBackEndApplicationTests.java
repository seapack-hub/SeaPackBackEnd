package org.seaPack.seapackbackend;

import org.junit.jupiter.api.Test;
import org.seaPack.SeaPackBackEndApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

@SpringBootTest(classes = SeaPackBackEndApplication.class)
class SeaPackBackEndApplicationTests {

	@Test
	void contextLoads() {
	}

	public static void main(String[] args) throws Exception {
		// 1. 初始化密钥生成器，指定 RSA 算法和 2048 位长度
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);

		// 2. 生成密钥对
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		// 3. 获取公钥和私钥，并转换为 Base64 字符串
		String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
		String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

		// 4. 输出结果（直接复制这两行到 yml 配置文件中）
		System.out.println("=== 公钥 (Public Key) ===");
		System.out.println(publicKeyBase64);

		System.out.println("\n=== 私钥 (Private Key) ===");
		System.out.println(privateKeyBase64);
	}

}
