package com.ludo3wifi.net;

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

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ludo3wifi.net.tcpLayer.ClientTask;
import com.ludo3wifi.net.tcpLayer.CommunicateTask;
import com.ludo3wifi.net.tcpLayer.ServerTask;

/**
 * Created by eduardogomes on 13/04/17.
 */

public class GroupManager implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.GroupInfoListener {
    public static int COMMUNICATE_THREAD = 0x01;
    public static int NEW_MESSAGE = 0x02;

    public static String DISCONNECT = "DSCN_";
    public static String INVITE = "INVI_";

    WifiP2pManager mManager;
    Handler messageHandler;
    WifiP2pManager.Channel mChannel;
    int group_members = 0;
    WifiP2pDevice target;
    WifiP2pDevice mDevide;
    List<WifiP2pDevice> inviteLst = new ArrayList<>();
    List<WifiP2pDevice> groupMembers = new ArrayList<>();
    ClientTask clientTask = null;
    ServerTask serverTask = null;
    HashMap<InetAddress, CommunicateTask> membersMap = new HashMap<InetAddress, CommunicateTask>();
    public Boolean is_connected = false;
    public Boolean is_groupOwner = false;
    ConnectionListener mListener;


    public GroupManager(WifiP2pManager manager, WifiP2pManager.Channel channel, ConnectionListener listener) {

        this.mManager = manager;
        this.mChannel = channel;
        this.mListener = listener;
        this.is_connected = false;

        messageHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == GroupManager.COMMUNICATE_THREAD) {
                    Log.d("TAG", "TCP connection established");
                    Object[] obj_arr = (Object[]) msg.obj;
                    InetAddress addr = (InetAddress) obj_arr[1];
                    CommunicateTask task = (CommunicateTask) obj_arr[0];
                    membersMap.put(addr, task);
                    mListener.onTcpConnected();

                } else if (msg.what == GroupManager.NEW_MESSAGE) {
                    System.out.println("Recevied " + msg);
                    String message = (String) msg.obj;
                    String messageCONTENT = message.substring(5);
                    String messageTYPE = message.substring(0, 5);

                    if (messageTYPE.equals("POST_")) {
                        //POST TYPE MESSAGES SHOULD BE BROADCASTED TO ALL GROUP
                        if (is_groupOwner) {
                            postMsg(messageCONTENT);
                        }

                        mListener.onNewPost(messageCONTENT);

                    } else if (messageTYPE.equals("RQST_")) {
                        if (is_groupOwner) {
                            rqstMsg(messageCONTENT);
                        }
                        mListener.onNewRequest(messageCONTENT);

                    } else if(messageTYPE.equals(DISCONNECT)){
                        String infoMsg = message.substring(DISCONNECT.length());
                        String extraMsg = infoMsg.substring(0,infoMsg.indexOf('/'));
                        String addr = infoMsg.substring(infoMsg.indexOf('/') + 1);
                        mListener.onPeerDisconnected(extraMsg);
                        if(is_groupOwner){
                            for (InetAddress it : membersMap.keySet()) {
                                if (membersMap.get(it) != null)
                                    membersMap.get(it).write(DISCONNECT+ extraMsg + "/" + addr);
                            }
                        }

                    }


                }
            }
        };
    }

    public void setListener(ConnectionListener listener){
        this.mListener = listener;
    }

    /**
     * Called to re-establish a connection, in case the group-owner left.
     */
    public void reEstablishConnection(){
        String dc ="";
        if(target != null ){
            dc = target.deviceAddress;
        }
        else if(!groupMembers.isEmpty()) {
            for (WifiP2pDevice d : groupMembers) {
                if (d.deviceAddress != mDevide.deviceAddress) {
                    groupMembers.remove(d);
                    dc = d.deviceAddress;
                    break;
                }
            }
        }
        else{
            return;
        }
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = dc;
        config.wps.setup = WpsInfo.PBC;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }

    public void connect(WifiP2pDevice device) {
        target = device;

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //\mManager.requestConnectionInfo(mChannel, GroupManager.this);
            }

            @Override
            public void onFailure(int reason) {
                Log.d("TAG", "Error connecting to group " + String.valueOf(reason));
                target = null;
            }
        });

    }

    public void cancelConnect(){
  
        mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                is_connected = false;
                Log.d("TAG","Connection attempt stoped!");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("TAG","Connection attempt cant be stopped!");
            }
        });
    }
    @Override
    public void onGroupInfoAvailable(WifiP2pGroup info) {

        if (info != null && info.getClientList() != null && info.getClientList().size() > 0) {
            group_members = info.getClientList().size();
            is_groupOwner = info.isGroupOwner();
            groupMembers = new ArrayList<WifiP2pDevice>(info.getClientList());
            mListener.onGroupInfoAvailable(info);
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {

        /*This condiction must be met every time an attempt to connect failed*/
        if(p2pInfo.groupFormed){
            if (p2pInfo.isGroupOwner) {
                is_groupOwner = true;
                Log.d("TAG", "Connected as group owner");
                if(serverTask == null){
                    serverTask = new ServerTask(messageHandler);
                    serverTask.start();
                }
            } else {
                membersMap.clear();
                if(!inviteLst.contains(target)){
                    WifiP2pDevice d = new WifiP2pDevice(target);
                    inviteLst.add(d);
                }

                is_groupOwner = false;
                Log.d("TAG", "Connected as peer");
                clientTask = new ClientTask(messageHandler, p2pInfo.groupOwnerAddress);
                clientTask.start();
            }
            is_connected = true;
            mListener.onGroupConnected();
        }
    }

    public void postMsg(String message) {
        String toSend = "POST_" + message;
        for (InetAddress it : membersMap.keySet()) {
            if (membersMap.get(it) != null)
                membersMap.get(it).write(toSend);
        }

    }

    public void rqstMsg(String message) {
        String toSend = "RQST_" + message;
        for (InetAddress it : membersMap.keySet()) {
            if (membersMap.get(it) != null)
                membersMap.get(it).write(toSend);
        }
    }

    public void leaveGroup() {
        is_connected = false;
        if(mManager == null) return;
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

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

    public void Destroy() {
        is_connected = false;
        if (serverTask != null) {
            serverTask.stopThread();
            serverTask = null;
        }
        if (clientTask != null) {
            clientTask.stopThread();
            clientTask = null;
        }

        if(membersMap!= null && !membersMap.isEmpty()){
            membersMap.clear();
        }
        leaveGroup();
        if(groupMembers != null)
            groupMembers.clear();
    }

    public interface ConnectionListener {
        void onNewRequest(String message);

        void onNewPost(String message);

        void onGroupInfoAvailable(WifiP2pGroup group);

        void onPeerDisconnected(String extraMsg);

        void onGroupConnected();

        void onTcpConnected();

        void onDisconnected(String extraMsg);
    }

    //Remove existing WifiP2p Groups
    public static void deletePersistentGroups(WifiP2pManager man,WifiP2pManager.Channel chan){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(man, chan, netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void updateMyInfo(WifiP2pDevice mDevice){
        this.mDevide = mDevice;
    }

    public void disconnect(String extraMsg){
        if(mDevide == null)
            return;
        for (InetAddress it : membersMap.keySet()) {
            if (membersMap.get(it) != null)
                membersMap.get(it).write(DISCONNECT + extraMsg +"/" + mDevide.deviceAddress);

        }
        leaveGroup();
        deletePersistentGroups(mManager,mChannel);
        is_connected = false;
        groupMembers.clear();
    }

    public void onDisconnected(){
    }
}
