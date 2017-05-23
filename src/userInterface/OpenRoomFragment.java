package rmsf.eduardodiogo.ludo3wifi.userInterface;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import rmsf.eduardodiogo.ludo3wifi.R;

/**
 * Created by eduardogomes on 22/05/17.
 */

public class OpenRoomFragment extends RoomFrag {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.open_rooms, container, false);
        this.listView = (ListView) v.findViewById(R.id.listrpeers);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mRoomActivity = (RoomActivity) getActivity();


        this.mUserListAdapter = new BuddyListAdapter(mRoomActivity,
                android.R.layout.simple_list_item_1,mRoomActivity.peers);
        this.listView.setAdapter(mUserListAdapter);
        updatePeers();
        if(mRoomActivity.mode == RoomActivity.Mode.roomOwner){
            Button startButton = new Button(mRoomActivity);
            startButton.setText("Start Game");
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onStartGame(null);
                }
            });
            ViewGroup v = (ViewGroup) mRoomActivity.findViewById(R.id.start_container);
            v.addView(startButton);
        }


    }

    public void onStartGame(View v){
        mRoomActivity.startGame();
    }

    public void rcvMsg(String msg){

    }

    public void addMember(){}
}
