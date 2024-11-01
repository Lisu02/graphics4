package org.example;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedImage image = ImageIO.read(new File("kostka.png"));
        BufferedImage editableImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = editableImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Wybierz operację: ");
        System.out.println("1. Dodawanie");
        System.out.println("2. Odejmowanie");
        System.out.println("3. Mnożenie");
        System.out.println("4. Dzielenie");
        System.out.println("5. Zmiana jasności");
        System.out.println("6. Przejście do skali szarości (metoda 1)");
        System.out.println("7. Przejście do skali szarości (metoda 2)");
        System.out.println("8. Filtr wygładzający");
        System.out.println("9. Filtr medianowy");
        System.out.println("10. Filtr wykrywania krawędzi (Sobel)");
        System.out.println("11. Filtr górnoprzepustowy wyostrzający");
        System.out.println("12. Filtr rozmycia gaussowskiego");

        int choice = scanner.nextInt();
        int value;

        switch (choice) {
            case 1:
                System.out.print("Podaj wartość do dodania: ");
                value = scanner.nextInt();
                applyOperation(editableImage, (x, y, rgb) -> addValue(rgb, value));
                break;
            case 2:
                System.out.print("Podaj wartość do odjęcia: ");
                value = scanner.nextInt();
                applyOperation(editableImage, (x, y, rgb) -> subtractValue(rgb, value));
                break;
            case 3:
                System.out.print("Podaj wartość do pomnożenia: ");
                value = scanner.nextInt();
                applyOperation(editableImage, (x, y, rgb) -> multiplyValue(rgb, value));
                break;
            case 4:
                System.out.print("Podaj wartość do podziału: ");
                value = scanner.nextInt();
                applyOperation(editableImage, (x, y, rgb) -> divideValue(rgb, value));
                break;
            case 5:
                System.out.print("Podaj poziom zmiany jasności: ");
                value = scanner.nextInt();
                applyOperation(editableImage, (x, y, rgb) -> changeBrightness(rgb, value));
                break;
            case 6:
                applyGrayscaleMethod1(editableImage);
                break;
            case 7:
                applyGrayscaleMethod2(editableImage);
                break;
            case 8:
                applySmoothingFilter(editableImage);
                break;
            case 9:
                applyMedianFilter(editableImage);
                break;
            case 10:
                applySobelFilter(editableImage);
                break;
            case 11:
                applyHighPassFilter(editableImage);
                break;
            case 12:
                System.out.print("Podaj wartość odchylenia standardowego dla rozmycia gaussowskiego: ");
                value = scanner.nextInt();
                applyGaussianBlur(editableImage, value);
                break;
            default:
                System.out.println("Nieprawidłowy wybór.");
        }

        ImageIO.write(editableImage, "png", new File("nowy.png"));
        System.out.println("Obraz został zapisany jako nowy.png");
    }

    public static void applyOperation(BufferedImage image, PixelOperation operation) {
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                int rgb = image.getRGB(j, i);
                int newRgb = operation.apply(j, i, rgb);
                image.setRGB(j, i, newRgb);
            }
        }
    }

    // Add filter methods
    public static void applySmoothingFilter(BufferedImage image) {
        float[] kernel = {
                1 / 9f, 1 / 9f, 1 / 9f,
                1 / 9f, 1 / 9f, 1 / 9f,
                1 / 9f, 1 / 9f, 1 / 9f
        };
        applyConvolutionFilter(image, kernel, 3);
    }

    public static void applyMedianFilter(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage output = new BufferedImage(width, height, image.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int[] r = new int[9];
                int[] g = new int[9];
                int[] b = new int[9];
                int index = 0;

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int rgb = image.getRGB(x + dx, y + dy);
                        r[index] = (rgb >> 16) & 0xFF;
                        g[index] = (rgb >> 8) & 0xFF;
                        b[index] = rgb & 0xFF;
                        index++;
                    }
                }

                Arrays.sort(r);
                Arrays.sort(g);
                Arrays.sort(b);
                int medianR = r[4];
                int medianG = g[4];
                int medianB = b[4];

                int newRgb = (medianR << 16) | (medianG << 8) | medianB;
                output.setRGB(x, y, newRgb);
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    output.setRGB(x, y, image.getRGB(x, y));
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, output.getRGB(x, y));
            }
        }
    }

    public static void applySobelFilter(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage output = new BufferedImage(width, height, image.getType());

        // Sobel kernels
        int[][] kernelX = {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };

        int[][] kernelY = {
                {1, 2, 1},
                {0, 0, 0},
                {-1, -2, -1}
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int gxR = 0, gxG = 0, gxB = 0;
                int gyR = 0, gyG = 0, gyB = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = image.getRGB(x + kx, y + ky);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;

                        gxR += r * kernelX[ky + 1][kx + 1];
                        gxG += g * kernelX[ky + 1][kx + 1];
                        gxB += b * kernelX[ky + 1][kx + 1];

                        gyR += r * kernelY[ky + 1][kx + 1];
                        gyG += g * kernelY[ky + 1][kx + 1];
                        gyB += b * kernelY[ky + 1][kx + 1];
                    }
                }

                int r = Math.min(255, Math.max(0, (int) Math.sqrt(gxR * gxR + gyR * gyR)));
                int g = Math.min(255, Math.max(0, (int) Math.sqrt(gxG * gxG + gyG * gyG)));
                int b = Math.min(255, Math.max(0, (int) Math.sqrt(gxB * gxB + gyB * gyB)));
                int newRgb = (r << 16) | (g << 8) | b;

                output.setRGB(x, y, newRgb);
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    output.setRGB(x, y, image.getRGB(x, y));
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, output.getRGB(x, y));
            }
        }
    }

    public static void applyHighPassFilter(BufferedImage image) {
        float[] kernel = {
                0, -1, 0,
                -1, 5, -1,
                0, -1, 0
        };
        applyConvolutionFilter(image, kernel, 3);
    }

    public static void applyGaussianBlur(BufferedImage image, float sigma) {
        int radius = (int) Math.ceil(sigma * 3);
        float[][] kernel = createGaussianKernel(radius, sigma);
        applyConvolutionFilter(image, flattenKernel(kernel), kernel.length);
    }

    private static float[][] createGaussianKernel(int radius, float sigma) {
        int size = 2 * radius + 1;
        float[][] kernel = new float[size][size];
        float sum = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                float value = (float) (Math.exp(-(x * x + y * y) / (2 * sigma * sigma)) / (2 * Math.PI * sigma * sigma));
                kernel[x + radius][y + radius] = value;
                sum += value;
            }
        }

        // Normalize the kernel
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                kernel[x][y] /= sum;
            }
        }

        return kernel;
    }

    private static float[] flattenKernel(float[][] kernel) {
        int size = kernel.length;
        float[] flatKernel = new float[size * size];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                flatKernel[x * size + y] = kernel[x][y];
            }
        }

        return flatKernel;
    }

    private static void applyConvolutionFilter(BufferedImage image, float[] kernel, int kernelSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage output = new BufferedImage(width, height, image.getType());

        for (int y = kernelSize / 2; y < height - kernelSize / 2; y++) {
            for (int x = kernelSize / 2; x < width - kernelSize / 2; x++) {
                float r = 0, g = 0, b = 0;

                for (int ky = -kernelSize / 2; ky <= kernelSize / 2; ky++) {
                    for (int kx = -kernelSize / 2; kx <= kernelSize / 2; kx++) {
                        int rgb = image.getRGB(x + kx, y + ky);
                        r += ((rgb >> 16) & 0xFF) * kernel[(ky + kernelSize / 2) * kernelSize + (kx + kernelSize / 2)];
                        g += ((rgb >> 8) & 0xFF) * kernel[(ky + kernelSize / 2) * kernelSize + (kx + kernelSize / 2)];
                        b += (rgb & 0xFF) * kernel[(ky + kernelSize / 2) * kernelSize + (kx + kernelSize / 2)];
                    }
                }

                int newR = Math.min(255, Math.max(0, (int) r));
                int newG = Math.min(255, Math.max(0, (int) g));
                int newB = Math.min(255, Math.max(0, (int) b));
                int newRgb = (newR << 16) | (newG << 8) | newB;

                output.setRGB(x, y, newRgb);
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, output.getRGB(x, y));
            }
        }
    }

    // Pixel operations
    public static int addValue(int rgb, int value) {
        int r = Math.min(255, Math.max(0, ((rgb >> 16) & 0xFF) + value));
        int g = Math.min(255, Math.max(0, ((rgb >> 8) & 0xFF) + value));
        int b = Math.min(255, Math.max(0, (rgb & 0xFF) + value));
        return (r << 16) | (g << 8) | b;
    }

    public static int subtractValue(int rgb, int value) {
        int r = Math.min(255, Math.max(0, ((rgb >> 16) & 0xFF) - value));
        int g = Math.min(255, Math.max(0, ((rgb >> 8) & 0xFF) - value));
        int b = Math.min(255, Math.max(0, (rgb & 0xFF) - value));
        return (r << 16) | (g << 8) | b;
    }

    public static int multiplyValue(int rgb, int value) {
        int r = Math.min(255, Math.max(0, ((rgb >> 16) & 0xFF) * value));
        int g = Math.min(255, Math.max(0, ((rgb >> 8) & 0xFF) * value));
        int b = Math.min(255, Math.max(0, (rgb & 0xFF) * value));
        return (r << 16) | (g << 8) | b;
    }

    public static int divideValue(int rgb, int value) {
        if (value == 0) return rgb; // Avoid division by zero
        int r = Math.min(255, Math.max(0, ((rgb >> 16) & 0xFF) / value));
        int g = Math.min(255, Math.max(0, ((rgb >> 8) & 0xFF) / value));
        int b = Math.min(255, Math.max(0, (rgb & 0xFF) / value));
        return (r << 16) | (g << 8) | b;
    }

    public static int changeBrightness(int rgb, int value) {
        return addValue(rgb, value);
    }

    public static void applyGrayscaleMethod1(BufferedImage image) {
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                int rgb = image.getRGB(j, i);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (r + g + b) / 3;
                int newRgb = (gray << 16) | (gray << 8) | gray;
                image.setRGB(j, i, newRgb);
            }
        }
    }

    public static void applyGrayscaleMethod2(BufferedImage image) {
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                int rgb = image.getRGB(j, i);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (int) (0.2989 * r + 0.5870 * g + 0.1140 * b);
                int newRgb = (gray << 16) | (gray << 8) | gray;
                image.setRGB(j, i, newRgb);
            }
        }
    }

    interface PixelOperation {
        int apply(int x, int y, int rgb);
    }
}




