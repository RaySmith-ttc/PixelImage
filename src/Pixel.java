import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class Pixel {

    public static void main(String[] args) throws Exception {
        File directory = new File("C:\\Users\\Ray\\Desktop\\pixels"); // Директория изображений
        File[] folderEntries = directory.listFiles();

        assert folderEntries != null;
        for (File file : folderEntries) {
            BufferedImage image = ImageIO.read(file);   // Исходное изображение

            int factor = 150; // Размер конечныйх пикселей

            // Обрезаем изображение (centerCrop)
            if (image.getWidth() % factor > 0 || image.getHeight() % factor > 0) {
                image = image.getSubimage(
                        (image.getWidth() % factor) / 2,
                        (image.getHeight() % factor) / 2,
                        (image.getWidth() / factor) * factor,
                        (image.getHeight() / factor) * factor
                );
            }

            List<List<Integer>> bigPixelsColors = new ArrayList<>(); // Список из 3 цветов конечного пикселя

            // Списки цветов
            List<Integer> red = new ArrayList<>();
            List<Integer> green = new ArrayList<>();
            List<Integer> blue = new ArrayList<>();

            // Обходим все пиксели изображения
            for (int i = 0; i < image.getWidth(); i++) {
                for (int j = 0; j < image.getHeight(); j++) {

                    int[] color = image.getRaster().getPixel(i, j, new int[3]); // RGB цвет пикселя
                    red.add(color[0]);
                    green.add(color[1]);
                    blue.add(color[2]);

                    // Если собраны все пиксели целевого пикселя, добовляем средние значения по цветам
                    if (i % factor == 0 && j % factor == 0) {
                        List<Integer> colors = new ArrayList<>();
                        colors.add(getAverageColor(red));
                        colors.add(getAverageColor(green));
                        colors.add(getAverageColor(blue));

                        bigPixelsColors.add(colors);
                        red.clear(); green.clear(); blue.clear();
                    }
                }
            }

            // Создаем новое изображение
            BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            int k = 0;

            // Обходим все целевые пиксели
            for (int i = 0; i < image.getWidth() / factor; i++) {
                for (int j = 0; j < image.getHeight() / factor; j++) {
                    Color color = new Color(
                            bigPixelsColors.get(k).get(0),
                            bigPixelsColors.get(k).get(1),
                            bigPixelsColors.get(k).get(2)
                    );

                    g2d.setColor(color); // Устанавливаем цвет пикселя
                    g2d.fillRect(i * factor, j * factor, factor, factor); // Рисуем квадрат
                    k++;
                }
            }

            saveImage(img, "pix.png"); // Сохраняем изображение
        }

    }


    private static void saveImage(BufferedImage template, String name) throws IOException {
        ImageIO.write(template, "png", new File("C:\\Users\\Ray\\Desktop\\" + name));
    }

    private static int getAverageColor(List<Integer> list) {
        int[] redColors = list.stream().mapToInt(Integer::intValue).toArray();
        double value = IntStream.of(redColors).average().orElse(-1);
        return value - (int)value >= 0.5 ? (int)value + 1 : (int)value;
    }
}
