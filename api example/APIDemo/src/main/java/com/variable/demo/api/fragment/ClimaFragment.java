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
import com.variable.framework.node.ClimaSensor;
import com.variable.framework.node.NodeDevice;
import com.variable.framework.node.enums.NodeEnums;
import com.variable.framework.node.reading.SensorReading;

import java.text.DecimalFormat;

/**
 * Created by coreymann on 8/13/13.
 */
public class ClimaFragment extends Fragment  implements
                                                        ClimaSensor.ClimaHumidityListener,
                                                        ClimaSensor.ClimaLightListener,
                                                        ClimaSensor.ClimaPressureListener,
                                                        ClimaSensor.ClimaTemperatureListener{
    public static final String TAG = ClimaFragment.class.getName();

    private TextView climaLightText;
    private TextView climaPressureText;
    private TextView climaTempText;
    private TextView climaHumidText;
    private ClimaSensor clima;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       super.onCreateView(inflater, container, savedInstanceState);

        // Start Karl addition
        NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if (node.findSensor(NodeEnums.ModuleType.CLIMA) == null) {
            return inflater.inflate(R.layout.sensor_not_present, null, false);
        } else {
            // End Karl addition

            View root = inflater.inflate(R.layout.clima, null, false);

            climaHumidText = (TextView) root.findViewById(R.id.txtClimaHumidity);
            climaLightText = (TextView) root.findViewById(R.id.txtClimaLight);
            climaPressureText = (TextView) root.findViewById(R.id.txtClimaPressure);
            climaTempText = (TextView) root.findViewById(R.id.txtClimaTemperature);

            return root;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start Karl addition
        NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if (node.findSensor(NodeEnums.ModuleType.CLIMA) == null) {
            return;
        }
        // End Karl addition

        DefaultNotifier.instance().addClimaHumidityListener(this);
        DefaultNotifier.instance().addClimaLightListener(this);
        DefaultNotifier.instance().addClimaTemperatureListener(this);
        DefaultNotifier.instance().addClimaPressureListener(this);

        //karl NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if(node != null){

            //Located the first available clima sensor.
            clima = node.findSensor(NodeEnums.ModuleType.CLIMA);

            //Turn on all streaming
            clima.setStreamMode(true, true, true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Start Karl addition
        NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if (node.findSensor(NodeEnums.ModuleType.CLIMA) == null) {
            return;
        }
        // End Karl addition

        //Unregister for clima events.
        DefaultNotifier.instance().removeClimaHumidityListener(this);
        DefaultNotifier.instance().removeClimaLightListener(this);
        DefaultNotifier.instance().removeClimaTemperatureListener(this);
        DefaultNotifier.instance().removeClimaPressureListener(this);

        //Turn off clima sensor
        clima.setStreamMode(false, false, false);
    }

    @Override
    public void onClimaHumidityUpdate(ClimaSensor clima, SensorReading<Float> humidityLevel) {
        Message m = mHandler.obtainMessage(MessageConstants.MESSAGE_CLIMA_HUMIDITY);
        m.getData().putFloat(MessageConstants.FLOAT_VALUE_KEY, humidityLevel.getValue());
        m.sendToTarget();
    }

    @Override
    public void onClimaLightUpdate(ClimaSensor clima, SensorReading<Float> lightLevel) {
        Message m = mHandler.obtainMessage(MessageConstants.MESSAGE_CLIMA_LIGHT);
        m.getData().putFloat(MessageConstants.FLOAT_VALUE_KEY, lightLevel.getValue());
        m.sendToTarget();
    }

    @Override
    public void onClimaPressureUpdate(ClimaSensor clima, SensorReading<Integer> kPa) {
        Message m = mHandler.obtainMessage(MessageConstants.MESSAGE_CLIMA_PRESSURE);
        m.getData().putFloat(MessageConstants.FLOAT_VALUE_KEY, kPa.getValue());
        m.sendToTarget();
    }

    @Override
    public void onClimaTemperatureUpdate(ClimaSensor clima, SensorReading<Float> temperature) {
        Message m = mHandler.obtainMessage(MessageConstants.MESSAGE_CLIMA_TEMPERATURE);
        m.getData().putFloat(MessageConstants.FLOAT_VALUE_KEY, temperature.getValue());
        m.sendToTarget();
    }

    private final Handler mHandler = new Handler(){
        private final DecimalFormat formatter = new DecimalFormat("0.00");

        @Override
        public void handleMessage(Message msg){

            float value = msg.getData().getFloat(MessageConstants.FLOAT_VALUE_KEY);
            switch(msg.what){
                case MessageConstants.MESSAGE_CLIMA_HUMIDITY:
                    climaHumidText.setText(formatter.format(value) + " %RH");
                    break;
                 case MessageConstants.MESSAGE_CLIMA_LIGHT:
                    climaLightText.setText(formatter.format(value) + " LUX");
                    break;

                case MessageConstants.MESSAGE_CLIMA_PRESSURE:
                    climaPressureText.setText(formatter.format(value / 1000) + " kPA");
                    break;

                case MessageConstants.MESSAGE_CLIMA_TEMPERATURE:
                    climaTempText.setText(formatter.format(value) + " C");
                    break;

            }

        }
    };
}
