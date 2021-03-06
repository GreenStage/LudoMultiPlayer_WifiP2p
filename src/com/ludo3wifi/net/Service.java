package com.ludo3wifi.net;

import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Handler;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import com.ludo3wifi.userInterface.peers.Buddy;

/**
 * Created by eduardogomes on 16/04/17.
 * Service Class, used to create a local service and ensure that it is visible on all our range
 */

public class Service {
    public static final String SERVICE_DESCP = "GameService";
    boolean success;
    boolean discover;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    String musername, mroomname;
    boolean isOwner;
    int mtype;
    MessageHandler handler;
    Handler mHandler = new Handler();

    public Service(String username, String roomname, boolean is_owner, WifiP2pManager manager, WifiP2pManager.Channel channel, final MessageHandler callback) {
        success = false;
        this.musername = username;
        this.mroomname = roomname;
        this.mManager = manager;
        this.mChannel = channel;
        this.handler = callback;
        this.isOwner = is_owner;
        this.mtype = (is_owner) ? Buddy.TYPE_OWNER : Buddy.TYPE_PEER;
    }

    public void start(){
        discover = true;
        mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Map record = new HashMap();
                record.put("username", musername);
                record.put("roomname", mroomname);
                record.put("type", String.valueOf(mtype));
                WifiP2pDnsSdServiceInfo serviceInfo =
                        WifiP2pDnsSdServiceInfo.newInstance(SERVICE_DESCP, "_presence._tcp", record);
                mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("TAG", "local service added");
                        handler.onMessageAvailable("Local Service Started");

                        //Hence android has this bug that devices only discover a service if the least starts
                        //after discoverservices being initiated , we need to make a background thread to
                        // force localservices to rebroadcast its information periodically
                        mHandler.post(discoverloop);
                    }

                    @Override
                    public void onFailure(int arg0) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                        Log.d("TAG", "error starting local service" + String.format("Error reason %d", arg0));
                        handler.onMessageAvailable("Failed to start local service ");
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                Log.d("TAG", "Fail to stop previous local service");
                handler.onMessageAvailable("Failed to stop previous local service " + Integer.toString(reason));
            }
        });
    }

    public void stop() {
        discover = false;
        mManager.clearLocalServices(mChannel, null);
        //mHandler.removeCallbacks(discoverloop);
    }

    private Runnable discoverloop = new Runnable() {
        @Override
        public void run() {
            if(discover){
                Log.d("TAG", "Ping");
                mManager.discoverPeers(mChannel, null);
                mHandler.postDelayed(discoverloop, 4000);
            }

        }
    };

    public interface MessageHandler {
        void onMessageAvailable(String message);
    }
}
