
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class UploadActor{

    static void go(BlockingQueue<Object[]> Q) {
        while(true){
            Object[] T=null;
            try {
                T = Q.take();
            } catch (InterruptedException ex) {
                System.exit(0);
            }
            byte[] data = (byte[]) T[0];
            if( data == null )
                return;
            String filename = (String) T[1];
            System.out.println("Uploading "+filename);
            String b64data = javax.xml.bind.DatatypeConverter.printBase64Binary(data);
            Socket s;
            try {
                //fake server...
                s = new Socket("127.0.0.1",8888);
                OutputStream os = s.getOutputStream();
                PrintWriter pw = new PrintWriter(os);
                pw.print("POST /"+filename+" HTTP/1.0\r\n");
                pw.print("Content-type: image/jpeg\r\n");
                pw.print("Content-length: "+Integer.toString(b64data.length())+"\r\n");
                pw.print("\r\n");
                pw.print(b64data);
                pw.flush();
                s.close();
            } catch (IOException ex) {
                System.out.println("Cannot communicate with server");
            }
        }
    }
}
