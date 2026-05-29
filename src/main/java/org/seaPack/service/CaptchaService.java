package org.seaPack.service;

import org.seaPack.dto.CaptchaDTO;
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

@Service
public class CaptchaService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 生成滑块验证码（带缺口的背景图 + 滑块图）
     * @return 验证码DTO（含背景图Base64、滑块图Base64、token、滑块初始X/Y）
     * @throws IOException 图片读取/写入异常
     */
    public CaptchaDTO generateCaptcha() throws IOException {
        String[] images = {"bg1.jpg", "bg2.jpg", "bg3.jpg", "bg4.jpg", "bg5.jpg"};
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
     * @param token 验证码token
     * @param userX 用户拖动滑块的X坐标
     * @return true=验证通过, false=验证失败
     */
    public boolean verifyCaptcha(String token, double userX) {
        String storedX = redisTemplate.opsForValue().get("captcha:" + token);
        if (storedX == null) return false;
        boolean isValid = Math.abs(Integer.parseInt(storedX) - userX) <= 20;
        redisTemplate.delete(token);
        return isValid;
    }

    /**
     * 在图片上绘制干扰线（防机器识别）
     * @param image 原图
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
     * 将BufferedImage转为Base64编码的DataURL
     * @param image 图片
     * @return data:image/png;base64,...
     * @throws IOException 写入异常
     */
    private String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}
