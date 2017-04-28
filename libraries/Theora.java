
import com.fluendo.jheora.Comment;
import com.fluendo.jheora.Info;
import com.fluendo.jheora.State;
import com.fluendo.jheora.YUVBuffer;
import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


//https://bluishcoder.co.nz/2009/06/24/reading-ogg-files-using-libogg.html
//https://bluishcoder.co.nz/2009/06/25/decoding-theora-files-using-libtheora.html
//https://github.com/doublec/plogg/blob/part2_theora/plogg.cpp


public class Theora {
    
    public static class ImagePlane {
        public int width;
        public int height;
        public int stride;
        public byte[] data;
    }
    
    public static class Context {
        private Page page = new Page();
        private StreamState streamstate=null;
        private Packet packet = new Packet();
        private Info theorainfo = new Info();
        private Comment theora_comment = new Comment();
        private State theora_context = new State(); 
        private SyncState syncstate = new SyncState();
        private BlockingQueue<byte[]> indata = new LinkedBlockingQueue<>();
        private int buffered_data_size=0;
        private boolean initialized=false;
        private boolean page_exhausted=true;
        private boolean got_bos=false;
        private boolean theora_header_decoded=false;
        private boolean theora_info_initialized=false;
        private int num_header_packets=0;
        
        private ImagePlane ip(int w, int h, int stride, short[] pix, int start){
            ImagePlane p = new ImagePlane();
            p.width = w;
            p.height = h;
            p.stride = Math.abs(stride);
            p.data = new byte[p.stride*h];
            for(int y=0;y<h;++y){
                int sidx = start + y * stride;
                int didx = y*p.stride;
                for(int x=0;x<w;++x){
                    p.data[didx+x] = (byte)(pix[sidx+x]);
                }
            }
            return p;
        }
        
        public Context(){
        }
        
        /**Get the number of bytes buffered.*/
        public int getBufferedDataSize(){
            return buffered_data_size;
        }
        
        /** Enqueue a buffer of data.
         * To indicate the end of the file, enqueue a zero-length buffer.
         * @param b The buffer.
         * @param start The index in b where the data starts.
         * @param size The amount of data to buffer.
         */
        public void enqueueData(byte[] b, int start, int size){
            try {
                //copy the data so the user can re-use the buffer
                //they sent in...
                byte[] b2 = new byte[size];
                System.arraycopy(b,start,b2,0,size);
                indata.put(b2);
                buffered_data_size += b2.length;
            } catch (InterruptedException ex) {
                throw new RuntimeException("Interrupted");
            }
        }
        /**Return the next frame.
            @return An array of 3 ImagePlane's (Y, Cb, Cr) or null if no more frames.
        */
        public ImagePlane[] getFrame(){
            while(true){
                while(page_exhausted ){
                    int rv = syncstate.pageout(page);
                    if( rv == -1 ){
                        System.out.println("Not yet synced\n");
                    }
                    else if( rv == 0 ){
                        //You must construct more pylons.
                        byte[] b;
                        int nr;
                        try{
                            b = indata.take();
                            nr = b.length;
                            buffered_data_size -= nr;
                        } 
                        catch(InterruptedException e){
                            return null;
                        }
                        if( nr == 0 || nr == -1){
                            //eof
                            return null;
                        }
                        int offs = syncstate.buffer(nr);
                        for(int i=0;i<nr;++i){
                            syncstate.data[i+offs] = b[i];
                        }
                        syncstate.wrote( nr );
                    }
                    else if( rv == 1 ){
                        //we've got a whole page
                        page_exhausted=false;
                        if( 0 != page.bos() ) {
                            if( got_bos ) 
                                throw new RuntimeException("Like a bos");
                            got_bos=true;
                            //this page begins a stream.
                            //Note that we only support single-stream files.
                            streamstate = new StreamState();
                            streamstate.init(page.serialno());
                        }
                        if(!got_bos)
                            throw new RuntimeException("Not like a bos");
                        rv = streamstate.pagein(page);
                        if( rv != 0 )
                            throw new RuntimeException("Page fault");
                        break;
                    }
                }

                while(true){
                    int rv = streamstate.packetout(packet);
                    if( rv == -1 ){
                        //not in sync
                        System.out.printf("Not work. Fix.\n");
                    }
                    else if( rv == 0 ){
                        //insufficient data
                        page_exhausted=true;
                        break;
                    }
                    else if( rv == 1 ){
                        //have packet
                       
                        if( !theora_header_decoded ){
                            rv = theorainfo.decodeHeader(theora_comment,packet);
                            num_header_packets++;
                            if( num_header_packets == 3 ){
                                theora_header_decoded=true;
                                theora_context.decodeInit(theorainfo);
                            }
                        }
                        else{
                            rv = theora_context.decodePacketin(packet);
                            if( rv == 0 ){
                                //got data
                                YUVBuffer buff = new YUVBuffer();
                                theora_context.decodeYUVout(buff);
                                ImagePlane[] r = new ImagePlane[3];
                                r[0] = ip(buff.y_width,buff.y_height,buff.y_stride,buff.data,buff.y_offset);
                                r[1] = ip(buff.uv_width,buff.uv_height,buff.uv_stride,buff.data,buff.u_offset);
                                r[2] = ip(buff.uv_width,buff.uv_height,buff.uv_stride,buff.data,buff.v_offset);
                                return r;
                            }
                        }
                    }
                    else{
                        throw new RuntimeException("?");
                    }
                }
            }
        }
    }
}

