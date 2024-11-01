package org.example;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.Arrays;
import java.util.Scanner;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.Scanner;

public class ImageProcessor {
    private BufferedImage image;

    // Metoda wczytująca obraz
    public void loadImage(String path) {
        try {
            image = ImageIO.read(new File(path));
            System.out.println("Obraz wczytany pomyślnie.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metoda zapisująca obraz
    public void saveImage(String path) {
        try {
            ImageIO.write(image, "png", new File(path));
            System.out.println("Obraz zapisany pomyślnie.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Dodawanie do pikseli
    public void addBrightness(int value) {
        processPixels((r, g, b) -> new int[]{
                Math.min(255, Math.max(0, r + value)),
                Math.min(255, Math.max(0, g + value)),
                Math.min(255, Math.max(0, b + value))
        });
    }

    // Odejmowanie od pikseli
    public void subtractBrightness(int value) {
        processPixels((r, g, b) -> new int[]{
                Math.max(0, r - value),
                Math.max(0, g - value),
                Math.max(0, b - value)
        });
    }

    // Mnożenie pikseli
    public void multiplyBrightness(double value) {
        processPixels((r, g, b) -> new int[]{
                (int) Math.min(255, Math.max(0, r * value)),
                (int) Math.min(255, Math.max(0, g * value)),
                (int) Math.min(255, Math.max(0, b * value))
        });
    }

    // Dzielenie pikseli
    public void divideBrightness(double value) {
        if (value == 0) return; // Sprawdzamy, aby uniknąć dzielenia przez zero
        processPixels((r, g, b) -> new int[]{
                (int) Math.min(255, Math.max(0, r / value)),
                (int) Math.min(255, Math.max(0, g / value)),
                (int) Math.min(255, Math.max(0, b / value))
        });
    }

    // Konwersja do skali szarości (metoda średnia)
    public void grayscaleAverage() {
        processPixels((r, g, b) -> {
            int gray = (r + g + b) / 3;
            return new int[]{gray, gray, gray};
        });
    }

    // Filtr wygładzający (uśredniający)
    public void smoothingFilter() {
        applyKernel(new double[][]{
                {1 / 9.0, 1 / 9.0, 1 / 9.0},
                {1 / 9.0, 1 / 9.0, 1 / 9.0},
                {1 / 9.0, 1 / 9.0, 1 / 9.0}
        });
    }

    // Metoda aplikująca dowolny kernel
    private void applyKernel(double[][] kernel) {
        int kernelSize = kernel.length;
        int offset = kernelSize / 2;
        BufferedImage tempImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = offset; y < image.getHeight() - offset; y++) {
            for (int x = offset; x < image.getWidth() - offset; x++) {
                double sumR = 0, sumG = 0, sumB = 0;

                for (int j = -offset; j <= offset; j++) {
                    for (int i = -offset; i <= offset; i++) {
                        int rgb = image.getRGB(x + i, y + j);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;

                        sumR += r * kernel[j + offset][i + offset];
                        sumG += g * kernel[j + offset][i + offset];
                        sumB += b * kernel[j + offset][i + offset];
                    }
                }

                int r = Math.min(255, Math.max(0, (int) sumR));
                int g = Math.min(255, Math.max(0, (int) sumG));
                int b = Math.min(255, Math.max(0, (int) sumB));
                tempImage.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        image = tempImage;
    }

    // Wspólna metoda do przetwarzania pikseli
    private void processPixels(PixelOperation op) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int[] newRGB = op.apply(r, g, b);
                image.setRGB(x, y, (newRGB[0] << 16) | (newRGB[1] << 8) | newRGB[2]);
            }
        }
    }

    // Interfejs dla operacji na pikselach
    @FunctionalInterface
    private interface PixelOperation {
        int[] apply(int r, int g, int b);
    }

    // Główna metoda main
    public static void main(String[] args) {
        ImageProcessor processor = new ImageProcessor();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Podaj ścieżkę do obrazu wejściowego:");
        String inputPath = scanner.nextLine();
        processor.loadImage(inputPath);

        System.out.println("Wybierz operację:\n1. Dodawanie\n2. Odejmowanie\n3. Mnożenie\n4. Dzielenie\n5. Skala szarości\n6. Wygładzanie");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1 -> {
                System.out.println("Podaj wartość do dodania:");
                int value = scanner.nextInt();
                processor.addBrightness(value);
            }
            case 2 -> {
                System.out.println("Podaj wartość do odjęcia:");
                int value = scanner.nextInt();
                processor.subtractBrightness(value);
            }
            case 3 -> {
                System.out.println("Podaj wartość do mnożenia:");
                double value = scanner.nextDouble();
                processor.multiplyBrightness(value);
            }
            case 4 -> {
                System.out.println("Podaj wartość do dzielenia:");
                double value = scanner.nextDouble();
                processor.divideBrightness(value);
            }
            case 5 -> processor.grayscaleAverage();
            case 6 -> processor.smoothingFilter();
            default -> System.out.println("Nieprawidłowy wybór");
        }

        System.out.println("Podaj ścieżkę do zapisu obrazu wyjściowego:");
        String outputPath = scanner.next();
        processor.saveImage(outputPath);
        scanner.close();
    }
}

