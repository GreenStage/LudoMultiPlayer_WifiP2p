package rmsf.eduardodiogo.ludo3wifi.tcpLayer;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by eduardogomes on 13/04/17.
 */

public class ClientTask extends Thread{
    private static final String TAG = "ClientSocketHandler";
    private Handler handler;
    private CommunicateTask Communicate;
    private InetAddress mAddress;
    Socket socket = null;

    public ClientTask(Handler handler, InetAddress groupOwnerAddress) {
        this.handler = handler;
        this.mAddress = groupOwnerAddress;
    }

    @Override
    public void run() {

        socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mAddress.getHostAddress(),
                    4600), 5000);
            Log.d(TAG, "Launching the I/O handler");
            Communicate = new CommunicateTask(socket, handler);
            Communicate.start();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
    }

    public CommunicateTask getMessageTask() {
        return Communicate;
    }


    public void stopThread(){

        if(Communicate != null) Communicate.stopThread();
        if(socket != null && !socket.isClosed()) try {socket.close();} catch(IOException e){e.printStackTrace();}
    }
}