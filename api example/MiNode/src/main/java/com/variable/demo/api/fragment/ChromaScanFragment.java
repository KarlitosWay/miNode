package com.variable.demo.api.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.variable.demo.api.NodeApplication;
import com.variable.demo.api.R;
import com.variable.framework.node.ChromaDevice;
import com.variable.framework.node.NodeDevice;
import com.variable.framework.node.enums.NodeEnums;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by Corey Mann on 8/28/13.
 */
public class ChromaScanFragment extends ChromaFragment {


    public static final String TAG = ChromaScanFragment.class.getName();
    private final DecimalFormat formatter = new DecimalFormat("###.##");
    private ChromaDevice chroma;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup view, Bundle savedInstanced){
        super.onCreateView(inflater,view, savedInstanced);

        final View rootView = inflater.inflate(R.layout.single_scan, null, false);
        rootView.findViewById(R.id.btnSingleScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               chroma.requestChromaReading();
            }
        });

        NodeDevice node  = ((NodeApplication)getActivity().getApplication()).getActiveNode();
        chroma = node.findSensor(NodeEnums.ModuleType.CHROMA);

        if(chroma.getCalibrationList().size() == 52 && chroma.getModel() == 2.0f){
            new AlertDialog.Builder(getActivity())
                    .setTitle("Warning")
                    .setMessage("Your chroma needs to be returned to Variable inc for recall. Please contact us. Chroma is not ensured to work properly until the recall has been satisfied.")
                    .setPositiveButton("OK", null)
                    .show();
        }

        return rootView;
    }

    @Override
    public void onColorUpdate(int color){
        super.onColorUpdate(color);

        getView().findViewById(R.id.imgScanColor).setBackgroundColor(color);
    }

    @Override
    public void onRGBUpdate(float r, float g, float b){
        super.onRGBUpdate(r, g, b);

        String text = formatter.format(r) + " , " + formatter.format(g) + " , " + formatter.format(b);
        ((TextView) getView().findViewById(R.id.txtRGB)).setText(text);
    }


    @Override
    public void onLABUpdate(double l, double a, double b){
        super.onLABUpdate(l, a, b);
        String text = formatter.format(l) + " , " + formatter.format(a) + " , " + formatter.format(b);
        ((TextView) getView().findViewById(R.id.txtLab)).setText(text);
    }


    @Override
    public void onHexValue(String hex){
        super.onHexValue(hex);
        ((TextView) getView().findViewById(R.id.txtHex)).setText(hex);
    }
}
