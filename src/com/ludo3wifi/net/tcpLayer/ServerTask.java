package com.ludo3wifi.net.tcpLayer;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by eduardogomes on 13/04/17.
 */

public class ServerTask extends Thread {
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor( 4, 4, 10, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
    ServerSocket socket = null;
    Handler mHandler;

    public ServerTask(Handler handler) {
        this.mHandler = handler;
        try {
            socket = new ServerSocket(4600);
        } catch (IOException e) {
            Log.d("TAG", "EXECPTIOn " + e.getLocalizedMessage());
        }
        Log.d("TAG", "SUCESS");
    }

    @Override
    public void run() {
        while (true) {
            try {
                pool.execute(new CommunicateTask(socket.accept(), mHandler));
            } catch (IOException e) {
                try {
                    if (socket != null && !socket.isClosed())
                        socket.close();
                } catch (IOException ioe) {
                    e.printStackTrace();
                }
                pool.shutdownNow();
                break;
            }
        }
    }

    public void stopThread() {
        if (socket != null && !socket.isClosed()) try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}