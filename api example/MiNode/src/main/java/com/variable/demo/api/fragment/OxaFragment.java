package com.variable.demo.api.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.variable.demo.api.MessageConstants;
import com.variable.demo.api.NodeApplication;
import com.variable.demo.api.R;
import com.variable.framework.dispatcher.DefaultNotifier;
import com.variable.framework.node.NodeDevice;
import com.variable.framework.node.OxaSensor;
import com.variable.framework.node.enums.NodeEnums;
import com.variable.framework.node.reading.SensorReading;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by coreymann on 8/13/13.
 */
public class OxaFragment extends Fragment implements OxaSensor.OxaListener {
    public static final String TAG = OxaFragment.class.getName();

    private TextView oxaText;
    private TextView oxaBaseLineA;
    private OxaSensor oxa;

    // karl addition
    private OutputStreamWriter oxaReadingStreamWriter = null;
    private OutputStreamWriter oxaBaseLineAStreamWriter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.oxa, null, false);
        oxaText = (TextView) root.findViewById(R.id.txtOxa);
        oxaBaseLineA = (TextView) root.findViewById(R.id.txtBaseLineA);

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();

        DefaultNotifier.instance().removeOxaListener(this);

        //Turn off oxa
        oxa.stopSensor();

        // karl addition
        try {
            if (oxaReadingStreamWriter != null) {
                oxaReadingStreamWriter.close();
            }
            if (oxaBaseLineAStreamWriter != null) {
                oxaBaseLineAStreamWriter.close();
            }
        } catch (Exception e) {
            // TODO, handle exception
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //Register Oxa Listener
        DefaultNotifier.instance().addOxaListener(this);

        NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if(node != null)
        {
            oxa = node.findSensor(NodeEnums.ModuleType.OXA);
            oxa.startSensor();

            // karl addition
            File baseDirectory = ((NodeApplication) getActivity().getApplication()).mNodeFolder;
            if (baseDirectory != null) {
                for (String s : new String[]{"/oxa_reading.csv", "/oxa_baseline_a.csv"}) {
                    File oxaFragmentFile = new File(baseDirectory.getAbsolutePath() + s);
                    try {
                        if (!oxaFragmentFile.exists()) {
                            // TODO, check for failure
                            oxaFragmentFile.createNewFile();
                        }
                        if (s.equals("/oxa_reading.csv")) {
                            oxaReadingStreamWriter = new OutputStreamWriter(
                                    new FileOutputStream(oxaFragmentFile.getAbsolutePath()));
                            oxaReadingStreamWriter.write("timestamp,RAW\r\n");
                        }
                        if (s.equals("/oxa_baseline_a.csv")) {
                            oxaBaseLineAStreamWriter = new OutputStreamWriter(
                                    new FileOutputStream(oxaFragmentFile.getAbsolutePath()));
                        }
                    } catch (Exception e) {
                        // TODO, handle exception
                    }
                }
            }
        }
    }

    @Override
    public void onOxaBaselineUpdate(OxaSensor sensor, final SensorReading<Float> baseline_reading) {
       mHandler.obtainMessage(MessageConstants.MESSAGE_OXA_BASELINE_A,baseline_reading.getValue()).sendToTarget();
    }

    @Override
    public void onOxaUpdate(OxaSensor sensor, SensorReading<Float> reading) {
        Message m = mHandler.obtainMessage(MessageConstants.MESSAGE_OXA_READING);
        m.getData().putFloat(MessageConstants.FLOAT_VALUE_KEY, reading.getValue());
        m.sendToTarget();
    }

    private final Handler mHandler = new Handler(){
     private final DecimalFormat formatter = new DecimalFormat("0.0000");

     @Override
     public void handleMessage(Message message)
     {
        float value = message.getData().getFloat(MessageConstants.FLOAT_VALUE_KEY);
         String timestamp = new SimpleDateFormat("HHmmssSSS").format(new Date());
        switch(message.what){
                case MessageConstants.MESSAGE_OXA_READING:
                    String formattedVal = formatter.format(value);
                    oxaText.setText(formattedVal);
                    try {
                        if (oxaReadingStreamWriter != null) {
                            oxaReadingStreamWriter.write(timestamp + "," + formattedVal + "\r\n");
                        }
                    }  catch (Exception e) {
                        // TODO, handle exception
                    }
                    break;
                case MessageConstants.MESSAGE_OXA_BASELINE_A:
                    oxaBaseLineA.setText(message.obj.toString());
                    try {
                        if (oxaBaseLineAStreamWriter != null) {
                            oxaBaseLineAStreamWriter.write(timestamp + "," + message.obj.toString() + "\r\n");
                        }
                    }  catch (Exception e) {
                        // TODO, handle exception
                    }
                    break;
        }
      }
    };
}
