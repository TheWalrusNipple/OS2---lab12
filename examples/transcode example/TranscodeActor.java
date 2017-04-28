
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import javax.imageio.ImageIO;

public class TranscodeActor {
    static void go(BlockingQueue<Object[]> Q) {
        while(true){
            String filename="";
            try {
                filename = (String) Q.take()[0];
                if( filename == null ){
                    Main.queues.get("UploadActor").put(new Object[]{null});
                    return;
                }
                System.out.println("Transcoding "+filename);
                BufferedImage img = ImageIO.read(new File(filename));
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(img,"jpg",bos);
                byte[] b = bos.toByteArray();
                Main.queues.get("UploadActor").put(new Object[]{b,filename});
            } catch (IOException ex) {
                System.out.println("Cannot load file "+filename);
            } catch (InterruptedException ex) {
                System.exit(0);
            }
        }
    }
}
