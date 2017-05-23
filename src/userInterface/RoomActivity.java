package rmsf.eduardodiogo.ludo3wifi.userInterface;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import rmsf.eduardodiogo.ludo3wifi.R;
import rmsf.eduardodiogo.ludo3wifi.wifiLayer.Service;
import rmsf.eduardodiogo.ludo3wifi.wifiLayer.BroadcastReceiver;
import rmsf.eduardodiogo.ludo3wifi.wifiLayer.GroupManager;

public class RoomActivity extends AppCompatActivity {
    public enum Mode{ roomClient, roomOwner};
    Mode mode;
    IntentFilter intentFilter;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    GroupManager mGroupManager;
    String userName;
    String roomName;
    Service mService;
    Buddy self;
    ArrayList<Buddy> peers;
    HashMap<String,Buddy> Peermap;
    BroadcastReceiver receiver;
    RoomFrag openRoomFrag, gameFrag, currentFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_room);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        try {
            mChannel = mManager.initialize(this, getMainLooper(), null);
        }catch( Exception e){
            Log.e("MYAPP", "exception: " + e.getMessage());
            Log.e("MYAPP", "exception: " + e.toString());
        }


        Intent intent = getIntent();
        this.userName = intent.getStringExtra("USERNAME");
        this.roomName = intent.getStringExtra("ROOMNAME");

        peers = new ArrayList<Buddy>();
        FragmentManager fm = getSupportFragmentManager();
        openRoomFrag = new OpenRoomFragment();
        currentFrag = openRoomFrag;
        FragmentTransaction transaction=  fm.beginTransaction();
        transaction.replace(R.id.roomContainer, openRoomFrag);
        transaction.addToBackStack(null);
        transaction.commit();
        //CREATE GROUPMANAGER


        mGroupManager = new GroupManager(mManager, mChannel, this, new GroupManager.ConnectionListener() {

            @Override
            public void onGroupConnected(){ //SAY HI
                int stype = (mode == Mode.roomOwner) ? Buddy.TYPE_OWNER : Buddy.TYPE_PEER;
                //Send my info & Request all users info
                mGroupManager.rqstMsg("INFO_" + String.valueOf(stype) + userName);
            }

            @Override
            public void onNewPost(String message){
                String messageSubType = message.substring(0,5);
                if(messageSubType.substring(0,5).equals("USER_")){
                    Buddy aux;
                    aux = new Buddy(null,message.substring(6),"in room",Integer.valueOf(message.substring(5,6) ) & ~Buddy.TYPE_SELF);
                    if(!aux.Username.equals(userName) && !peers.contains(aux)){
                        peers.add(aux);
                        Collections.sort(peers);
                        currentFrag.updatePeers();
                    }
                }
                else if (messageSubType.substring(0,5).equals("STRT_")){
                    if(mode != Mode.roomOwner){
                        Log.d("TAG",messageSubType);
                        startGame(Integer.valueOf(message.substring(5)));
                    }

                }
                else if(messageSubType.substring(0,5).equals("ACTN_")){
                    currentFrag.rcvMsg(message.substring(5));

                }

                Log.d("MSG",message);
            }

            @Override
            public void onNewRequest(String message) {

                String messageSubType = message.substring(0,5);

                if(messageSubType.equals("INFO_") && mode == Mode.roomOwner) { //SOMEONE ASKED FOR INFO
                    if (message.length() > 5) {
                        String infomsg = message.substring(5);
                        Buddy aux;
                        aux = new Buddy(null, infomsg.substring(1), "in room", Integer.valueOf(infomsg.substring(0, 1)) & (~Buddy.TYPE_SELF));
                        if (!aux.Username.equals(userName) && !peers.contains(aux)) {
                            peers.add(aux);
                            Collections.sort(peers);
                            currentFrag.updatePeers();
                        }
                    }
                    for (Buddy it : peers) {
                        mGroupManager.postMsg("USER_" + String.valueOf(it.type & (~Buddy.TYPE_SELF)) + it.Username);
                    }

                }
                else if(messageSubType.equals("ACTN_") && mode == Mode.roomOwner){
                    currentFrag.rcvMsg("TRY_" + message.substring(5));
                }
                Log.d("MSG",message);
            }

            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if(group.isGroupOwner()){

                }
            }

            @Override
            public void onPeerDisconnected(WifiP2pDevice peer){

            }
        });


        if(intent.getStringExtra("STATUS").equals("Peer")){
            Buddy owner = (Buddy) intent.getParcelableExtra("OWNER");
            if(owner == null){
                Log.d("TAG","BUDDY NULL!!");
            }
            mode = Mode.roomClient;
            mGroupManager.connect(owner.deviceInfo);

        }else{
            mode = Mode.roomOwner;

        }

        mService = new Service(userName,roomName,mode == Mode.roomOwner,mManager,mChannel,new Service.MessageHandler(){
            @Override
            public void onMessageAvailable(String message){
                Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT);
            }
        });

        TextView textView = (TextView) findViewById(R.id.roomname);
        textView.setText(roomName);

        self = new Buddy(null,userName,"in room",Buddy.TYPE_SELF |
                ( (mode == Mode.roomOwner) ? Buddy.TYPE_OWNER : Buddy.TYPE_PEER) );
        peers.add(self);


    }

    @Override
    public void onStart(){
        super.onStart();

    }

    public void startGame(int id){

        FragmentManager fm =  getSupportFragmentManager();
        gameFrag = new GameRoomFrag();
        Bundle args = new Bundle();
        args.putInt("userID",id);
        gameFrag.setArguments(args);

        currentFrag = gameFrag;
        FragmentTransaction transaction=  fm.beginTransaction();
        transaction.replace(R.id.roomContainer, gameFrag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    public void setMode(Mode mode_){
        this.mode = mode_;
    }

    @Override
    public void onResume() {
        super.onResume();

        receiver = new BroadcastReceiver(mManager, mChannel, this, new BroadcastReceiver.Callback() {
            @Override
            public void onConnected() {
                mManager.requestConnectionInfo(mChannel,
                        (WifiP2pManager.ConnectionInfoListener) mGroupManager);
                mManager.requestGroupInfo(mChannel, mGroupManager);
            }
            @Override
            public void onDisconnected(){
                Toast.makeText(RoomActivity.this, "Disconnected from Group.",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onMyInfoAvailable(WifiP2pDevice device){
                if(self == null)
                    self.setDevice(device);
            }
            @Override
            public void onError(){

            }
        });

        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void startGame(){
        if(mode == Mode.roomOwner){
            int it = 1;
            for(Buddy p : peers){
                if(!p.equals(self)){

                    mGroupManager.postMsg("STRT_" + String.valueOf(it));
                }
                it++;
            }
        }
        startGame(0);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void finish(){
        mGroupManager.Destroy();
        mService.stop();

        super.finish();
    }

    public void SendTestMsg(String msg){
        mGroupManager.postMsg(msg);
    }

    public void SendAction(){

    }

}
