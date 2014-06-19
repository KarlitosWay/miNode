package com.variable.demo.api.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.variable.demo.api.ColorUtils;
import com.variable.framework.node.reading.ColorSense;
import com.variable.framework.node.ChromaDevice;
import com.variable.framework.dispatcher.DefaultNotifier;
import com.variable.framework.node.NodeDevice;

import com.variable.framework.node.enums.NodeEnums;
import com.variable.framework.node.reading.VTRGBCReading;

import java.util.Date;


/**
 * ChromaFragment is a base fragment with no implementation specfied for layouts and work flow.
 * Any instance will listen for new chroma scans and process them accordingly by passing the neccessary values to its handler.
 *
 * Additionally, this will allow for requesting a chroma reading via a button pressed by default. If this behavior is not to be expected then
 * Created by coreymann on 6/28/13.
 */

public class ChromaFragment extends Fragment implements ChromaDevice.ChromaListener{
        public static final String TAG = ChromaFragment.class.getName();

      /*The what in a new message */
        public static final int MESSAGE_NEW_READING = 0;
        public static final int MESSAGE_NEW_TEMPERATURE_READING = 1;

        public static final String  EXTRA_TIMESTAMP        = "com.variable.chroma.EXTRA_TIMESTAMP";
        public static final String  EXTRA_SCAN_COLOR       = "com.variable.chroma.EXTRA_SCAN_COLOR";
        public static final String EXTRA_COLOR_RED         = "com.variable.chroma.EXTRA_COLOR_RED";
        public static final String EXTRA_COLOR_GREEN       = "com.variable.chroma.EXTRA_COLOR_GREEN";
        public static final String EXTRA_COLOR_BLUE        = "com.variable.chroma.EXTRA_COLOR_BLUE";
        public static final String EXTRA_HEX_STRING        = "com.variable.chroma.EXTRA_HEX";
        public static final String  EXTRA_VALUE_L          = "com.variable.chroma.EXTRA_L";
        public static final String  EXTRA_VALUE_A          = "com.variable.chroma.EXTRA_A";
        public static final String  EXTRA_VALUE_B          = "com.variable.chroma.EXTRA_B";

        private NodeDevice.ButtonListener mButtonListener = new NodeDevice.ButtonListener() {
            //TODO: Test Button Pressed and Released Events
            @Override
            public void onPushed(NodeDevice nodeDevice) {
                Log.d(TAG, "onPushed()");
            }

            @Override
            public void onReleased(NodeDevice nodeDevice) {
                Log.d(TAG, "onReleased()");

                if(allowScanWhenButtonReleased()){
                    //By sleeping, this will allow the downward pressure to be released and
                    // avoid the propagation of errors during a chroma scan.
                    try {   Thread.sleep(500);  }
                    catch (InterruptedException e) {    e.printStackTrace();    }

                    //Issue a request for a new reading.
                    ChromaDevice chroma = nodeDevice.findSensor(NodeEnums.ModuleType.CHROMA);
                    if(chroma != null){
                        chroma.requestChromaReading();
                    }
                }
            }
        };



    @Override
    public void onResume(){
        super.onResume();

        //Register for Chroma Scans.
        DefaultNotifier.instance().addButtonListener(mButtonListener);
        DefaultNotifier.instance().addChromaListener(this);
    }

    @Override
    public void onPause(){
        super.onPause();

        //UnRegister for Chroma Scans
        DefaultNotifier.instance().removeButtonListener(mButtonListener);
        DefaultNotifier.instance().removeChromaListener(this);
    }

