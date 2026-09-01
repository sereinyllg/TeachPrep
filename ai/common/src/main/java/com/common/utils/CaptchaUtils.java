package com.common.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 验证码工具类
 */
public class CaptchaUtils {
    // 验证码字符集（排除易混淆字符）
    private static final String CHAR_SET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int WIDTH = 120;    // 图片宽度 
    private static final int HEIGHT = 40;    // 图片高度 
    private static final int LENGTH = 4;     // 验证码长度 
    private static final int LINE_COUNT = 10; // 干扰线数量

    /**
     * 生成验证码图片（Base64编码）
     * @return 包含验证码文本和图片Base64的Map
     */
    public static Map<String, String> generateCaptcha() {
        // 1. 创建内存图像 
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 2. 绘制背景（白色）
        g.setColor(Color.WHITE);
        g.fillRect(0,  0, WIDTH, HEIGHT);

        // 3. 绘制干扰线（随机颜色）
        Random random = new Random();
        for (int i = 0; i < LINE_COUNT; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.setColor(getRandomColor());
            g.drawLine(x1,  y1, x2, y2);
        }

        // 4. 生成随机验证码 
        StringBuilder captchaText = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            // 随机字符 
            char c = CHAR_SET.charAt(random.nextInt(CHAR_SET.length()));
            captchaText.append(c);
            // 绘制字符（随机颜色、位置和旋转）
            g.setColor(getRandomColor());
            g.setFont(new  Font("Arial", Font.BOLD, 30));
            g.rotate(Math.toRadians(random.nextInt(30)  - 15), 25 * i + 10, 25);
            g.drawString(String.valueOf(c),  25 * i + 10, 25);
            g.rotate(-Math.toRadians(random.nextInt(30)  - 15), 25 * i + 10, 25);
        }

        // 5. 释放资源 
        g.dispose();

        // 6. 转换为Base64 
        String base64Image = imageToBase64(image);

//        // 7. 返回结果
//        return Map.of(
//                "text", captchaText.toString(),  // 验证码文本（需存入Redis等）
//                "image", "data:image/png;base64," + base64Image // 可直接在HTML中显示的Base64
//        );

        //"data:image/png;base64,"
        HashMap<String, String> map = new HashMap<>();
        map.put("text", captchaText.toString());
        map.put("image", base64Image);
        return map;
    }

    /**
     * BufferedImage 转 Base64 
     */
    private static String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image,  "png", os);
            return Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("验证码生成失败", e);
        }
    }

    /**
     * 生成随机颜色（避免浅色）
     */
    private static Color getRandomColor() {
        Random random = new Random();
        return new Color(
                random.nextInt(150),
                random.nextInt(150),
                random.nextInt(150)
        );
    }
}