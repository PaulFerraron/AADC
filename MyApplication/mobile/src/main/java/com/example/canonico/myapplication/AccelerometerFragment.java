package com.example.canonico.myapplication;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class AccelerometerFragment extends Fragment implements SensorEventListener {

    private AccelerometerTask task;
    private float[] sensorValue;
    private String sensorXValueString = "0";
    private String sensorYValueString = "0";
    private String sensorZValueString = "0";
    private Sensor accelerator;
    private SensorManager sensorManager;
    private float sensorXValue=0, sensorYValue=0, sensorZValue=0;
    private TextView textViewX;
    private TextView textViewY;
    private TextView textViewZ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_accelerometer, container, false);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.equals(accelerator)) {
            sensorValue = event.values;

            sensorXValue = sensorValue[0];
            sensorYValue = sensorValue[1];
            sensorZValue = sensorValue[2];


            sensorXValueString = String.valueOf(sensorXValue);
            sensorYValueString = String.valueOf(sensorYValue);
            sensorZValueString = String.valueOf(sensorZValue);

            if(sensorXValueString!=null && sensorYValueString!=null && sensorZValueString!=null){
                textViewX.setText(sensorXValueString);
                textViewY.setText(sensorYValueString);
                textViewZ.setText(sensorZValueString);
            }

        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        textViewX = (TextView) getActivity().findViewById(R.id.textViewX);
        textViewY = (TextView) getActivity().findViewById(R.id.textViewY);
        textViewZ = (TextView) getActivity().findViewById(R.id.textViewZ);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerator = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerator, SensorManager.SENSOR_DELAY_UI);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
}
