package rmsf.eduardodiogo.ludo3wifi.wifiLayer;

import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rmsf.eduardodiogo.ludo3wifi.tcpLayer.ClientTask;
import rmsf.eduardodiogo.ludo3wifi.tcpLayer.CommunicateTask;
import rmsf.eduardodiogo.ludo3wifi.userInterface.RoomActivity;
import rmsf.eduardodiogo.ludo3wifi.tcpLayer.ServerTask;

/**
 * Created by eduardogomes on 13/04/17.
 */

public class GroupManager implements WifiP2pManager.ConnectionInfoListener,WifiP2pManager.GroupInfoListener {
    public static int COMMUNICATE_THREAD = 0x01;
    public static int NEW_MESSAGE = 0x02;
    WifiP2pManager mManager;
    Handler messageHandler;
    WifiP2pManager.Channel mChannel;
    int group_members = 0;
    List<WifiP2pDevice> GroupMembers;
    WifiP2pGroup mGroup;
    RoomActivity mainActv;
    ClientTask clientTask = null;
    ServerTask serverTask = null;
    HashMap<InetAddress,CommunicateTask> membersMap = new HashMap<InetAddress, CommunicateTask>();
    Boolean is_connected = false;
    Boolean is_groupOwner = false;

    public interface ConnectionListener{
        void onNewRequest(String message);
        void onNewPost(String message);
        void onGroupInfoAvailable(WifiP2pGroup group);
        void onPeerDisconnected(WifiP2pDevice peer);
        void onGroupConnected();
    }

    ConnectionListener mListener;

    public GroupManager(WifiP2pManager manager, WifiP2pManager.Channel channel, RoomActivity mainactv, ConnectionListener listener){

        this.mManager = manager;
        this.mChannel = channel;
        this.mainActv = mainactv;
        this.mListener = listener;

        messageHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == GroupManager.COMMUNICATE_THREAD){
                    Toast.makeText(mainActv, "TCP connection established",
                            Toast.LENGTH_LONG).show();
                    Object[] obj_arr = (Object[]) msg.obj;
                    InetAddress addr = (InetAddress) obj_arr[1];
                    CommunicateTask task = (CommunicateTask) obj_arr[0];
                    membersMap.put(addr,task);
                    mListener.onGroupConnected();
                }
                else if (msg.what == GroupManager.NEW_MESSAGE){

                    String message= (String) msg.obj;
                    String messageCONTENT = message.substring(5);
                    String messageTYPE = message.substring(0,5);

                    if(messageTYPE.equals("POST_")){
                        //POST TYPE MESSAGES SHOULD BE BROADCASTED TO ALL GROUP
                        if(is_groupOwner){
                            postMsg(messageCONTENT);
                        }

                        mListener.onNewPost(messageCONTENT);
                    }

                    else if(messageTYPE.equals("RQST_")){
                        if(is_groupOwner){
                            rqstMsg(messageCONTENT);
                        }

                        mListener.onNewRequest(messageCONTENT);
                    }


                }
            }
        };
    }

    public void connect(WifiP2pDevice peer){
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = peer.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mManager.requestGroupInfo(mChannel,GroupManager.this);
                mListener.onGroupConnected();
            }

            @Override
            public void onFailure(int reason) {
                Log.d("TAG","Error connecting to group " + String.valueOf(reason));
            }
        });

    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup info){

        if(info != null) {
            group_members++;
            is_groupOwner = info.isGroupOwner();
            GroupMembers = null;
            GroupMembers = new ArrayList<WifiP2pDevice>(info.getClientList());
            mListener.onGroupInfoAvailable(info);
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
        if(!is_connected){
            is_connected = true;
            if (p2pInfo.isGroupOwner) {
                is_groupOwner = true;
                Log.d("TAG", "Connected as group owner");
                serverTask = new ServerTask(messageHandler);
                serverTask.start();
            } else {
                is_groupOwner = false;
                Log.d("TAG", "Connected as peer");
                clientTask = new ClientTask(messageHandler,p2pInfo.groupOwnerAddress);
                clientTask.start();
            }
        }

    }

    public void postMsg(String message){
        String toSend = "POST_" + message;
        for (InetAddress it: membersMap.keySet()) {
            if (membersMap.get(it) != null)
                membersMap.get(it).write(toSend);
        }

    }

    public void rqstMsg(String message){
        String toSend = "RQST_" + message;
        for (InetAddress it: membersMap.keySet()) {
            if (membersMap.get(it) != null)
                membersMap.get(it).write(toSend);
        }
    }

    public void leaveGroup(){
        if (mManager != null && mChannel != null) {
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener(){
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null) {
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener(){

                            @Override
                            public void onSuccess() {

                                Log.d("TAG", "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d("TAG", "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });
        }
    }

    public void Destroy(){
        if(serverTask != null) {
            serverTask.stopThread();
            serverTask = null;
        }
        if(clientTask != null) {
            clientTask.stopThread();
            clientTask = null;
        }
        leaveGroup();
    }

}
