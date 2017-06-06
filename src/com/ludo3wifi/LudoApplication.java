package com.ludo3wifi;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;

import com.ludo3wifi.userInterface.peers.Buddy;
import com.ludo3wifi.net.BroadcastReceiver;
import com.ludo3wifi.net.GroupManager;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by eduardogomes on 29/05/17.
 * Main Application, in charge of registering itself on androids wifi framework
 */
public class LudoApplication extends android.app.Application {


    IntentFilter intentFilter;
    BroadcastReceiver receiver;
    private GroupManager mGroupManager;
    private WifiP2pManager mP2pManager;
    private WifiP2pManager.Channel mChannel;
    private ArrayList<Buddy> peerLST = new ArrayList<Buddy>();
    private boolean frameWorkenabled = false, wifiState = false;

    @Override

    public void onCreate() {
        super.onCreate();
        mP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        try {
            mChannel = mP2pManager.initialize(this, Looper.getMainLooper(), new WifiP2pManager.ChannelListener() {
                @Override
                public void onChannelDisconnected() {
                    frameWorkenabled = false;
                }
            });
        } catch (Exception e) {
            Log.e("TAG", "exception: " + e.getMessage());
            Log.e("TAG", "exception: " + e.toString());
        }
        peerLST.clear();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
    }

    public void startWifiFrameWork() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        frameWorkenabled = true;
    }

    public BroadcastReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(BroadcastReceiver receiver) {
        this.receiver = receiver;
    }

    public IntentFilter getIntentFilter() {
        return this.intentFilter;
    }

    public boolean getWifiState() {
        return wifiState;
    }

    public void setWifiState(boolean st) {
        this.wifiState = st;
    }

    public void addPeer(Buddy p) {
        peerLST.add(p);
    }

    public void sortPeers() {
        Collections.sort(peerLST);
    }

    public ArrayList<Buddy> getPeerLST() {
        return peerLST;
    }

    public WifiP2pManager getP2pManager() {
        return mP2pManager;
    }

    public WifiP2pManager.Channel getWifiChannel() {
        return mChannel;
    }

    public GroupManager getGroupManager() {
        return mGroupManager;
    }

    public GroupManager startGroupManager(GroupManager.ConnectionListener listener) {
        mGroupManager = new GroupManager(mP2pManager, mChannel, listener);
        return mGroupManager;
    }

}
