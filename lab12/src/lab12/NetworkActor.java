package lab12;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.BlockingQueue;
import javax.swing.JOptionPane;
import lab12.Theora.ImagePlane;


public class NetworkActor {
    private static Boolean hasStarted = false;              //makes initializer run only once at startup
    
    private static URL url;
    private static InputStream inputStream;
    private static byte[] inputBuffer = new byte[100];
    private static Theora.Context ctx;
    private static int bytesRead;
    //note: may need to create input buffer in the while loop
    static void go(BlockingQueue<Object[]> Q) throws MalformedURLException, IOException, InterruptedException{
        if (!hasStarted){
            initialize();
        }
        
        bytesRead = inputStream.read(inputBuffer, 0, 100);
        //get byte array from inputStream and send it on
        while (bytesRead > -1){
            //puts the read data into the theora context buffer
            //said data will be handled behind the scenes automatically by theora
            ctx.enqueueData(inputBuffer, 0, bytesRead);
            bytesRead = inputStream.read(inputBuffer, 0, 100);
        }                                                       
    }
    
    static void initialize() throws MalformedURLException, IOException, InterruptedException{
        //Prompt for a URL
        //String x = JOptionPane.showInputDialog("Site?");                      //TODO CHANGE THIS BEFORE TURNING IN
        //url = new URL(x);
        url = new URL("http://selenium.ssucet.org/04.OGG");
        URLConnection c = url.openConnection();
        //Creates input stream of data
        inputStream = c.getInputStream();
        //Creates theora context to pass to decoder on its init
        ctx = new Theora.Context();
        Lab11.queues.get("DecodeActor").put(new Object[] {ctx});
        
        System.out.println("Initialized network actor");
        hasStarted = true;
    }
}
