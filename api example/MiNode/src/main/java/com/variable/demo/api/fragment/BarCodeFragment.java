package com.variable.demo.api.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.variable.demo.api.NodeApplication;
import com.variable.demo.api.R;
import com.variable.framework.dispatcher.DefaultNotifier;
import com.variable.framework.node.BarCodeScanner;
import com.variable.framework.node.enums.NodeEnums;
import com.variable.framework.node.reading.SensorReading;

/**
 *
 */
public class BarCodeFragment extends Fragment {
    public static final String TAG = BarCodeFragment.class.getName();

    private BarCodeScanner scanner;
    private BarCodeScanner.BarCodeScannerListener listener;
    public BarCodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Obtain the Active Scanner.
        scanner = ((NodeApplication) getActivity().getApplication()).getActiveNode().findSensor(NodeEnums.ModuleType.BARCODE);
        assert scanner != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.barcode, null, false);

        //Create a Listener for recieving barcodes and update the edit text box.
        final EditText barCodeEditText = (EditText) root.findViewById(R.id.editBarCode);
        listener = new BarCodeScanner.BarCodeScannerListener() {
            @Override
            public void onBarCodeTransmitted(BarCodeScanner barCodeScanner, final SensorReading<String> barCodeReading) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        barCodeEditText.setText(barCodeReading.getValue());
                    }
                });

            }
        };
        return root;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        DefaultNotifier.instance().addBarCodeScannerListener(listener);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        DefaultNotifier.instance().addBarCodeScannerListener(listener);
    }



}