    /**
     * Builds a new message. This in turn will invoked
     *
     *    onRGBUpdate
     *    onTimeStampUpdate
     *    onColorUpdate
     *    onLABUpdate
     *
     *
     *
     * @param chromaDevice
     * @param reading
     */
    @Override
    public void  onChromaReadingReceived(ChromaDevice chromaDevice,VTRGBCReading reading){
        ColorSense sense = reading.getColorSense();
        Log.d(TAG, "SENSE_VALUES: " + sense.getSenseRed().floatValue() + " , " + sense.getSenseGreen() + " , " + sense.getSenseBlue() + " , " + sense.getSenseClear());

        Message msg = mHandler.obtainMessage(MESSAGE_NEW_READING);
        String hex = "";
        int color = 0;
                color = ColorUtils.RGBToColor(reading.getD65srgbR(), reading.getD65srgbG(), reading.getD65srgbB());

                msg.getData().putDouble(EXTRA_VALUE_A, reading.getD65a());
                msg.getData().putDouble(EXTRA_VALUE_L, reading.getD65L());
                msg.getData().putDouble(EXTRA_VALUE_B, reading.getD65b());
//D65 Values
//                color = ColorUtils.RGBToColor(reading.getD50srgbR(), reading.getD50srgbG(), reading.getD50srgbB());
//
//                msg.getData().putFloat(EXTRA_VALUE_A, reading.getD50a());
//                msg.getData().putFloat(EXTRA_VALUE_L, reading.getD50L());
//                msg.getData().putFloat(EXTRA_VALUE_B, reading.getD50b());

        hex = Integer.toHexString(color);
        hex = "#" + hex.substring(2);

        msg.getData().putString(EXTRA_HEX_STRING, hex.toUpperCase());

        msg.getData().putFloat(EXTRA_COLOR_RED, Color.red(color));
        msg.getData().putFloat(EXTRA_COLOR_GREEN, Color.green(color));
        msg.getData().putFloat(EXTRA_COLOR_BLUE, Color.blue(color));

        msg.getData().putLong(EXTRA_TIMESTAMP, reading.getTimeStamp().getTime());
        msg.getData().putInt(EXTRA_SCAN_COLOR, color);
        msg.sendToTarget();
    }

    /**
     * Invoked when a new reading has been recieved. Additionally, this method is invoked on the UI Thread.
     * @param red
     * @param green
     * @param blue
     */
    public void onRGBUpdate(float red, float green, float blue){ }

    /**
     * Invoked when a new reading has been recieved. Additionally, this method is invoked on the UI Thread.
     *
     * @param hexString - formatted such that #RRGGBB
     */
    public void onHexValue(String hexString){  }

    /**
     * Invoked when a new reading has been recieved. Additionally, this method is invoked on the UI Thread.
     * @param l
     * @param a
     * @param b
     */
    public void onLABUpdate(double l, double a, double b){ }

    /**
     * Invoked when a new reading has bee recieved. Additionally, this method is invoked on the UI Thread.
     * @param color
     */
    public void onColorUpdate(int color ) {  }

    /**
     * Invoked when a new reading has been recieved. Additionally, this method is invoked on the UI Thread.
     * @param timeStamp
     */
    public void onTimeStampUpdate(Date timeStamp){ }


    /**
     * Invoked when a new temperature reading has been recieved. Additionally, this method is invoked on the UI Thread.
     * @param temp
     */
    public void onTemperatureUpdate(float temp){    }

    /**
     * Gets the handler used in this instance of ChromaFragment.
     * @return
     */
    protected Handler getHandler(){ return mHandler; }

    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){

                case MESSAGE_NEW_READING:
                    Bundle data = msg.getData();

                    onRGBUpdate(data.getFloat(EXTRA_COLOR_RED),data.getFloat(EXTRA_COLOR_GREEN), data.getFloat(EXTRA_COLOR_BLUE));
                    onLABUpdate(data.getDouble(EXTRA_VALUE_L) ,data.getDouble(EXTRA_VALUE_A), data.getDouble(EXTRA_VALUE_B));
                    onColorUpdate(data.getInt(EXTRA_SCAN_COLOR));
                    onHexValue(data.getString(EXTRA_HEX_STRING));

                    Date timeStamp = new Date(data.getLong(EXTRA_TIMESTAMP));
                    onTimeStampUpdate(timeStamp);
                    break;

                case MESSAGE_NEW_TEMPERATURE_READING:
                    float temperature = Float.valueOf(msg.obj.toString());
                    onTemperatureUpdate(temperature);
            }
        }
    };


    public boolean allowScanWhenButtonReleased(){
        return true;
    }

    /**
     * Passes a new message, that will invoke onTemperatureUpdate(float) on the UI Thread.
     *
     * NOTE: This method is not on the UI Thread.
     *
     * @param device
     * @param temperature
     */
    @Override
    public void onChromaTemperatureReading(ChromaDevice device, Float temperature) {
        mHandler.obtainMessage(MESSAGE_NEW_TEMPERATURE_READING, temperature).sendToTarget();
    }

    @Override
    public void onWhitePointCalComplete(ChromaDevice device, boolean status) {
        Toast.makeText(getActivity(), "Chroma has finished calibration. result= " + (status ? "true" : "false"), Toast.LENGTH_SHORT).show();
    }
}
