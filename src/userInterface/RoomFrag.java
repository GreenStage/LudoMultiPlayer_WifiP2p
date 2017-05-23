package rmsf.eduardodiogo.ludo3wifi.userInterface;

import android.support.v4.app.Fragment;
import android.widget.ListView;

/**
 * Created by eduardogomes on 22/05/17.
 */

public abstract class RoomFrag extends Fragment {
    RoomActivity mRoomActivity;
    BuddyListAdapter mUserListAdapter;
    ListView listView;

    public void updatePeers(){
        mUserListAdapter.notifyDataSetChanged();
    }

    public abstract void addMember();

    public abstract void rcvMsg(String msg);

}
