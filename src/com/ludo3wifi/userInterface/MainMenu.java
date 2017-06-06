package com.ludo3wifi.userInterface;


import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ludo3wifi.LudoApplication;
import com.ludo3wifi.R;
import com.ludo3wifi.net.BroadcastReceiver;

/**
 * Main activity class, called when the application starts, and
 * it is where the user decides if he wants to creata a room or join a peer's
 */
public class MainMenu extends Activity {
    public LudoApplication mAPP;
    private boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        if (savedInstanceState == null) {
            doHosting(null);
        }
        startFramework();

    }

    /**
     * Starts the wifi framework and registers the broadcast receiver.
     */
    public void startFramework() {
        if (exit) {
            finish();
        }
        mAPP = (LudoApplication) getApplication();
        mAPP.startWifiFrameWork();
        mAPP.setReceiver(new BroadcastReceiver(mAPP.getP2pManager(), mAPP.getWifiChannel(), this, new BroadcastReceiver.Callback() {
            @Override
            public void onConnected() {
            }

            @Override
            public void onDisconnected() {
            }

            @Override
            public void onMyInfoAvailable(WifiP2pDevice device) {
            }

            @Override
            public void onError() {
                mAPP.setWifiState(false);
            }

            @Override
            public void wifiEnabled() {
                mAPP.setWifiState(true);
            }
        }));
        if (!mAPP.getWifiState())
            checkWifiState();
    }

    /**
     * Creates a progress dialog until information regarding the wi-fi direct
     * state is gathered.
     */
    private void checkWifiState() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Checking device's Wi-fi state");
        pd.setMessage("Please wait...");
        pd.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mAPP.getWifiState()) {
                    displayWifiError();
                }
                pd.dismiss();
            }
        }, 3000);

    }

    /**
     * Displays an alert dialog if wi-fi direct is not enabled.
     */
    private void displayWifiError() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Wi-fi Direct Error")
                .setMessage("Wi-fi direct is not enabled.\n Please enable wi-fi direct before continuing")
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkWifiState();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }

    /**
     * On click handler, starts the {@link InputHostInfoFragment} fragment
     * @param v clicked view, unused.
     */
    public void doHosting(View v) {
        Button b = (Button) findViewById(R.id.doHosting);
        b.setBackground(getResources().getDrawable(R.drawable.bh2));

        Button s = (Button) findViewById(R.id.searchGame);
        s.setBackground(getResources().getDrawable(R.drawable.bs1));


        FragmentManager fm = getFragmentManager();
        InputHostInfoFragment frag = new InputHostInfoFragment();
        findViewById(R.id.main_menu).setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    finish();
                }
                return MainMenu.this.onKeyDown(keyCode, event);
            }
        });
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.inputInfoContainer, frag);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    /**
     * On click handler, starts the {@link FindGameFragment} fragment
     * @param v clicked view, unused
     */
    public void findGame(View v) {

        Button b = (Button) findViewById(R.id.searchGame);
        b.setBackground(getResources().getDrawable(R.drawable.bs2));

        Button s = (Button) findViewById(R.id.doHosting);
        s.setBackground(getResources().getDrawable(R.drawable.bh1));

        FragmentManager fm = getFragmentManager();
        FindGameFragment frag = new FindGameFragment();
        findViewById(R.id.main_menu).setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    finish();
                }
                return MainMenu.this.onKeyDown(keyCode, event);
            }
        });
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.inputInfoContainer, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Creates a new room with input from the {@link InputHostInfoFragment},
     * starting the {@link RoomActivity} activity
     * @param v clicked view, unused
     */
    public void StartRoom(View v) {
        EditText UserNameInput = (EditText) this.findViewById(R.id.inptUsername);
        EditText roomName = (EditText) this.findViewById(R.id.roomName);
        String username = UserNameInput.getText().toString();
        if (username.matches("")) {
            Toast.makeText(this, "You did not enter a username!", Toast.LENGTH_SHORT).show();
            return;
        }else{
            String roomname = roomName.getText().toString();
            if(roomname.matches("")){
                roomname = username + " Room";
            }
            Intent intent = new Intent(this, RoomActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("STATUS", "OWNER");
            intent.putExtra("ROOMNAME", roomname);
            startActivity(intent);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mAPP.getPeerLST().clear();
        registerReceiver(mAPP.getReceiver(), mAPP.getIntentFilter());
    }

    @Override
    public void onPause() {
        unregisterReceiver(mAPP.getReceiver());
        super.onPause();
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
        startActivity(intent);
        finish();
        System.exit(0);
    }

}
