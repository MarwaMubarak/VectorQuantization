package com.company;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ReadWriteImage {
    public int[][] readImage(String filePath) {
        File file = new File(filePath);
        BufferedImage image;
        int width, height;
        try {
            image = ImageIO.read(file);
            width = image.getWidth();
            height = image.getHeight();
            int[][] pixels = new int[height][width];
            int rgb;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    rgb = image.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = (rgb & 0xFF);
                    pixels[y][x]=r;

                }
            }
            return pixels;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeImage(int[][] pixels, String outputFilePath) {
        File fileout = new File(outputFilePath);
        int height = pixels.length;
        int width = pixels[0].length;
        BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image2.setRGB(x, y, (pixels[y][x]));
            }
        }
        try {
            ImageIO.write(image2, "png", fileout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
