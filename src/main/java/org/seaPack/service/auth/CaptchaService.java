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

@Service
public class CaptchaService {

    @Autowired
    private StringRedisTemplate redisTemplate;

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

    public boolean verifyCaptcha(String token, double userX) {
        String storedX = redisTemplate.opsForValue().get("captcha:" + token);
        if (storedX == null) return false;
        boolean isValid = Math.abs(Integer.parseInt(storedX) - userX) <= 20;
        redisTemplate.delete(token);
        return isValid;
    }

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

    private String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}