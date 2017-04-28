
package lab12;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class RenderActor {
    private static Boolean hasStarted = false;
    
    static JFrame window;
    static JPanel jpanel;

    private static BufferedImage img;

    static void go(BlockingQueue<Object[]> Q) throws InterruptedException {
        if (!hasStarted){
            initialize();
        }
        while(true) {
            img = (BufferedImage)Q.take()[0];

            jpanel.getGraphics().drawImage(img, 0, 0, jpanel.getWidth(), jpanel.getHeight(), null);
        }
    }    
    
    static void initialize(){
        window = new JFrame("lab11");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jpanel = new JPanel();
        window.add(jpanel);
        window.setSize(720, 640);
        window.setVisible(true);

        System.out.println("Initialized render actor");
        hasStarted = true;
    }
}
