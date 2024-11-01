package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ZapisSzary {

    public static void main(String[] args) throws IOException {
        BufferedImage image = ImageIO.read(new File("kostka.png"));
        BufferedImage editableImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = editableImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        for (int i = 0; i < editableImage.getHeight(); i++) {
            for (int j = 0; j < editableImage.getWidth(); j++) {
                operacjaSzary(j, i, editableImage);
            }
        }

        // Write the modified image, not the original one
        ImageIO.write(editableImage, "png", new File("nowy.png"));
    }

    // Update the method to convert to grayscale
    public static void operacjaSzary(int x, int y, BufferedImage image) {
        int rgb = image.getRGB(x, y);
        // Extract RGB components
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        // Convert to grayscale using the luminosity method
        int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        // Create new RGB value
        int newRgb = (gray << 16) | (gray << 8) | gray; // Set R, G, B to gray value
        image.setRGB(x, y, newRgb);
    }

}
