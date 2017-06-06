package com.ludo3wifi.userInterface;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ludo3wifi.LudoApplication;
import com.ludo3wifi.R;
import com.ludo3wifi.net.BroadcastReceiver;
import com.ludo3wifi.net.GroupManager;
import com.ludo3wifi.net.Service;
import com.ludo3wifi.userInterface.peers.Buddy;
import com.ludo3wifi.userInterface.peers.BuddyListAdapter;

/**
 * Open Room activity, when a user enters a room
 * Called from MainMenu activity
 */
public class RoomActivity extends Activity {
    public static final int CONNECTION_TIME_OUT = 40000; //40 Seconds to connect
    public static final String INFO_MSG = "INFO_";
    public static final String USER_MSG = "USER_";
    public static final String START_MSG = "STRT_";

    public enum Mode {roomClient, roomOwner}
    Mode mode = Mode.roomClient;
    LudoApplication mApplication;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    GroupManager mGroupManager;
    String userName;
    String roomName;
    Service mService;
    Buddy self;
    ListView listView;
    BuddyListAdapter mUserListAdapter;
    ProgressDialog connectionDialog;
    boolean connected1 = false, connected2 = false;
    boolean leave = false, announce = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Manage window and view settings*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_room);

        /*Fetch application and network manager instances*/
        mApplication = (LudoApplication) getApplication();
        mManager = mApplication.getP2pManager();
        mChannel = mApplication.getWifiChannel();

        /*Create a listview to display connected peers*/
        this.listView = (ListView) findViewById(R.id.listrpeers);
        this.mUserListAdapter = new BuddyListAdapter(this,
                android.R.layout.simple_list_item_1, mApplication.getPeerLST());
        this.listView.setAdapter(mUserListAdapter);
        mUserListAdapter.notifyDataSetChanged();

        /*Fetch our username and room name from previous activity*/
        Intent intent = getIntent();
        this.userName = intent.getStringExtra("USERNAME");
        this.roomName = intent.getStringExtra("ROOMNAME");

        final Buddy owner;
        if (intent.getStringExtra("STATUS").equals("Peer")) {
            self = new Buddy(userName, "in room",null, Buddy.TYPE_SELF | Buddy.TYPE_PEER);
            mApplication.addPeer(self);
            owner =intent.getParcelableExtra("OWNER");
            mode = Mode.roomClient;
            mUserListAdapter.notifyDataSetChanged();
        }

        else {
            self = new Buddy(userName, "in room",null, Buddy.TYPE_SELF | Buddy.TYPE_OWNER);
            mApplication.addPeer(self);
            mode = Mode.roomOwner;
            owner = self;
            mUserListAdapter.notifyDataSetChanged();
        }

        /* Get a wi-fi direct group manager instance*/
        mGroupManager = mApplication.startGroupManager(new GroupManager.ConnectionListener() {

            @Override
            public void onGroupConnected() { //SAY H
                //Send my info & Request all users info
                connected1 = true;
                if (mode != Mode.roomOwner) {
                    connectionDialog.setMessage("Creating TCP connection");
                }

            }

            @Override
            public void onTcpConnected() {
                connected2 = true;
                if (mode == Mode.roomClient && connectionDialog != null && connectionDialog.isShowing()) {
                    connectionDialog.setMessage("Connected!");
                    Handler p = new Handler();
                    p.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (connectionDialog != null && connectionDialog.isShowing())
                                connectionDialog.dismiss();
                        }
                    }, 1000);
                    String addr = "";
                    if(self.device != null){
                        addr =self.device.deviceAddress;
                    }
                    mGroupManager.rqstMsg(INFO_MSG + String.valueOf(self.type & (~Buddy.TYPE_SELF)) + self.Username + "/" + addr);
                }
                if(mGroupManager.is_groupOwner){
                    mService.stop();
                    mService.start();
                    announce = true;
                }
                else announce = false;
            }

            @Override
            public void onNewPost(String message) {
                String messageSubType = message.substring(0, 5);
                if (messageSubType.equals(USER_MSG)) {
                    String infomsg = message.substring(5);
                    int st = Integer.valueOf(infomsg.substring(0, 1)) & (~Buddy.TYPE_SELF);
                    String usr = infomsg.substring(1, infomsg.indexOf('/'));
                    String mcA = infomsg.substring((infomsg.indexOf('/') + 1));
                    WifiP2pDevice d = new WifiP2pDevice();
                    d.deviceAddress = mcA;
                    d.deviceName = usr;
                    Buddy aux = new Buddy(usr, "in room", d, st);

                    if (!aux.Username.equals(userName) && !mApplication.getPeerLST().contains(aux)) {
                        mApplication.addPeer(aux);
                        mApplication.sortPeers();
                        mUserListAdapter.notifyDataSetChanged();
                    }
                } else if (messageSubType.equals(START_MSG) && mode != Mode.roomOwner) {
                    String usrN = message.substring(5, message.indexOf('/'));
                    int id = Integer.valueOf(message.substring(message.indexOf('/') + 1, message.lastIndexOf('/')));
                    int players = Integer.valueOf(message.substring(message.lastIndexOf('/') + 1));
                    if (self.Username.equals(usrN)) {
                        self.peerID = id;
                        startGame(id, players);
                    }
                }

                Log.d("newMSG", "POST" + message);
            }

            @Override
            public void onNewRequest(String message) {

                String messageSubType = message.substring(0, 5);

                if (messageSubType.equals(INFO_MSG)) { //SOMEONE ASKED FOR INFO
                    String infomsg = message.substring(5);
                    int st = Integer.valueOf(infomsg.substring(0, 1)) & (~Buddy.TYPE_SELF);
                    String usr = infomsg.substring(1, infomsg.indexOf('/'));
                    String mcA = infomsg.substring((infomsg.indexOf('/') + 1));
                    WifiP2pDevice d = new WifiP2pDevice();
                    d.deviceAddress = mcA;
                    d.deviceName = usr;
                    Buddy aux = new Buddy(usr, "in room", d, st);
                    if (!mApplication.getPeerLST().contains(aux)) {
                        mApplication.getPeerLST().add(aux);
                        mUserListAdapter.notifyDataSetChanged();
                    }
                    mGroupManager.postMsg(USER_MSG + String.valueOf(self.type & (~Buddy.TYPE_SELF)) + userName + '/' + self.device.deviceAddress);
                }

                Log.d("newMSG", "RQST" + message);
            }

            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
            }

            @Override
            public void onPeerDisconnected(String extraMsg) {

            }

            @Override
            public void onDisconnected(String extraMsg) {

            }
        });

        /*Display room name on top left cornet*/
        TextView textView = (TextView) findViewById(R.id.roomname);
        textView.setText(roomName);


        /*Room owner is the one who decides when to start the game*/
        if (mode == RoomActivity.Mode.roomOwner) {

            /*When starting room, let the room owner be the group owner, although this might be changed in the future*/
            //mGroupManager.createGroup();
            Button startButton = new Button(this);
            startButton.setText("Start Game");
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mode == RoomActivity.Mode.roomOwner) {
                        int it = 1;
                        for (Buddy p : mApplication.getPeerLST()) {
                            if (!p.Username.equals(self.Username)) {
                                mGroupManager.postMsg(START_MSG + p.Username + '/' + String.valueOf(it++) + '/' + String.valueOf(mApplication.getPeerLST().size()));
                            }
                        }
                        startGame(0, mApplication.getPeerLST().size());
                    }

                }
            });
            ViewGroup v = (ViewGroup) findViewById(R.id.start_container);
            RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
            v.addView(startButton,params);
        }


        else {
            /* If we are in the room as peers, attempt to connect to room owner*/
            mGroupManager.connect(owner.device);

            /*Set a timeout to the connection attempt, after witch the application return to the main screen*/
            openConnectionDialog(owner.device.deviceAddress);
            final Handler timeoutHandler = new Handler();
            final Runnable runTimeout = new Runnable() {
                @Override
                public void run() {

                    if (!connected1) {
                        mGroupManager.cancelConnect();
                        connectionDialog.dismiss();
                        openAlertDialog("Cant Connect", "Timeout Connecting to " + owner.device.deviceAddress);

                    } else if (!connected2) {
                        connectionDialog.dismiss();
                        openAlertDialog("Cant Connect", "Timeout creating tcp connection with " + owner.device.deviceAddress);
                    }

                }
            };
            timeoutHandler.postDelayed(runTimeout, CONNECTION_TIME_OUT);
        }

        /*Set our broadcast receiver to alert this activity when relevant information is found*/
        mApplication.setReceiver(new BroadcastReceiver(mManager, mChannel, this, new BroadcastReceiver.Callback() {
            @Override
            public void onConnected() {
                connected1 = true;
                mManager.requestConnectionInfo(mChannel, mGroupManager);

                mManager.requestGroupInfo(mChannel, mGroupManager);
            }

            @Override
            public void onDisconnected() {
                Toast.makeText(RoomActivity.this, "Disconnected from Group.", Toast.LENGTH_SHORT).show();
                if(!leave)
                    mGroupManager.onDisconnected();
            }

            @Override
            public void onMyInfoAvailable(WifiP2pDevice device) {
                if (self != null) {
                    self.device = device;
                    mGroupManager.updateMyInfo(device);
                }

            }

            @Override
            public void onError() {

            }

            @Override
            public void wifiEnabled() {

            }

        }));

        /* Display ourselves on the wifi channel*/
        mService = new Service(userName, roomName, mode == Mode.roomOwner, mManager, mChannel, new Service.MessageHandler() {
            @Override
            public void onMessageAvailable(String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        if(mode == Mode.roomOwner){
            announce = true;
            mService.start();
        }

    }

    /**
     * Alert dialog handling method
     * @param title Dialog title
     * @param msg Display message
     */
    public void openAlertDialog(String title, String msg) {
        android.app.AlertDialog.Builder ap = new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        if (this != null && !this.isFinishing()) {
            ap.show();
        }

    }

    /**
     * Creates a connecting dialog
     * @param addr device's address attempting to connect to
     */
    public void openConnectionDialog(String addr) {
        connectionDialog = new ProgressDialog(this);
        connectionDialog.setTitle("Connection Attempt");
        connectionDialog.setMessage("Attempting to connect to " + addr);
        connectionDialog.setCancelable(true);
        connectionDialog.setCanceledOnTouchOutside(false);
        connectionDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mGroupManager.cancelConnect();
                connectionDialog.dismiss();
                onBackPressed();
            }
        });
        connectionDialog.show();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Starts the game activity
     * @param id our player Id
     * @param nPlayers total amount of player
     */
    public void startGame(int id, int nPlayers) {
        if(connected2 || mode == Mode.roomOwner)
            mService.stop();
        this.self.peerID = id;

        Intent intent = new Intent(getBaseContext(), GameLauncher.class);
        intent.putExtra("playerID", id);
        intent.putExtra("nPlayers", nPlayers);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(announce)
            mService.start();
        if(mApplication.getReceiver() == null){
            mApplication.startWifiFrameWork();
        }
        registerReceiver(mApplication.getReceiver(), mApplication.getIntentFilter());
    }

    @Override
    public void onPause() {
        unregisterReceiver(mApplication.getReceiver());

        mService.stop();
        super.onPause();
    }

    @Override
    public void onBackPressed() {

        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
         mService.stop();
    }

    @Override
    public void finish() {
        leave = true;
        mService.stop();
        mGroupManager.disconnect("0");
        mGroupManager.Destroy();
        mApplication.getPeerLST().clear();
        super.finish();
    }

}
