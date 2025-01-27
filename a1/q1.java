package a1;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.List;
import java.awt.*;

public class q1 {

    // Parameters
    public static int t;
    public static int n;
    public static int width;
    public static int height;
    public static BufferedImage outputimage;
    // int[] is used to store the x, y, r of a circle, used to store the circles of all snowmen on the output image
    public static List<int[]> circles = new ArrayList<>();

    public static void main(String[] args) {
        
        try {
            // Check and parse input
            if (args.length != 4) {
                System.out.println("Usage: java q1 <width> <height> <number of threads> <number of snowman>");
                System.exit(1);
            }

            width = Integer.parseInt(args[0]);
            height = Integer.parseInt(args[1]);
            t = Integer.parseInt(args[2]);
            n = Integer.parseInt(args[3]);

            // once we know what size we want we can creat an empty image
            outputimage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);

            // ------------------------------------
            // Your code would go here
            
            // The easiest mechanisms for getting and setting pixels are the
            // BufferedImage.setRGB(x,y,value) and getRGB(x,y) functions.
            // Consult the javadocs for other methods.

            // The getRGB/setRGB functions return/expect the pixel value in ARGB format, one byte per channel.  For example,
            //  int p = img.getRGB(x,y);
            // With the 32-bit pixel value you can extract individual colour channels by shifting and masking:
            //  int red = ((p>>16)&0xff);
            //  int green = ((p>>8)&0xff);
            //  int blue = (p&0xff);
            // If you want the alpha channel value it's stored in the uppermost 8 bits of the 32-bit pixel value
            //  int alpha = ((p>>24)&0xff);
            // Note that an alpha of 0 is transparent, and an alpha of 0xff is fully opaque.
            
            // ------------------------------------
            
            // Create t threads
            Thread[] threads = new Thread[t];
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < t; i++) {
                threads[i] = new Thread(new Runnable() {
                    // Each thread will draw n/t snowmen
                    @Override
                    public void run() {
                        Random random = new Random();
                        for (int j = 0; j < n/t; j++) {
                            drawSnowman(random);
                        }
                    }
                });
                threads[i].start();
            }

            // join the threads
            for (int i = 0; i < t; i++) {
                threads[i].join();
            }

            long endTime = System.currentTimeMillis();
            // Print out the time taken
            System.out.println("Time taken for " + t + " threads to draw " + n + " snowmen is " + (endTime - startTime) + "ms");

            // Write out the image
            File outputfile = new File("outputimage.png");
            ImageIO.write(outputimage, "png", outputfile);

        } catch (Exception e) {
            System.out.println("ERROR " + e);
            e.printStackTrace();
        }
    }

    private static void drawSnowman(Random random) {
        // initialize snowman parameters
        // Lower bound size -> 8, Upper bound size -> 50
        int size = 8 + random.nextInt(42);
        String[] orientations = {"up", "down", "left", "right"};
        String orientation = orientations[random.nextInt(4)];
        int x, y;

        // array to store the circles of the snowman
        List<int[]> snowman;
        boolean overlap;
        boolean outOfBounds;
        do {
            x = random.nextInt(width);
            y = random.nextInt(height);
            snowman = snowmanCircles(x, y, size, orientation);

            synchronized (circles) {
                overlap = isOverlap(snowman);
                //System.out.println(Thread.currentThread().getId() + " overlap: " + overlap);
                outOfBounds = isOutOfBounds(snowman);
                //System.out.println(Thread.currentThread().getId() + " OOB " + outOfBounds);
                // add the snowman to the list of circles if it doesn't overlap with any existing snowman before any other thread checks for overlap
                if (!overlap && !outOfBounds){
                    circles.addAll(snowman);
                }
            }
        } while (overlap || outOfBounds); // if the snowman overlaps with any existing snowman or is out of bounds, rerandomize x and y

        Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        //System.out.println(Thread.currentThread().getId() + " color: " + color);

        // draw the snowman
        for (int[] circle : snowman) {
            drawCircle(circle[0], circle[1], circle[2], color);
        }
    }

    private static List<int[]> snowmanCircles(int x, int y, int size, String orientation) {
        // calculate the circles of the snowman based on the orientation
        List<int[]> snowman = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            snowman.add(new int[]{x, y, size});
            switch (orientation) {
                case "up":
                    y -= size;
                    break;
                case "down":
                    y += size;
                    break;
                case "left":
                    x -= size;
                    break;
                case "right":
                    x += size;
                    break;
            }
            // reduce size by 0.7 for each circle
            size = (int) (size * 0.7);
        }
        return snowman;
    }

    private static boolean isOverlap(List<int[]> snowmanCircles) {
        // check if the snowman overlaps with any existing snowman on the output image
        for (int[] newCircle : snowmanCircles) {
            for (int[] existingCircle : circles) {
                if (Math.pow(newCircle[0] - existingCircle[0], 2) + Math.pow(newCircle[1] - existingCircle[1], 2) 
                <= Math.pow(newCircle[2] + existingCircle[2], 2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isOutOfBounds(List<int[]> snowmanCircles) {
        // check if the snowman is out of bounds
        for (int[] circle : snowmanCircles) {
            if (circle[0] - circle[2] < 0 || circle[1] - circle[2] < 0 || circle[0] + circle[2] > width ||
             circle[1] + circle[2] > height) {
                return true;
            }
        }
        return false;
    }

    private static void drawCircle(int x, int y, int r, Color color) {
        for (int i = x - r; i <= x + r; i++) {
            for (int j = y - r; j <= y + r; j++) {
                // check if the current pixel is within the circle ((i-x)^2 + (i-y)^2 <= r^2)
                if (i >= 0 && i < width && j >= 0 && j < height && Math.pow(i - x, 2) + Math.pow(j - y, 2) 
                <= Math.pow(r, 2)) {
                    outputimage.setRGB(i, j, color.getRGB());
                }
            }
        }
    }


}