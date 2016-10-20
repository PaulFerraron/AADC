package com.example.canonico.myapplication;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Hans on 19/01/2016.
 */
public class MyServiceWear extends Service implements SensorEventListener {
    private final String PATH_ALARM = "/alarm";
    private final String PATH_STOP = "/stop";
    private final String PATH_CARDIAQUE = "/rythme";

    private LocationManager locationMgr = null;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float[] sensorValue;
    private String sensorXValueString = "0";
    private String sensorYValueString = "0";
    private String sensorZValueString = "0";
    private String sensorRezValueString = "0";
    private float sensorXValue=0, sensorYValue=0, sensorZValue=0;

    private GoogleApiClient apiClient;
    private NodeApi.NodeListener nodeListener;
    private MessageApi.MessageListener messageListener;
    private String remoteNodeId;
    private Handler handler;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler();



        // Create MessageListener that receives messages sent from a mobile
        messageListener = new MessageApi.MessageListener() {
            @Override
            public void onMessageReceived(MessageEvent messageEvent) {
                if (messageEvent.getPath().equals(PATH_ALARM)) {
                    Intent dialogIntent = new Intent(MyServiceWear.this, AlertActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dialogIntent);
                } else if (messageEvent.getPath().equals(PATH_STOP)) {

                }else if (messageEvent.getPath().equals(PATH_CARDIAQUE)){
                    Intent dialogIntent = new Intent(MyServiceWear.this, HeartActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dialogIntent);
                }
            }
        };
        nodeListener = new NodeApi.NodeListener() {
            @Override
            public void onPeerConnected(Node node) {
                remoteNodeId = node.getId();
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(getApplication(), getString(R.string.peer_connected), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onPeerDisconnected(Node node) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(getApplication(), getString(R.string.peer_disconnected), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        // Create GoogleApiClient
        apiClient = new GoogleApiClient.Builder(getApplicationContext()).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                // Register Node and Message listeners
                Wearable.NodeApi.addListener(apiClient, nodeListener);
                Wearable.MessageApi.addListener(apiClient, messageListener);
                // If there is a connected node, get it's id that is used when sending messages
                Wearable.NodeApi.getConnectedNodes(apiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        if (getConnectedNodesResult.getStatus().isSuccess() && getConnectedNodesResult.getNodes().size() > 0) {
                            remoteNodeId = getConnectedNodesResult.getNodes().get(0).getId();
                            //buttonAlarm.setEnabled(true);


                        }
                    }
                });
            }

            @Override
            public void onConnectionSuspended(int i) {
                //buttonAlarm.setEnabled(false);

            }
        }).addApi(Wearable.API).build();


        apiClient.connect();



    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.equals(mAccelerometer)) {
            sensorValue = event.values;

            sensorXValue = sensorValue[0];
            sensorYValue = sensorValue[1];
            sensorZValue = sensorValue[2];
            if(sensorXValue > 15 || sensorYValue > 15  || sensorZValue > 15 )
            {

                NotificationCompat.WearableExtender wearableExtender =
                        new NotificationCompat.WearableExtender()
                                .setHintShowBackgroundOnly(true);

                Notification notification =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Chute detection")
                                .setContentText("Vous êtes tombé.")
                                .extend(wearableExtender)
                                .build();

                NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(this);

                int notificationId = 1;
                notificationManager.notify(notificationId, notification);
                Intent dialogIntent = new Intent(this, AlertActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(dialogIntent);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Wearable.MessageApi.sendMessage(apiClient, remoteNodeId, PATH_ALARM, null);
                    }
                }, 500);

            }



            sensorXValueString = String.valueOf(sensorXValue);
            sensorYValueString = String.valueOf(sensorYValue);
            sensorZValueString = String.valueOf(sensorZValue);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}