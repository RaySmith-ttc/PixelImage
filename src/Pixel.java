import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Pixel {

    public static void main(String[] args) throws Exception {
        File directory = new File("C:\\Users\\Ray\\Desktop\\re"); // Директория изображений
        File[] folderEntries = directory.listFiles();

        assert folderEntries != null;
        int index = 525;
        for (File file : folderEntries) {
            //BufferedImage image = ImageIO.read(file);   // Исходное изображение



            List<Float> factors = new ArrayList<>(Arrays.asList(1.0F, 1.5F, 2.0F, 2.5F, 3.5F, 4.5F, 5.5F));
            List<Boolean> isWalls = new ArrayList<>(Arrays.asList(false, false, false, true, true, true, true));
            int p = -1;
            for (float preFactor : factors) {
                p++;
                BufferedImage image = ImageIO.read(file);   // Исходное изображение
                int size = image.getHeight() < image.getWidth() ? image.getHeight() : image.getWidth();
                int factor = (int) (size / (6 * preFactor));

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

                List<Integer> allRed = new ArrayList<>();
                List<Integer> allGreen = new ArrayList<>();
                List<Integer> allBlue = new ArrayList<>();

                // Обходим все пиксели изображения
                for (int i = 0; i < image.getWidth(); i++) {
                    for (int j = 0; j < image.getHeight(); j++) {
                        int[] color; // RGB цвет пикселя
                        try {
                            color = image.getRaster().getPixel(i, j, new int[3]);
                        } catch (Exception e) {
                            color = new int[]{255, 255, 255};
                        }
                        red.add(color[0]);
                        green.add(color[1]);
                        blue.add(color[2]);

                        allRed.add(color[0]);
                        allGreen.add(color[1]);
                        allBlue.add(color[2]);

                        // Если собраны все пиксели целевого пикселя, добовляем средние значения по цветам
                        if (i % factor == 0 && j % factor == 0) {
                            List<Integer> colors = new ArrayList<>();
                            colors.add(getAverageColor(red));
                            colors.add(getAverageColor(green));
                            colors.add(getAverageColor(blue));

                            bigPixelsColors.add(colors);
                            red.clear();
                            green.clear();
                            blue.clear();
                        }
                    }
                    red.clear();
                    green.clear();
                    blue.clear();
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

                StringBuilder name = new StringBuilder();
                String[] path = file.getName().split("[.]");
                ;
                for (int i = 0; i < path.length - 1; i++) {
                    name.append(path[i]);
                }

                Color c = new Color(getAverageColor(allRed), getAverageColor(allGreen), getAverageColor(allBlue));

                if (p == 0) {
                    System.out.println("INSERT INTO levels (answer, color) VALUES ('" + name + "', '" + c.getRGB() + "');");
                    System.out.println("INSERT INTO post_levels (answer) VALUES ('" + name + "');");
                }

                Image iimage;
                if (isWalls.get(p)) {
                    iimage = img.getScaledInstance(807, -1, Image.SCALE_SMOOTH);
                } else {
                    iimage = img.getScaledInstance(347, -1, Image.SCALE_SMOOTH);
                }
                image = new BufferedImage(iimage.getWidth(null), iimage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D bGr = image.createGraphics();
                bGr.drawImage(iimage, 0, 0, null);
                bGr.dispose();
                saveImage(image, index + "_" + preFactor + ".jpg"); // Сохраняем изображение

                if (p == 0) {
                    image = ImageIO.read(file);
                    iimage = image.getScaledInstance(347, -1, Image.SCALE_SMOOTH);
                    image = new BufferedImage(iimage.getWidth(null), iimage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                    bGr = image.createGraphics();
                    bGr.drawImage(iimage, 0, 0, null);
                    bGr.dispose();
                    saveImage(image, index + "_orig.jpg"); // Сохраняем изображение
                }
            }
            index++;
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
