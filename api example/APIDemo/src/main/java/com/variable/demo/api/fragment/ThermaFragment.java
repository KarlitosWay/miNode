package com.variable.demo.api.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.variable.demo.api.MessageConstants;
import com.variable.demo.api.NodeApplication;
import com.variable.demo.api.R;
import com.variable.framework.dispatcher.DefaultNotifier;
import com.variable.framework.node.NodeDevice;
import com.variable.framework.node.ThermaSensor;
import com.variable.framework.node.enums.NodeEnums;
import com.variable.framework.node.reading.SensorReading;

import java.text.DecimalFormat;

/**
 * Created by coreymann on 8/13/13.
 */
public class ThermaFragment extends Fragment implements ThermaSensor.ThermaListener {
    public static final String TAG = ThermaFragment.class.getName();

    //The Handler of this class primarily demonstrates how to use a NodeDevice isntance with a physical therma attached.

    private TextView temperatureText;
    private ToggleButton irLedsSwitch;
    private int temperatureUnit = 0;

    private ThermaSensor therma;
    public static final String PREF_EMISSIVITY_NUMBER = "com.variable.demo.api.setting.EMISSIVITY_NUMBER";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Start Karl addition
        NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if (node.findSensor(NodeEnums.ModuleType.THERMA) == null) {
            return inflater.inflate(R.layout.sensor_not_present, null, false);
        } else {
            // End Karl addition

            View root = inflater.inflate(R.layout.therma, null, false);
            temperatureText = (TextView) root.findViewById(R.id.txtTherma);
            temperatureText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (++temperatureUnit == 2) {
                        temperatureUnit = 0;
                    }
                }
            });

            irLedsSwitch = (ToggleButton) root.findViewById(R.id.irToggle);
            irLedsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mHandler.obtainMessage(MessageConstants.MESSAGE_CHANGE_IR_THERMA).sendToTarget();
                }
            });

            root.findViewById(R.id.btnEmissivityChange).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buildAndShowEmissivityDialog();
                }
            });
            return root;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Start Karl addition
        NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if (node.findSensor(NodeEnums.ModuleType.THERMA) == null) {
            return;
        }
        // End Karl addition

        //Unregister for therma event.
        DefaultNotifier.instance().removeThermaListener(this);

        therma.stopSensor();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start Karl addition
        NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if (node.findSensor(NodeEnums.ModuleType.THERMA) == null) {
            return;
        }
        // End Karl addition

        //Register for Therma Event
        DefaultNotifier.instance().addThermaListener(this);

        //karl NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if(node != null)
        {
            therma = node.findSensor(NodeEnums.ModuleType.THERMA);
            therma.startSensor();
        }
    }

    /**
     * Builds a Dialog to ask the user to change the emissivity setting.
     */
    public void buildAndShowEmissivityDialog(){
        final EditText text = new EditText(getActivity());
        text.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        text.setHint("Enter a number for the emissivity of the surface.");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter an Emissivity Number");
        builder.setView(text);
        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String rawText = text.getText().toString();
                try
                {
                    Float emissivity_value = Float.parseFloat(rawText);
                    PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putFloat(PREF_EMISSIVITY_NUMBER, emissivity_value)
                        .commit();

                    mHandler.obtainMessage(MessageConstants.MESSAGE_EMISSIVITY_NUMBER_UPDATE).sendToTarget();

                }catch(NumberFormatException e){ }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    @Override
    public void onTemperatureReading(ThermaSensor sensor, SensorReading<Float> reading) {
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
                  String unitSymbol = " ºC";
                  if(temperatureUnit  == 1){
                      value =  value * 1.8000f + 32;
                      unitSymbol = " ºF";
                  }
                  temperatureText.setText(formatter.format(value) +  unitSymbol);
                  break;

              case MessageConstants.MESSAGE_CHANGE_IR_THERMA:
                  //This Block show how to adjust the ir lights on THERMA without changing its streaming state.
                  //Sets the New IR State.
                  therma.setStreamMode(therma.isStreaming(), !therma.isLEDOn());
                  break;

              case MessageConstants.MESSAGE_EMISSIVITY_NUMBER_UPDATE:
                  Float emiss = PreferenceManager.getDefaultSharedPreferences(getActivity()).getFloat(PREF_EMISSIVITY_NUMBER, 1);

                  //Updates the Stream by passing the emissivity to node with a stream lifetime of infinity.
                  therma.setStreamMode(therma.isStreaming(),true, emiss, 0,0, true);
                  break;
        }
      }
    };
}
