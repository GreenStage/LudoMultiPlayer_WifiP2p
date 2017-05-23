package rmsf.eduardodiogo.ludo3wifi.userInterface;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;

import rmsf.eduardodiogo.ludo3wifi.R;
import rmsf.eduardodiogo.ludo3wifi.wifiLayer.GroupManager;


/**
 * Created by eduardogomes on 16/04/17.
 */

public class FindGameFragment extends Fragment {
    public static final String SERVICE_DESCP = "GameService";
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    GroupManager mGroupManager;
    private BuddyListAdapter RoomsListAdapter;
    private ArrayList<Buddy> Rooms;
    private ListView listView;
    private Activity mActivity ;
    Runnable mDiscoverThread;
    Handler discoverHandler;
    public Buddy owner;
    WifiP2pDnsSdServiceRequest CserviceRequest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.find_games_fragment, container, false);
        this.listView = (ListView) v.findViewById(R.id.rooms_list);
        return v;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        mActivity = getActivity();

        Rooms = new ArrayList<Buddy>();


        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                owner = (Buddy) RoomsListAdapter.getItem(position);
                final EditText input = new EditText(mActivity);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                new AlertDialog.Builder(mActivity)
                        .setTitle("Confirm action")
                        .setMessage("Please insert your Display username")
                        .setIcon(android.R.drawable.ic_input_add)
                        .setView(input)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String user = input.getText().toString();
                                Intent intent = new Intent( mActivity, RoomActivity.class);
                                intent.putExtra("USERNAME", user);
                                intent.putExtra("STATUS","Peer");
                                intent.putExtra("ROOMNAME",owner.Status);
                                intent.putExtra("OWNER", owner);
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
        this.RoomsListAdapter = new BuddyListAdapter(getActivity(),
                android.R.layout.simple_list_item_1,Rooms);
        this.listView.setAdapter(this.RoomsListAdapter);

        mManager = (WifiP2pManager) getActivity().getSystemService(Context.WIFI_P2P_SERVICE);
        try {
            mChannel = mManager.initialize(mActivity, mActivity.getMainLooper(), null);
        }catch( Exception e){
            Log.e("MYAPP", "exception: " + e.getMessage());
            Log.e("MYAPP", "exception: " + e.toString());
        }

        LoopFindGames();
    }


    public void LoopFindGames(){
        discoverHandler = new Handler();
        mDiscoverThread = new Runnable() {
            @Override
            public void run() {
                FindGames(null);
                discoverHandler.postDelayed(mDiscoverThread,15000);
            }
        };
        FindGames(null);
        mDiscoverThread.run();
    }


    public void FindGames(View v){

        if(CserviceRequest == null)
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
                                        Log.d("TAG","FOUNDz");
                                        if (!InstanceName.equalsIgnoreCase(SERVICE_DESCP))
                                            return;

                                    }
                                },
                                new WifiP2pManager.DnsSdTxtRecordListener() {
                                    @Override
                                    public void onDnsSdTxtRecordAvailable(String fullDomain, Map record, WifiP2pDevice device) {
                                        Log.d("TAG","FOUND2");
                                        Buddy new_buddy = new Buddy(device,(String)record.get("username"),(String)record.get("roomname"),Integer.valueOf( (String)record.get("type")));
                                        if(Rooms.contains(new_buddy) ) {


                                        }
                                        else{
                                            Rooms.add(new_buddy);
                                            RoomsListAdapter.notifyDataSetChanged();
                                        }

                                    }
                                }
                        );
                        CserviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
                        mManager.addServiceRequest(mChannel, CserviceRequest,
                                new WifiP2pManager.ActionListener() {

                                    @Override
                                    public void onSuccess() {
                                        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

                                            @Override
                                            public void onSuccess() {
                                                // Success!
                                                Log.d("TAG","DiscoverServices started");
                                            }

                                            @Override
                                            public void onFailure(int code) {
                                                Log.d("TAG",String.format("Error reason %d", code));
                                            }
                                        });
                                    }
                                    @Override
                                    public void onFailure(int reasoncode){

                                    }
                                });
                    }

                    @Override
                    public void onFailure(int reasoncode){

                    }
                });


    }

    @Override
    public void onDestroyView() {
        Log.d("TAG","TESTE");
        if( mGroupManager != null) mGroupManager.Destroy();
        if( mManager != null) mManager.removeServiceRequest(mChannel, CserviceRequest,null);
        discoverHandler.removeCallbacks(mDiscoverThread);
        super.onDestroyView();
        super.onDestroy();
    }


}
