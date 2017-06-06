package com.ludo3wifi.userInterface;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ludo3wifi.R;

/**
 * Input Host info fragment
 */
public class InputHostInfoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.input_host_fragment, container, false);
    }
}
