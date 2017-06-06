package com.ludo3wifi.userInterface.peers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ludo3wifi.R;

import java.util.List;

/**
 * Created by eduardogomes on 12/04/17.
 */

/**
 * Class in charge of displaying each list item on screen
 */
public class BuddyListAdapter extends ArrayAdapter {

    public BuddyListAdapter(Context context, int textViewResourceId, List<Buddy> users) {
        super(context, textViewResourceId, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.buddy_element, null);

        }

        Buddy usr = (Buddy) getItem(position);

        if (usr != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.buddy_name);
            TextView tt2 = (TextView) v.findViewById(R.id.buddy_status);
            ImageView icv = (ImageView) v.findViewById(R.id.buddy_icon);
            ImageView icv2 = (ImageView) v.findViewById(R.id.buddy_icon2);

            if (tt1 != null) {
                tt1.setText(usr.Username);
            }

            if (tt2 != null) {
                tt2.setText(usr.Status);
            }
            if (icv != null) {
                if ((usr.type & Buddy.TYPE_SELF) == Buddy.TYPE_SELF) {
                    icv.setImageResource(R.drawable.you);
                    icv.setVisibility(View.VISIBLE);
                } else icv.setVisibility(View.INVISIBLE);
            }
            if (icv2 != null) {
                if ((usr.type & Buddy.TYPE_OWNER) == Buddy.TYPE_OWNER) {
                    icv2.setImageResource(R.drawable.owner);
                    icv2.setVisibility(View.VISIBLE);
                } else icv2.setVisibility(View.INVISIBLE);

            }
        }

        return v;
    }
}
