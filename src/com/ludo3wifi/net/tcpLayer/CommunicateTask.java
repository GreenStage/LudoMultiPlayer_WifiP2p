package com.ludo3wifi.net.tcpLayer;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.ludo3wifi.net.GroupManager;

/**
 * Created by eduardogomes on 13/04/17.
 */

public class CommunicateTask extends Thread {
    private static final String TAG = "ChatHandler";
    static private Pattern MsgPattern = Pattern.compile("#_.*_#.*");
    private Socket socket = null;
    private boolean stop_signal = false;
    private Handler handler;
    private InputStream iStream;
    private OutputStream oStream;

    private class writeThread extends Thread{
        public String writeMsg;

        public writeThread(String msg){
            writeMsg = msg;
        }
        @Override
        public void run(){
            writeMsg = "#_" + writeMsg + "_#";
            byte[] buffer = writeMsg.getBytes();
            try {
                oStream = socket.getOutputStream();
                oStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }
    }


    public CommunicateTask(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }


    @Override
    public void run() {
        try {

            iStream = socket.getInputStream();

            int bytes;
            int inx;
            String msg;
            Object[] info = new Object[2];

            info[0] = (Object) this;
            info[1] = (Object) socket.getInetAddress();

            byte[] buffer = new byte[256];

            handler.obtainMessage(GroupManager.COMMUNICATE_THREAD, info)
                    .sendToTarget();

            while (!stop_signal) {
                try {
                    // Read from the InputStream
                    bytes = iStream.read(buffer);

                    if (bytes == -1) {
                        break;
                    }

                    msg = new String(buffer, 0, bytes);

                    /*Check if bytes message follows the protocol and is complete.
                      If so, send the complete message up the App's message handler
                      and clean the buffer */
                    if (MsgPattern.matcher((msg = new String(buffer, 0, bytes))).matches()) {

                        Log.d(TAG, "Rec:" + String.valueOf(buffer));
                        inx = msg.indexOf("_#");

                        Arrays.fill(buffer, (byte) '0');

                        /*if there is still data ,refill buffer with leftover data*/
                        if (msg.length() > inx + 2) {
                            String dummy = msg.substring(inx + 2);
                            byte cpy[] = msg.substring(inx + 2).getBytes();
                            System.arraycopy(cpy, 0, buffer, 0, cpy.length);
                        }

                        handler.obtainMessage(GroupManager.NEW_MESSAGE,
                                bytes, -1, msg.substring(2, inx)).sendToTarget();

                    }

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(String message) {
        /*Create a task to send the message*/
        writeThread wt = new writeThread(message);
        wt.start();
    }

    public void stopThread() {
        stop_signal = true;
    }
}
