package com.ludo3wifi.userInterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ludo3wifi.LudoApplication;
import com.ludo3wifi.R;
import com.ludo3wifi.userInterface.peers.Buddy;
import com.ludo3wifi.userInterface.peers.BuddyListAdapter;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by eduardogomes on 16/04/17.
 * Fragment to find and display open rooms on the wi-fi direct framework
 */

public class FindGameFragment extends Fragment {
    public static final int DISCOVERY_PERIOD = 10000;
    public static final String SERVICE_DESCP = "GameService";
    public Buddy owner;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    Runnable mDiscoverThread;
    Handler discoverHandler;
    WifiP2pDnsSdServiceRequest CserviceRequest;
    boolean discover = true;
    View v;
    private BuddyListAdapter RoomsListAdapter;
    private ArrayList<Buddy> Rooms;
    private ListView listView;
    private Activity mActivity;
    private ProgressBar pg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.find_games_fragment, container, false);
        this.listView = (ListView) v.findViewById(R.id.rooms_list);
        return v;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = getActivity();
        LudoApplication mAPP = (LudoApplication) mActivity.getApplication();
        mManager = mAPP.getP2pManager();
        mChannel = mAPP.getWifiChannel();

        /*start required view items*/
        RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.pb);
        pg = new ProgressBar(mActivity, null, android.R.attr.progressBarStyleLarge);
        pg.setIndeterminate(true);
        pg.setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(pg, params);

        /*Start peers list*/
        Rooms = new ArrayList<Buddy>();
        this.RoomsListAdapter = new BuddyListAdapter(getActivity(),
                android.R.layout.simple_list_item_1, Rooms);
        this.listView.setAdapter(this.RoomsListAdapter);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                owner = (Buddy) RoomsListAdapter.getItem(position);
                final EditText input = new EditText(mActivity);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                new AlertDialog.Builder(mActivity)
                        .setTitle("Connect to " + owner.Username + "?")
                        .setMessage("Please insert your display username")
                        .setIcon(android.R.drawable.ic_input_add)
                        .setView(input)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                String user = input.getText().toString();
                                if(user.matches("")){
                                    Toast.makeText(mActivity,"Username can not be empty!",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                /*Start room activity*/
                                Intent intent = new Intent(mActivity, RoomActivity.class);
                                intent.putExtra("USERNAME", user);
                                intent.putExtra("STATUS", "Peer");
                                intent.putExtra("ROOMNAME", owner.Status);
                                intent.putExtra("OWNER", owner);
                                discover = false;
                                if (mManager != null)
                                    mManager.removeServiceRequest(mChannel, CserviceRequest, null);
                                discoverHandler.removeCallbacks(mDiscoverThread);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();

            }
        });

    }

    /**
     * Start a loop requesting a new service discovery every {@link FindGameFragment#DISCOVERY_PERIOD}
     */
    public void LoopFindGames() {
        discoverHandler = new Handler();
        mDiscoverThread = new Runnable() {
            @Override
            public void run() {
                if (!discover)
                    return;
                FindGames(null);
                displaysearchingWindow();
                discoverHandler.postDelayed(mDiscoverThread, DISCOVERY_PERIOD);
            }
        };
        FindGames(null);
        mDiscoverThread.run();
    }

    /**
     * Attempt to find game services in the network
     * @param v Clicked view, unused
     */
    public void FindGames(View v) {
        /*Stop previous service request*/
        if (CserviceRequest == null)
            CserviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mManager.removeServiceRequest(mChannel, CserviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                        mManager.setDnsSdResponseListeners(mChannel,

                                new WifiP2pManager.DnsSdServiceResponseListener() {
                                    @Override
                                    public void onDnsSdServiceAvailable(String InstanceName, String registrationType,
                                                                        WifiP2pDevice device) {
                                        Log.d("TAG", "FOUNDz");
                                        if (!InstanceName.equalsIgnoreCase(SERVICE_DESCP))
                                            return;

                                    }
                                },
                                new WifiP2pManager.DnsSdTxtRecordListener() {
                                    @Override
                                    public void onDnsSdTxtRecordAvailable(String fullDomain, Map record, WifiP2pDevice device) {
                                        Log.d("TAG", "FOUND2");
                                        Buddy new_buddy = new Buddy((String) record.get("username"), (String) record.get("roomname"), device, Integer.valueOf((String) record.get("type")));
                                        if (Rooms.contains(new_buddy)) {


                                        } else {
                                            Rooms.add(new_buddy);
                                            RoomsListAdapter.notifyDataSetChanged();
                                        }

                                    }
                                }
                        );

                        addServiceRequest(CserviceRequest);
                    }

                    @Override
                    public void onFailure(int reasoncode) {

                    }
                });


    }

    /*Add new service request*/
    public void addServiceRequest(final WifiP2pServiceRequest request) {
        mManager.addServiceRequest(mChannel, request,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {

                                Log.d("TAG", "DiscoverServices started");
                            }

                            @Override
                            public void onFailure(int code) {
                                Log.d("TAG", String.format("Error discovering services reason %d", code));
                            }
                        });
                    }

                    @Override
                    public void onFailure(int reasoncode) {

                    }
                });
    }

    private void displaysearchingWindow() {
        pg.setVisibility(View.VISIBLE);
        final Handler n = new Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                pg.setVisibility(View.INVISIBLE);
                n.postDelayed(this, 3000);
            }
        };
        n.postDelayed(r, 3000);
    }

    @Override
    public void onPause() {
        discoverHandler.removeCallbacks(mDiscoverThread);
        discover = false;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        discover = true;
        LoopFindGames();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (mManager != null) mManager.removeServiceRequest(mChannel, CserviceRequest, null);
        discoverHandler.removeCallbacks(mDiscoverThread);
        discover = false;
        super.onDestroyView();
        super.onDestroy();
    }


}
