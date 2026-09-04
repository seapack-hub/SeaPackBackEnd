package org.seaPack;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

/**
 * 邮件发送测试
 * <p>测试邮件送达率，每轮间隔5分钟，共3轮。</p>
 */
@SpringBootTest
class EmailDeliveryTest {

    @Autowired
    private JavaMailSender mailSender;

    @Test
    void testDeliveryRate() throws Exception {
        String[] recipients = {
            "3270937741@qq.com"
        };

        for (int round = 1; round <= 3; round++) {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("3270937741@qq.com");
            msg.setTo(recipients);
            msg.setSubject("[Monitor] 系统健康检查 #round-" + round);
            msg.setText("这是一封自动化监控测试邮件。\n\n" +
                        "时间：" + LocalDateTime.now() + "\n" +
                        "状态：正常\n" +
                        "此邮件由个人监控系统自动发送，请勿回复。");
            mailSender.send(msg);
            System.out.println("第 " + round + " 轮邮件发送完成");

            if (round < 2) {
                Thread.sleep(60_000); // 每轮间隔1分钟
            }
        }
    }
}
