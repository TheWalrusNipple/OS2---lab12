
package lab12;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;


public class DecodeActor {
    private static Boolean hasStarted = false;
    private static Theora.Context ctx;

    static void go(BlockingQueue<Object[]> Q) throws InterruptedException
    {
        if (!hasStarted){
            initialize(Q);
        }
        while(true){
            Theora.ImagePlane[] imgPlane = ctx.getFrame();                      //returns 3 image planes   ycbcr
            Lab11.queues.get("ColorActor").put(new Object[]{imgPlane});
        }
    }

    static void initialize(BlockingQueue<Object[]> Q) throws InterruptedException
    {
        ctx = (Theora.Context) Q.take()[0];

        System.out.println("Initialized decode actor");
        hasStarted = true;
    }
}
