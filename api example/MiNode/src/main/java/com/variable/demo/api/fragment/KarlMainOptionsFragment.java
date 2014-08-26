package com.variable.demo.api.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.variable.demo.api.NodeApplication;
import com.variable.demo.api.R;
import com.variable.framework.android.bluetooth.BluetoothService;
import com.variable.framework.android.bluetooth.DefaultBluetoothDevice;
import com.variable.framework.node.AndroidNodeDevice;
import com.variable.framework.node.NodeDevice;

import java.util.Set;

/**
 * Created by coreymann on 8/13/13.
 */
public class KarlMainOptionsFragment extends Fragment {
    public static final String TAG = KarlMainOptionsFragment.class.getName();

    private View.OnClickListener onClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.karl_main_options, null, false);

        ButtonClickHandler clickHandler = new ButtonClickHandler();
        root.findViewById(R.id.btnMotion).setOnClickListener(clickHandler);
        root.findViewById(R.id.btnPairedNodes).setOnClickListener(clickHandler);
        root.findViewById(R.id.btnGPS).setOnClickListener(clickHandler);
        root.findViewById(R.id.btnClima).setOnClickListener(clickHandler);
        root.findViewById(R.id.btnTherma).setOnClickListener(clickHandler);
        root.findViewById(R.id.btnOxa).setOnClickListener(clickHandler);
        root.findViewById(R.id.btnThermoCouple).setOnClickListener(clickHandler);
        root.findViewById(R.id.btnBarCode).setOnClickListener(clickHandler);
        root.findViewById(R.id.btnRefreshSensors).setOnClickListener(clickHandler);
        root.findViewById(R.id.btnChroma).setOnClickListener(clickHandler);
        root.findViewById(R.id.btnIOSensor).setOnClickListener(clickHandler);
        root.findViewById(R.id.btnPulseLed).setOnClickListener(clickHandler);
        root.findViewById(R.id.btnKarl).setOnClickListener(clickHandler);

        return root;
    }


    public KarlMainOptionsFragment setOnClickListener(View.OnClickListener listener) { onClickListener = listener; return this; }

    /**
     * Initiates a connection with the selected device.
     * @param device
     */
    private void onDeviceSelected(BluetoothDevice device)
    {
        Log.d("", "Selected Device Name" + device.getName());
        BluetoothService mService = NodeApplication.getService();
        NodeDevice node = NodeApplication.getActiveNode();

        if(mService != null){
            //One way to connect to a device
            //mService.connect(device.getAddress());

            //Second way, using the NodeDevice implementation
            NodeDevice selectedNODE = AndroidNodeDevice.getOrCreateNodeFromBluetoothDevice(device, new DefaultBluetoothDevice(mService));

            //Ensure One Connection At a Time...
            if(node != null && !selectedNODE.equals(node) && node.isConnected()){   node.disconnect(); }

            //Store the Active NODE in the application space for other fragments to use
            NodeApplication.setActiveNode(selectedNODE);

            //initiate connection
            selectedNODE.connect();
        }
    }


    public class ButtonClickHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.btnPairedNodes:
                    showPairedNodesDialog(view.getContext());
                    break;
                default:
                    if(onClickListener != null){
                        onClickListener.onClick(view);
                    }
                    break;
            }
        }
    }


    /**
     * Shows a Dialog for any bonded device.
     *
     * Additionally, when item is select, onDeviceSelected is invoked.
     * @param c
     */
    private void showPairedNodesDialog(Context c) {
        final Set<BluetoothDevice> mBondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        final String[] bluetoothNames =  new String[mBondedDevices.size()];
        int i=0;
        for(BluetoothDevice device : mBondedDevices) { bluetoothNames[i++] = device.getName(); }


        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Select a NODE")
                .setItems(bluetoothNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {

                        int index=0;
                        for(BluetoothDevice device : mBondedDevices){
                            if(index++ == position){
                                onDeviceSelected(device);
                            }
                        }
                    }
                });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();


    }

}
