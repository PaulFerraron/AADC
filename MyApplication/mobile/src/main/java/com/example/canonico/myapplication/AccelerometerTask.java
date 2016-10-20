package com.example.canonico.myapplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.AsyncTask;

/**
 * Created by Canonico on 12/12/2015.
 */
public class AccelerometerTask extends AsyncTask implements SensorEventListener {

    private float[] sensorValue;
    private String sensorXValueString = "0";
    private String sensorYValueString = "0";
    private String sensorZValueString = "0";

    private float sensorXValue=0, sensorYValue=0, sensorZValue=0;

    @Override
    protected Object doInBackground(Object[] params) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
