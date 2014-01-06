package com.variable.demo.api;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.variable.demo.api.fragment.BarCodeFragment;
import com.variable.demo.api.fragment.ChromaScanFragment;
import com.variable.demo.api.fragment.ClimaFragment;
import com.variable.demo.api.fragment.MainOptionsFragment;
import com.variable.demo.api.fragment.MotionFragment;
import com.variable.demo.api.fragment.OxaFragment;
import com.variable.demo.api.fragment.ThermaFragment;
import com.variable.demo.api.fragment.ThermoCoupleFragment;
import com.variable.framework.android.bluetooth.BluetoothService;
import com.variable.framework.android.bluetooth.DefaultBluetoothDevice;
import com.variable.framework.dispatcher.DefaultNotifier;
import com.variable.framework.node.AndroidNodeDevice;
import com.variable.framework.node.BaseSensor;
import com.variable.framework.node.ChromaCalibrationAndBatchingTask;
import com.variable.framework.node.DataLogSetting;
import com.variable.framework.node.NodeDevice;
import com.variable.framework.node.adapter.ConnectionAdapter;
import com.variable.framework.node.adapter.StatusAdapter;
import com.variable.framework.node.enums.NodeEnums;
import com.variable.framework.node.interfaces.ProgressUpdateListener;

public class MainActivity extends FragmentActivity implements View.OnClickListener{
    private static final String TAG = MainActivity.class.getName();
    private static BluetoothService mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Init Bluetooth Stuff
        if(ensureBluetoothIsOn()){
            mService = new BluetoothService(mHandler);
            NodeApplication.setServiceAPI(mService);
        }
    }


    @Override
    public void onResume(){
        super.onResume();

        ensureBluetoothIsOn();

        //Start Options Fragment
        Fragment frag = new MainOptionsFragment().setOnClickListener(this);
        animateToFragment(frag, MainOptionsFragment.TAG);
    }

    @Override
    public void onPause(){
        super.onResume();

        NodeDevice node = ((NodeApplication) getApplication()).getActiveNode();
        if(node != null){
            node.disconnect(); //Clean up after ourselves.
        }

        while(getSupportFragmentManager().popBackStackImmediate()) ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onConnected(final NodeDevice node)
    {
        //Set the NODE as connected and its connection status as connecting.
        node.isConnected(true);
        node.setConnectionStatus(BluetoothService.STATE_CONNECTING);

        //Issuing Initialization Commands
        node.requestVersion();
        node.initSensors();

        final ConnectionAdapter connectionAdapter = new ConnectionAdapter(){
            @Override
            public void onCommunicationInitCompleted(NodeDevice node) {

                BaseSensor sensor = node.findSensor(NodeEnums.ModuleType.CHROMA);
                if(sensor != null)
                {
                    ChromaCalibrationAndBatchingTask task = new ChromaCalibrationAndBatchingTask(MainActivity.this, sensor, node, new ProgressUpdateListener() {
                        @Override
                        public void onProgressUpdated(int i) {
                            if(i == 100){
                                mHandler.obtainMessage(DefaultBluetoothDevice.NODE_DEVICE_INIT_COMPLETE).sendToTarget();
                            }
                        }
                    });
                    new Thread(task).start();
                    return;
                }


                mHandler.obtainMessage(DefaultBluetoothDevice.NODE_DEVICE_INIT_COMPLETE).sendToTarget();
            }

            @Override
            public void nodeDeviceFailedToInit(NodeDevice device) {
                onCommunicationInitFailed(device);
            }
        };

        DefaultNotifier.instance().addConnectionListener(connectionAdapter);
    }

    /**
     * Signifies that NODE is ready for communication.
     * @param node
     */
    public void onCommunicationInitCompleted(NodeDevice node)
    {
        Toast.makeText(this, node.getName() + " is now ready for use.", Toast.LENGTH_SHORT).show();
    }

    public void onDisconnect(NodeDevice node)
    {
        Toast.makeText(this, node.getName() + " disconnected.", Toast.LENGTH_SHORT).show();
    }

    public void onCommunicationInitFailed(NodeDevice node)
    {
        Toast.makeText(this, node.getName() + " failed initialization.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Invokes a new intent to request to start the bluetooth, if not already on.
     */
    private boolean ensureBluetoothIsOn(){
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
            Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            btIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(btIntent, 200);
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 200){
            if(resultCode == RESULT_OK){
                mService = new BluetoothService(mHandler);
                NodeApplication.setServiceAPI(mService);
            }

        }
    }

    /**
     * Checks if a fragment with the specified tag exists already in the Fragment Manager. If present, then removes fragment.
     *
     * Animates out to the specified fragment.
     *
     *
     * @param frag
     * @param tag
     */
    public void animateToFragment(final Fragment frag, final String tag){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment existingFrag = getSupportFragmentManager().findFragmentByTag(tag);
        if(existingFrag != null){
            getSupportFragmentManager().beginTransaction().remove(existingFrag).commit();
        }

        ft.replace(R.id.center_fragment_container, frag, tag);
        ft.addToBackStack(null);
        ft.commit();
    }

    private boolean checkForSensor(NodeDevice node, NodeEnums.ModuleType type, boolean displayIfNotFound){
       BaseSensor sensor = node.findSensor(type);
        if(sensor == null && displayIfNotFound){
            Toast.makeText(MainActivity.this, type.toString() + " not found on " + node.getName(), Toast.LENGTH_SHORT).show();
        }

        return sensor != null;
    }
    private boolean isNodeConnected(NodeDevice node) { return node != null && node.isConnected(); }

    @Override
    public void onClick(View view) {
        NodeDevice node = ((NodeApplication) getApplication()).getActiveNode();
        if(isNodeConnected(node))
        {
            Toast.makeText(this, "No Connection Available", Toast.LENGTH_SHORT ).show();
            return;
        }
        switch(view.getId()){
            case R.id.btnMotion:
                animateToFragment(new MotionFragment(), MotionFragment.TAG);
                break;

            case R.id.btnClima:
               if(checkForSensor(node, NodeEnums.ModuleType.CLIMA, true))
                    animateToFragment(new ClimaFragment(), ClimaFragment.TAG);
               break;

            case R.id.btnTherma:
                if(checkForSensor(node, NodeEnums.ModuleType.THERMA, true))
                    animateToFragment(new ThermaFragment(), ThermaFragment.TAG);
                break;

            case R.id.btnOxa:
                if(checkForSensor(node, NodeEnums.ModuleType.OXA, true))
                    animateToFragment(new OxaFragment(), OxaFragment.TAG);
                break;

            case R.id.btnThermoCouple:
                if(checkForSensor(node, NodeEnums.ModuleType.THERMOCOUPLE, true))
                    animateToFragment(new ThermoCoupleFragment(), ThermoCoupleFragment.TAG);
                break;

            case R.id.btnBarCode:
                if(checkForSensor(node, NodeEnums.ModuleType.BARCODE, true))
                    animateToFragment(new BarCodeFragment(), BarCodeFragment.TAG);
                break;

            case R.id.btnChroma:
                if(checkForSensor(node, NodeEnums.ModuleType.CHROMA, true))
                    animateToFragment(new ChromaScanFragment(), ChromaScanFragment.TAG);
                break;

            //NODE must be polled to maintain an up to date array of sensors.
            case R.id.btnRefreshSensors:
                node.requestSensorUpdate();
                break;
        }
    }

    // The Handler that gets information back from the BluetoothService
    private final Handler mHandler = new Handler() {
        private ProgressDialog mProgressDialog;

        @Override
        public void handleMessage(Message msg) {
            NodeDevice node = ((NodeApplication) getApplication()).getActiveNode();

            switch (msg.what) {
                case BluetoothService.MESSAGE_DEVICE_ADDRESS:
                    //Retrieve the active
                    String address = msg.getData().getString(BluetoothService.DEVICE_ADDRESS);
                    node = AndroidNodeDevice.getManager().findFromAddress(address);
                    ((NodeApplication) getApplication()).setActiveNode(node);
                    break;

                case BluetoothService.MESSAGE_STATE_CHANGE:
                    String address2 = msg.getData().getString(BluetoothService.DEVICE_ADDRESS);
                    if(address2 != null){
                        node            = AndroidNodeDevice.getManager().findFromAddress(address2);
                    }
                    switch(msg.arg1){
                        case BluetoothService.STATE_CONNECTING:

                            if(mProgressDialog == null){
                                mProgressDialog = new ProgressDialog(MainActivity.this);
                            }else{
                                closeDialog(mProgressDialog);
                            }

                            buildDialog(node, mProgressDialog, "Connecting...");

                            break;

                        case  BluetoothService.STATE_CONNECTED:


                            if(mProgressDialog == null){
                                mProgressDialog = new ProgressDialog(MainActivity.this);
                            }else{
                                closeDialog(mProgressDialog);
                            }
                            buildDialog(node, mProgressDialog, "Initializing...");

                            //Log.d(TAG, "Connected Message Recieved for " + ((BaseApplication) getApplication()).getActiveNode().getName());
                            onConnected(node);

                            break;
                        case BluetoothService.STATE_DISCONNECTED:

                            closeDialog(mProgressDialog);

                            //Log.d(TAG, node.getName() + " is now disconnected");
                            onDisconnect(node);

                            break;

                    }

                    break;

                //Handle Node Successfull initialized.
                case DefaultBluetoothDevice.NODE_DEVICE_INIT_COMPLETE:
                    Log.d(TAG, "NodeDevice Init Completed in Handler");
                    closeDialog(mProgressDialog);
                    onCommunicationInitCompleted(node);
                    break;
            }
        }


        private final void closeDialog(ProgressDialog progressDialog){
            mProgressDialog.dismiss();
            mProgressDialog = new ProgressDialog(MainActivity.this);
        }

        private final void buildDialog(final NodeDevice node, ProgressDialog progressDialog, String message){
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    node.disconnect();
                }
            });

            mProgressDialog.setTitle("Bluetooth Connection");
            mProgressDialog.setMessage(message);

            if(!mProgressDialog.isShowing()) { mProgressDialog.show(); }
        }


    };


}
