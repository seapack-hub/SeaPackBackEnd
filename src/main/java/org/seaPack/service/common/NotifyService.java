package org.seaPack.service.common;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.system.User;
import org.seaPack.service.system.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 消息推送服务
 * <p>
 * 当股息率触发阈值时，根据用户配置的邮箱和手机号发送告警通知。
 * 邮件走 SMTP 真实发送，SMS 当前为日志输出（需对接第三方短信平台后启用）。
 * </p>
 */
@Slf4j
@Service
public class NotifyService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserService userService;

    @Value("${notify.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${notify.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送股息率告警通知（邮件 + SMS）
     *
     * @param userId       用户 ID
     * @param stockCode    股票代码
     * @param stockName    股票名称
     * @param triggeredRate 触发时的股息率(%)
     * @param triggeredPrice 触发时的股价
     * @param thresholdRate 阈值(%)
     * @param triggerType  CROSS_UP / CROSS_DOWN
     */
    public void sendAlert(Long userId, String stockCode, String stockName,
                          String triggeredRate, String triggeredPrice,
                          String thresholdRate, String triggerType) {

        User user = userService.getUserById(userId);
        if (user == null) {
            log.warn("用户 {} 不存在，跳过通知", userId);
            return;
        }

        String subject = String.format("【股息率告警】%s(%s)", stockName, stockCode);
        String direction = "CROSS_UP".equals(triggerType) ? "向上突破" : "向下跌破";
        String body = String.format(
                "<h3>股息率阈值告警</h3>" +
                "<table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse;'>" +
                "<tr><td>股票</td><td>%s(%s)</td></tr>" +
                "<tr><td>当前股息率</td><td style='color:red;font-weight:bold;'>%s%%</td></tr>" +
                "<tr><td>触发类型</td><td>%s</td></tr>" +
                "<tr><td>设定阈值</td><td>%s%%</td></tr>" +
                "<tr><td>触发时股价</td><td>%s</td></tr>" +
                "</table><br/><small>SeaPack 监控系统自动发送</small>",
                stockName, stockCode, triggeredRate, direction, thresholdRate, triggeredPrice);

        // 优先邮件通知，邮件为空时降级为手机号通知
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            if (emailEnabled) {
                try {
                    sendEmail(user.getEmail(), subject, body);
                    log.info("邮件已发送至 {}", user.getEmail());
                } catch (Exception e) {
                    log.error("邮件发送失败 to={}, error={}", user.getEmail(), e.getMessage());
                }
            } else {
                log.info("邮件通知未启用，跳过邮箱 {}", user.getEmail());
            }
        } else if (user.getMobile() != null && !user.getMobile().isEmpty()) {
            if (smsEnabled) {
                sendSms(user.getMobile(), stockCode, triggeredRate, direction, thresholdRate);
            } else {
                log.info("SMS 通知未启用，跳过手机号 {}", user.getMobile());
            }
        } else {
            log.warn("用户 {} 无手机号也无邮箱，无法发送通知", userId);
        }
    }

    private void sendEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }

    private void sendSms(String mobile, String stockCode,
                         String triggeredRate, String direction, String thresholdRate) {
        String text = String.format("【SeaPack】%s 股息率 %s%%，%s阈值 %s%%",
                stockCode, triggeredRate, direction, thresholdRate);
        log.info("[SMS 模拟] 发送至 {}: {}", mobile, text);
    }
}
