package lab12;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Lab11 {
    static Map<String, BlockingQueue<Object[]>> queues = new TreeMap<>();

    public static void main(String[] args) {
        try{
            BlockingQueue<Object[]> network = new LinkedBlockingQueue<>();
            BlockingQueue<Object[]> decoder = new LinkedBlockingQueue<>();
            BlockingQueue<Object[]> color   = new LinkedBlockingQueue<>();
            BlockingQueue<Object[]> renderer= new LinkedBlockingQueue<>();

            queues.put("NetworkActor", network);
            queues.put("DecodeActor", decoder);
            queues.put("ColorActor", color);
            queues.put("RenderActor", renderer);

            Thread netThread = new Thread( () -> {try {
                NetworkActor.go(network);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(Lab11.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Lab11.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Lab11.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            Thread decThread = new Thread( () -> {try {
                DecodeActor.go(decoder);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Lab11.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            Thread colThread = new Thread( () -> {try {
                ColorActor.go(color);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Lab11.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            Thread renThread = new Thread( () -> {
                try {
                    RenderActor.go(renderer);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            
            netThread.start();
            decThread.start();
            colThread.start();
            renThread.start();
            netThread.join();
            decThread.join();
            colThread.join();
            renThread.join();
        }
        catch(InterruptedException e){
            System.exit(0);
        }
    }   
}
