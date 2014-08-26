package com.variable.demo.api.fragment;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.variable.demo.api.NodeApplication;
import com.variable.demo.api.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Karl O'Neill on 25/08/14.
 */
public class GPSFragment extends Fragment implements LocationListener{
    public static final String TAG = GPSFragment.class.getName();

    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 0; // in Meters

    protected LocationManager locationManager;

    private TextView gpsLongTxt;
    private TextView gpsLatTxt;
    private TextView gpsHtTxt;

    private OutputStreamWriter gpsStreamWriter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.gps, null, false);

        gpsLongTxt = (TextView) root.findViewById(R.id.txtGpsLongitude);
        gpsLatTxt  = (TextView) root.findViewById(R.id.txtGpsLatitude);
        gpsHtTxt   = (TextView) root.findViewById(R.id.txtGpsHeight);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MINIMUM_TIME_BETWEEN_UPDATES,
                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                this
        );

        File baseDirectory = ((NodeApplication) getActivity().getApplication()).mNodeFolder;
        if (baseDirectory != null) {
            File gpsFragmentFile = new File(baseDirectory.getAbsolutePath() + "/gps.csv");
            try {
                if (!gpsFragmentFile.exists()) {
                    // TODO, check for failure
                    gpsFragmentFile.createNewFile();
                }
                gpsStreamWriter = new OutputStreamWriter(
                        new FileOutputStream(gpsFragmentFile.getAbsolutePath()));
                gpsStreamWriter.write("timestamp,longitude,latitude,height\r\n");

            } catch (Exception e) {
                // TODO, handle exception
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            if (gpsStreamWriter != null) {
                gpsStreamWriter.close();
            }
        } catch (Exception e) {
            // TODO, handle exception
        }
    }

    public void onLocationChanged(Location location) {

        String gpsLongStr = Double.toString(location.getLongitude());
        String gpsLatStr = Double.toString(location.getLatitude());
        String gpsHtStr = Double.toString(location.getAltitude());

        gpsLongTxt.setText(gpsLongStr);
        gpsLatTxt.setText(gpsLatStr);
        gpsHtTxt.setText(gpsHtStr);

        String timestamp = new SimpleDateFormat("HHmmssSSS").format(new Date());

        try {
            if (gpsStreamWriter != null) {
                gpsStreamWriter.write(timestamp + "," + gpsLongStr + "," + gpsLatStr + "," + gpsHtStr + "\r\n");
            }
        } catch (Exception e) {
            // TODO, handle exception
        }
    }

    public void onStatusChanged(String s, int i, Bundle b) {
        Toast.makeText(getActivity(), "Provider status changed",
                Toast.LENGTH_LONG).show();
    }

    public void onProviderDisabled(String s) {
        Toast.makeText(getActivity(),
                "Provider disabled by the user. GPS turned off",
                Toast.LENGTH_LONG).show();
    }

    public void onProviderEnabled(String s) {
        Toast.makeText(getActivity(),
                "Provider enabled by the user. GPS turned on",
                Toast.LENGTH_LONG).show();
    }

}
