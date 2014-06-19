package com.variable.demo.api.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.variable.demo.api.MessageConstants;
import com.variable.demo.api.NodeApplication;
import com.variable.demo.api.R;
import com.variable.framework.dispatcher.DefaultNotifier;
import com.variable.framework.node.MotionSensor;
import com.variable.framework.node.NodeDevice;
import com.variable.framework.node.reading.MotionReading;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by coreymann on 8/13/13.
 */
public class MotionFragment extends Fragment  implements
                                                    MotionSensor.AccelerometerListener,
                                                    MotionSensor.MagnetometerListener,
                                                    MotionSensor.GyroscopeListener {
    public static  final String TAG = MotionFragment.class.getName();
    private static final int DECIMAL_PRECISION = 1;

    private SeekBar accelX;
    private SeekBar accelY;
    private SeekBar accelZ;

    private SeekBar gyroX;
    private SeekBar gyroY;
    private SeekBar gyroZ;

    private SeekBar magX;
    private SeekBar magY;
    private SeekBar magZ;

    private MotionSensor motion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.motion, null, false);
        accelX = (SeekBar) root.findViewById(R.id.accelX);
        accelY = (SeekBar) root.findViewById(R.id.accelY);
        accelZ = (SeekBar) root.findViewById(R.id.accelZ);

        //Change the Values from [-16, 16] to [0,32] and add 1 places of precision by multiplying 10.
        int accelMax = 16 * 2 * DECIMAL_PRECISION;
        accelX.setMax(accelMax);
        accelY.setMax(accelMax);
        accelZ.setMax(accelMax);


        gyroX = (SeekBar) root.findViewById(R.id.gyroX);
        gyroY = (SeekBar) root.findViewById(R.id.gyroY);
        gyroZ = (SeekBar) root.findViewById(R.id.gyroZ);

        int gyroMax = 2000 * 2 * DECIMAL_PRECISION;
        gyroX.setMax(gyroMax);
        gyroY.setMax(gyroMax);
        gyroZ.setMax(gyroMax);


        magX = (SeekBar) root.findViewById(R.id.magX);
        magY = (SeekBar) root.findViewById(R.id.magY);
        magZ = (SeekBar) root.findViewById(R.id.magZ);

        int magMax = 6 * 2 * DECIMAL_PRECISION;
        magX.setMax(magMax);
        magY.setMax(magMax);
        magZ.setMax(magMax);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        //Register for Events
        DefaultNotifier.instance().addAccelerometerListener(this);
        DefaultNotifier.instance().addGyroscopeListener(this);
        DefaultNotifier.instance().addMagnetometerListener(this);


        //Start the Motion Stream
        NodeDevice node = ((NodeApplication) getActivity().getApplication()).getActiveNode();
        if(node != null)
        {
            motion = node.getMotionSensor();
            motion.startSensor();
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        //Stop the Motion Stream
        motion.stopSensor();


        //Unregister for Events
        DefaultNotifier.instance().removeAccelerometerListener(this);
        DefaultNotifier.instance().removeMagnetometerListener(this);
        DefaultNotifier.instance().removeGyroscopeListener(this);
    }


    private final Handler mHandler = new Handler()
    {
      @Override
      public void handleMessage(Message msg)
      {
          Bundle b = msg.getData();
          float x, y, z;
          if(b != null){
              x = b.getFloat(MessageConstants.X_VALUE_KEY);
              y = b.getFloat(MessageConstants.Y_VALUE_KEY);
              z = b.getFloat(MessageConstants.Z_VALUE_KEY);
          }else { x =0;  y=0; z =0; }
          switch(msg.what){
              case MessageConstants.MESSAGE_ACCELEROMETER_READING:
                  Log.d(TAG, "Accel: " + x + " , " + y + " , " + z);
                  accelX.setProgress((int) x);
                  accelY.setProgress((int) y);
                  accelZ.setProgress((int) z);
                  break;
              case MessageConstants.MESSAGE_GYROSCOPE_READING:
                  gyroX.setProgress((int) x);
                  gyroY.setProgress((int) y);
                  gyroZ.setProgress((int) z);
                  break;
              case MessageConstants.MESSAGE_MAGNETOMETER_READING:
                  magX.setProgress((int) x);
                  magY.setProgress((int) y);
                  magZ.setProgress((int) z);
                  break;
          }

      }
    };

    @Override
    public void onAccelerometerUpdate(MotionSensor sensor, MotionReading reading) {
        DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm.ss");

       // Log.d(TAG, "TimeStamp Source: " + reading.getTimeStampSource());
       // Log.d(TAG," Time:" + formatter.format(reading.getTimeStamp()));

        Message m = mHandler.obtainMessage(MessageConstants.MESSAGE_ACCELEROMETER_READING);
        Bundle b = m.getData();
        b.putFloat(MessageConstants.X_VALUE_KEY, (reading.getX() + 16) * DECIMAL_PRECISION);
        b.putFloat(MessageConstants.Y_VALUE_KEY, (reading.getY() + 16) * DECIMAL_PRECISION);
        b.putFloat(MessageConstants.Z_VALUE_KEY, (reading.getZ() + 16) * DECIMAL_PRECISION);

        //For this demo we are streaming all time stamp from the node device.
        b.putLong(MessageConstants.TIME_STAMP, reading.getTimeStamp().getTime());
        b.putInt(MessageConstants.TIME_SOURCE, reading.getTimeStampSource());

        m.sendToTarget();
    }


    @Override
    public void onMagnetometerUpdate(MotionSensor sensor, MotionReading reading) {
        Message m = mHandler.obtainMessage(MessageConstants.MESSAGE_MAGNETOMETER_READING);
        Bundle b = m.getData();
        b.putFloat(MessageConstants.X_VALUE_KEY, (reading.getX() + 6) * DECIMAL_PRECISION);
        b.putFloat(MessageConstants.Y_VALUE_KEY, (reading.getY() + 6) * DECIMAL_PRECISION);
        b.putFloat(MessageConstants.Z_VALUE_KEY, (reading.getZ() + 6) * DECIMAL_PRECISION);

        //For this demo we are streaming all time stamp from the node device.
        b.putLong(MessageConstants.TIME_STAMP, reading.getTimeStamp().getTime());
        b.putInt(MessageConstants.TIME_SOURCE, reading.getTimeStampSource());

        m.sendToTarget();
    }

    @Override
    public void onGyroscopeUpdate(MotionSensor sensor, MotionReading reading) {
        Message m = mHandler.obtainMessage(MessageConstants.MESSAGE_GYROSCOPE_READING);
        Bundle b = m.getData();
        b.putFloat(MessageConstants.X_VALUE_KEY, (reading.getX() + 2000) * DECIMAL_PRECISION);
        b.putFloat(MessageConstants.Y_VALUE_KEY, (reading.getY() + 2000) * DECIMAL_PRECISION);
        b.putFloat(MessageConstants.Z_VALUE_KEY, (reading.getZ() + 2000) * DECIMAL_PRECISION);

        //For this demo we are streaming all time stamp from the node device.
        b.putLong(MessageConstants.TIME_STAMP, reading.getTimeStamp().getTime());
        b.putInt(MessageConstants.TIME_SOURCE, reading.getTimeStampSource());

        m.sendToTarget();
    }
}
