
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/*
 */
/**
 *
 * @author jhudson
 */
public class Main {
    static Map<String, BlockingQueue<Object[]> > queues = new TreeMap<>();

    public static void main(String[] args){
        try{
            BlockingQueue<Object[]> dsq = new LinkedBlockingQueue<>();
            BlockingQueue<Object[]> tq = new LinkedBlockingQueue<>();
            BlockingQueue<Object[]> ua = new LinkedBlockingQueue<>();
            
            queues.put("DirScanActor",dsq);
            queues.put("TranscodeActor",tq);
            queues.put("UploadActor",ua);
            Thread t1 = new Thread( () -> { DirScanActor.go(dsq); } );
            Thread t2 = new Thread( () -> { TranscodeActor.go(tq); } );
            Thread t3 = new Thread( () -> { UploadActor.go(ua); } );
            t1.start();
            t2.start();
            t3.start();
            t1.join();
            t2.join();
            t3.join();
        }
        catch(InterruptedException e){
            System.exit(0);
        }
    }
}
