package com.cloud.picture.space.backend.utils;


import java.awt.*;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-12  16:54
 * @Description: TODO 工具类
 * <p>
 * 颜色相似度计算工具类，使用欧几里得距离算法
 */

public class ColorSimilarUtils {


    /**
     * 计算两个颜色之间的相似度
     *
     * @param color1 颜色1
     * @param color2 颜色2
     * @return 相似度(0-1, 越接近1表示越相似)
     */
    public static double calculateSimilarity(Color color1, Color color2) {
        int r1 = color1.getRed();
        int g1 = color1.getGreen();
        int b1 = color1.getBlue();

        int r2 = color2.getRed();
        int g2 = color2.getGreen();
        int b2 = color2.getBlue();

        // 计算欧氏距离
        double distance = Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));

        // 计算相似度
        return 1 - distance / Math.sqrt(3 * Math.pow(255, 2));
    }

    /**
     * 计算两个16进制颜色代码之间的相似度
     *
     * @param hexColor1 16进制颜色1（如：0xFF0000）
     * @param hexColor2 16进制颜色2（如：0xFF0001）
     * @return 相似度(0-1, 越接近1表示越相似)
     */
    public static double calculateSimilarity(String hexColor1, String hexColor2) {
        Color color1 = Color.decode(hexColor1);
        Color color2 = Color.decode(hexColor2);
        return calculateSimilarity(color1, color2);
    }


    // 测试,示例代码
    public static void main(String[] args) {

        // 测试颜色
        Color color1 = Color.decode("#FF0000");
        Color color2 = Color.decode("#FF0001");
        System.out.println("相似度：" + calculateSimilarity(color1, color2));
        // 测试十六进制方法
        System.out.println("相似度：" + calculateSimilarity("#FF0000", "#FF0001"));

    }


}
