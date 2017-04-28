
import java.io.File;
import java.util.concurrent.BlockingQueue;
/*
 */
/**
 *
 * @author jhudson
 */
public class DirScanActor 
{
    static void go(BlockingQueue<Object[]> dsq) {
        walk(new File("."));
        System.out.println("DirScan done");
        try {
            Main.queues.get("TranscodeActor").put(new Object[]{null});
        } catch (InterruptedException ex) {
            System.exit(0);
        }   
    }
    
    static void walk(File p) {
        if(p == null )
            return;
        File[] FA = p.listFiles();
        if( FA == null )
            return;     //Maybe we don't have permission to the directory
        for(File f : p.listFiles() ){
            if( f.isDirectory() ){
                walk(f);
            }
            else if( f.getName().endsWith(".png")){
                System.out.println("Asking for transcode of"+f);
                try {
                    Main.queues.get("TranscodeActor").put(
                            new Object[]{f.getAbsolutePath().toString()});
                } catch (InterruptedException ex) {
                    System.exit(0);
                }
            }
        }
    }
}
