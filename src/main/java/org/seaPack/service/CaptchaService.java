package org.seaPack.service;

import org.seaPack.dto.CaptchaDTO;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.core.io.ClassPathResource;

@Service
public class CaptchaService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    //生成带缺口的验证码
    public CaptchaDTO generateCaptcha() throws IOException {
        // 1. 随机选择背景图
        String[] images = {"bg1.jpg", "bg2.jpg", "bg3.jpg", "bg4.jpg", "bg5.jpg", };
        String imageName = images[new Random().nextInt(images.length)];

        ClassPathResource resource = new ClassPathResource("static/images/" + imageName);
        InputStream inputStream = resource.getInputStream();
        BufferedImage bgImage = ImageIO.read(inputStream);

        // 2. 创建带缺口的背景图副本
        BufferedImage bgWithGap = new BufferedImage(310,155,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bgWithGap.createGraphics();
        g2d.drawImage(bgImage, 0, 0, 310, 155,null);

        // 3. 随机生成缺口X轴、Y轴位置
        int gapX = new Random().nextInt(315 - 50);
        int gapY = new Random().nextInt(155 - 50);

        // 4. 在背景图上绘制缺口（核心新增逻辑）
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f)); // 30%不透明度
        g2d.drawImage(bgImage, gapX, gapY, 50, 50, null); // 绘制原始图像片段
        g2d.setComposite(AlphaComposite.SrcOver); // 恢复默认不透明度;
        // 创建渐变遮罩（从透明到半透明）
        GradientPaint paint = new GradientPaint(gapX, gapY, new Color(0, 0, 0, 0),gapX + 50, gapY + 50, new Color(0, 0, 0, 150));
        g2d.setPaint(paint);
        g2d.fillRect(gapX, gapY, 50, 50);
        g2d.setColor(new Color(255, 255, 255, 200)); // 半透明灰色边框
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(gapX, gapY, 50, 50);
        g2d.dispose();

        // 5. 创建滑块图（从背景图抠出缺口区域）
        BufferedImage sliderImage = bgImage.getSubimage(gapX, gapY, 50, 50);

        // 7. 设置滑块的初始X坐标,Y坐标和缺口坐标保持一致
        int sliderX = new Random().nextInt(315 - 50);

        // 7. 存储缺口位置到Redis（有效期2分钟）
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                "captcha:" + token,
                gapX+"",
                2, TimeUnit.MINUTES
        );

        // 8. 返回Base64编码的图片
        return new CaptchaDTO(imageToBase64(bgWithGap),imageToBase64(sliderImage),token,sliderX,gapY);
    }

    public boolean verifyCaptcha(String token, double userX) {
        String storedX = redisTemplate.opsForValue().get("captcha:"+token);
        if (storedX == null) return false;

        // 容差校验（±5像素内通过）
        boolean isValid = Math.abs(Integer.parseInt(storedX) - userX) <= 20;
        redisTemplate.delete(token); // 验证后立即删除
        return isValid;
    }

    // 绘制干扰线
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

    // 图片转Base64
    private String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return "data:image/png;base64," +
                Base64.getEncoder().encodeToString(baos.toByteArray());
    }

}
