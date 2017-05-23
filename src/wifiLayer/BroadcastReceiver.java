package rmsf.eduardodiogo.ludo3wifi.wifiLayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by eduardogomes on 12/04/17.
 */

public class BroadcastReceiver  extends android.content.BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Activity activity;
    public WifiP2pDevice self;
    private boolean isConnected;

    public interface Callback{
        void onError();
        void onConnected();
        void onDisconnected();
        void onMyInfoAvailable(WifiP2pDevice device);
    }
    Callback mCallback;
    /**
     * @param manager  WifiP2pManager system service
     * @param channel  Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public BroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                             Activity activity,final Callback callback ) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        this.mCallback = callback;
        this.isConnected = false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("TAG", action);
        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }

                NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                this.isConnected = true;
                mCallback.onConnected();
            } else if(isConnected){
                this.isConnected = false;
                mCallback.onDisconnected();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
                .equals(action)) {
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            mCallback.onMyInfoAvailable(device);

        }
        else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){

        }
        else if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state != WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                mCallback.onError();
            }
        }
    }
}
