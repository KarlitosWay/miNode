package com.variable.demo.api.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.variable.demo.api.R;


/**
 * Created by karl on 10/06/2014.
 */
public class KarlFragment extends Fragment {


    public static final String TAG = KarlFragment.class.getName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.karl_all, container, false);
        return view;
    }
}

