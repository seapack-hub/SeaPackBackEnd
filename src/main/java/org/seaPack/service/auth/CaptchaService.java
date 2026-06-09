package org.seaPack.service.auth;

import org.seaPack.dto.auth.CaptchaDTO;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.core.io.ClassPathResource;

/**
 * 滑块验证码服务
 * <p>随机选取背景图，在随机位置抠出滑块区域并半透明遮盖，
 * 将正确位置存入 Redis（2 分钟过期），前端拖拽后校验偏差是否在 20px 内。</p>
 */
@Service
public class CaptchaService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 生成滑块验证码
     * <p>从 classpath:static/images/ 下随机选取背景图，生成带缺口背景和滑块图，
     * 正确缺口位置存入 Redis。</p>
     * @return 包含背景图、滑块图、token 和滑块初始坐标的 DTO
     */
    public CaptchaDTO generateCaptcha() throws IOException {
        String[] images = {"bg1.jpg", "bg2.jpg", "bg3.jpg", "bg4.jpg", "bg5.jpg"};
        String imageName = images[new Random().nextInt(images.length)];

        ClassPathResource resource = new ClassPathResource("static/images/" + imageName);
        InputStream inputStream = resource.getInputStream();
        BufferedImage bgImage = ImageIO.read(inputStream);

        BufferedImage bgWithGap = new BufferedImage(310,155,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bgWithGap.createGraphics();
        g2d.drawImage(bgImage, 0, 0, 310, 155,null);

        int gapX = new Random().nextInt(315 - 50);
        int gapY = new Random().nextInt(155 - 50);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.drawImage(bgImage, gapX, gapY, 50, 50, null);
        g2d.setComposite(AlphaComposite.SrcOver);
        GradientPaint paint = new GradientPaint(gapX, gapY, new Color(0, 0, 0, 0),
                gapX + 50, gapY + 50, new Color(0, 0, 0, 150));
        g2d.setPaint(paint);
        g2d.fillRect(gapX, gapY, 50, 50);
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(gapX, gapY, 50, 50);
        g2d.dispose();

        BufferedImage sliderImage = bgImage.getSubimage(gapX, gapY, 50, 50);
        int sliderX = new Random().nextInt(315 - 50);

        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("captcha:" + token, gapX + "", 2, TimeUnit.MINUTES);

        return new CaptchaDTO(imageToBase64(bgWithGap), imageToBase64(sliderImage), token, sliderX, gapY);
    }

    /**
     * 校验滑块验证码
     * @param token 验证会话 token
     * @param userX 用户拖拽的 x 坐标
     * @return 是否校验通过（偏差 &le; 20px）
     */
    public boolean verifyCaptcha(String token, double userX) {
        String storedX = redisTemplate.opsForValue().get("captcha:" + token);
        if (storedX == null) return false;
        boolean isValid = Math.abs(Integer.parseInt(storedX) - userX) <= 20;
        redisTemplate.delete(token);
        return isValid;
    }

    /**
     * 在图片上绘制干扰线条（增强防机器识别能力）
     */
    private void drawInterferenceLines(BufferedImage image) {
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2));
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(image.getWidth());
            int y1 = random.nextInt(image.getHeight());
            int x2 = random.nextInt(image.getWidth());
            int y2 = random.nextInt(image.getHeight());
            g2d.drawLine(x1, y1, x2, y2);
        }
        g2d.dispose();
    }

    /**
     * BufferedImage 转 base64 字符串（含 data:image/png 前缀）
     */
    private String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}