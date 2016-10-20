package com.example.canonico.myapplication;

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
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.nio.Buffer;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * Created by Hans on 19/01/2016.
 */
public class MyServicePhone extends Service implements SensorEventListener {

    private final String PATH_ALARM = "/alarm";
    private final String PATH_STOP = "/stop";

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
    private Queue<Float> bufferX = new ArrayDeque<Float>();
    private Queue<Float> bufferY= new ArrayDeque<Float>();
    private Queue<Float> bufferZ = new ArrayDeque<Float>();
    private float somX=0;
    private float somY=0;
    private float somZ=0;

    private float som2X=0;
    private float som2Y=0;
    private float som2Z=0;

    private int plateauX=0;
    private int plateauY=0;
    private int plateauZ=0;




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();

        handler  = new Handler();
//        final Vibrator vibrator = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//
//                if (vibrator.hasVibrator()) {
//                    long[] pattern = {0, 500, 600};
//                    vibrator.vibrate(pattern, 1);
//                }
//            }
//        });
        final Intent dialogIntent = new Intent(this, AlarmActivity.class);
        // Create MessageListener that receives messages sent from a mobile
        messageListener = new MessageApi.MessageListener() {
            @Override
            public void onMessageReceived(MessageEvent messageEvent) {
                if (messageEvent.getPath().equals(PATH_ALARM)) {

                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(dialogIntent);
                } else if (messageEvent.getPath().equals(PATH_STOP)) {

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
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);

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

            bufferX.add(sensorValue[0]);
            bufferY.add(sensorValue[1]);
            bufferZ.add(sensorValue[2]);

            if(bufferX.size()>16) {
                float x,y,z;
                x=bufferX.poll();
                y=bufferY.poll();
                z=bufferZ.poll();

                somX-=x;
                somY-=y;
                somZ-=z;

                som2X-=x*x;
                som2Y-=y*y;
                som2Z-=z*z;
            }

            somX+=sensorValue[0];
            somY+=sensorValue[1];
            somZ+=sensorValue[2];

            som2X+=sensorValue[0]*sensorValue[0];
            som2Y+=sensorValue[1]*sensorValue[1];
            som2Z+=sensorValue[2]*sensorValue[2];


            float resX, resY,resZ;

            resX = (som2X-somX*somX)/16;
            resY = (som2Y-somY*somY)/16;
            resZ = (som2Z-somZ*somZ)/16;

            if(resX>14)
                plateauX++;
            else
                plateauX/=2;

            if(resY>14)
                plateauY++;
            else
                plateauY/=2;

            if(resZ>14)
                plateauZ++;
            else
                plateauZ/=2;
            //a ? (b || c) : (b && c);


            if(plateauX > 4 ? (plateauY>4 || plateauZ >4) :(plateauY>4 && plateauZ >4) )
            {
                Intent dialogIntent = new Intent(this, AlarmActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(dialogIntent);
            }


            sensorXValueString = String.valueOf(plateauX);
            sensorYValueString = String.valueOf(plateauY);
            sensorZValueString = String.valueOf(plateauZ);
            if (plateauX !=0 || plateauY!=0 || plateauZ!=0)
            System.out.println(sensorXValueString+"  " +sensorYValueString + "  "+sensorZValueString);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
