package com.variable.demo.api.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.variable.demo.api.MessageConstants;
import com.variable.demo.api.NodeApplication;
import com.variable.demo.api.R;
import com.variable.framework.dispatcher.DefaultNotifier;
import com.variable.framework.node.NodeDevice;
import com.variable.framework.node.ThermocoupleSensor;
import com.variable.framework.node.enums.NodeEnums;
import com.variable.framework.node.reading.SensorReading;

import java.text.DecimalFormat;

/**
 * Created by coreymann on 9/16/13.
 */
public class ThermoCoupleFragment extends Fragment implements ThermocoupleSensor.ThermaCoupleListener {
    public static final String TAG = ThermoCoupleFragment.class.getName();
    private TextView temperatureText;

    private ThermocoupleSensor thermocoupleSensor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Start Karl addition
        NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if (node.findSensor(NodeEnums.ModuleType.THERMOCOUPLE) == null) {
            return inflater.inflate(R.layout.sensor_not_present, null, false);
        } else {
            // End Karl addition

            View root = inflater.inflate(R.layout.thermocouple, null, false);
            temperatureText = (TextView) root.findViewById(R.id.txtThermoCoupleReading);

            return root;
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        // Start Karl addition
        NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if (node.findSensor(NodeEnums.ModuleType.THERMOCOUPLE) == null) {
            return;
        }
        // End Karl addition

        //Unregister for thermoCouple event.
        DefaultNotifier.instance().removeThermaCoupleListener(this);
        thermocoupleSensor.stopSensor();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start Karl addition
        NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if (node.findSensor(NodeEnums.ModuleType.THERMOCOUPLE) == null) {
            return;
        }
        // End Karl addition

        //Register for ThermoCouple Event
        DefaultNotifier.instance().addThermaCoupleListener(this);
        //karl NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if(node != null)
        {
            thermocoupleSensor = node.findSensor(NodeEnums.ModuleType.THERMOCOUPLE);
            thermocoupleSensor.startSensor();
        }
    }

    @Override
    public void onThermoCoupleReading(ThermocoupleSensor sensor, SensorReading<Float> reading) {
        Message m = mHandler.obtainMessage(MessageConstants.MESSAGE_THERMA_TEMPERATURE);
        m.getData().putFloat(MessageConstants.FLOAT_VALUE_KEY, reading.getValue());
        m.sendToTarget();
    }

    private final Handler mHandler = new Handler(){
        private final DecimalFormat formatter = new DecimalFormat("0.00");
        @Override
        public void handleMessage(Message msg)
        {
            float value = msg.getData().getFloat(MessageConstants.FLOAT_VALUE_KEY);
            switch(msg.what){
                case MessageConstants.MESSAGE_THERMA_TEMPERATURE:
                    temperatureText.setText(formatter.format(value) + " °C");
                    break;

            }
        }
    };


}
