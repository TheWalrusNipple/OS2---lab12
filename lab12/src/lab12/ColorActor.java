
package lab12;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;


public class ColorActor {
    
    static Theora.ImagePlane[] imgPlane;
    static BufferedImage img;
    static void go(BlockingQueue<Object[]> Q) throws InterruptedException {

        imgPlane = (Theora.ImagePlane[]) Q.take()[0];
        int h, w;
        h = imgPlane[0].height;
        w = imgPlane[1].width;


        int[] tmp = new int[w * h];      //pixels on the screen
        int idx = 0;


        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                int rowstart = imgPlane[0].stride * row;
                int Y = (imgPlane[0].data[rowstart + col]) & 255;

                rowstart = imgPlane[1].stride * row / 2;
                int Cb = (imgPlane[1].data[rowstart + col / 2]) & 255;

                rowstart = imgPlane[2].stride * row / 2;
                int Cr = (imgPlane[2].data[rowstart + col / 2]) & 255;


                int R = clamp((int)(Y + 1.402 * (Cr - 128.0f)), 0, 255);
                int G = clamp((int)(Y - 0.344 * (Cb - 128) + 0.711 * (Cr - 128)), 0, 255);
                int B = clamp((int)(Y + 1.765 * (Cb - 128)), 0, 255);

                //It took me 2 days to figure out that this line has 6 zeros, not 5.
                //I'm dropping out now. It was nice knowing you.
                tmp[idx] = 0xff000000 | (R << 16) | (G << 8) | B;
                idx++;

            }
        }

        img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, w, h, tmp, 0, w);

        Lab11.queues.get("RenderActor").put(new Object[]{img});
    }

    private static int clamp(int value, int min, int max){
        if (value < min)
            value = min;
        else if (value > max)
            value = max;
        return value;
    }
}
