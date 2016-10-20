package com.example.canonico.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Canonico on 12/12/2015.
 */

public class HeartTask extends AsyncTask implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor heartSensor;
    private HeartActivity mActivity;

    private double heartValue;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] params) {
       /* while(!this.isCancelled()){
            //    mActivity.setMTextVIew(""+heartValue);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        publishProgress(heartSensor);
        return null;
    }

    public HeartTask(HeartActivity activity){
        mActivity=activity;
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        heartSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        sensorManager.registerListener(this, heartSensor, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            heartValue=event.values[0];
            Log.i("mainActivity", "" + heartValue);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        mActivity.setRythmeCardique(heartValue);

    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        sensorManager.unregisterListener(this);
    }
}
