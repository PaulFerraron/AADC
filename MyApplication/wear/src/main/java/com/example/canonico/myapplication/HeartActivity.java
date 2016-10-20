package com.example.canonico.myapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import java.nio.ByteBuffer;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class HeartActivity extends Activity implements SensorEventListener {

    private TextView mTextView;
    private MessageApi.MessageListener messageListener;
    private GoogleApiClient apiClient;
    private NodeApi.NodeListener nodeListener;
    private final String PATH_CARDIAQUE = "/rythme";
    private final String PATH_CARDIAQUE_RESULT = "/rythme/value";
    private HeartTask task;
    private SensorManager sensorManager;
    private Sensor heartSensor;
    private String remoteNodeId;



    private double heartValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rect_activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startService(new Intent(HeartActivity.this, MyServiceWear.class));


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        heartSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        sensorManager.registerListener(HeartActivity.this, heartSensor, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

        mTextView=(TextView) findViewById(R.id.textViewCardiaque);
        final Handler h = new Handler();
        ; //milliseconds
        int delay = 1000;
        h.postDelayed(new Runnable(){
            public void run(){
                int delay = 1000;
                mTextView.setText("" + heartValue);
                byte[] bytes = new byte[8];
                ByteBuffer.wrap(bytes).putDouble(heartValue);
                Wearable.MessageApi.sendMessage(apiClient, remoteNodeId, PATH_CARDIAQUE_RESULT, bytes).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {


                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        Log.i("wear ", sendMessageResult.toString() + " " + heartValue);
                    }


                });
                h.postDelayed(this, delay);
            }
        }, delay);

        nodeListener = new NodeApi.NodeListener() {
            @Override
            public void onPeerConnected(Node node){
                remoteNodeId = node.getId();
                Log.i("mobile", "On peer connected");
            }

            @Override
            public void onPeerDisconnected(Node node) {
                Log.i("mobile","On peer disconnected");
            }
        };

        messageListener = new MessageApi.MessageListener() {

            @Override
            public void onMessageReceived(MessageEvent messageEvent) {
                Log.i("mobile","message received");
                //

                if (messageEvent.getPath().equals(PATH_CARDIAQUE)) {
                    //sensorManager.unregisterListener(HeartActivity.this);

                    mTextView.setText("" + heartValue);
                    byte[] bytes = new byte[8];
                    ByteBuffer.wrap(bytes).putDouble(heartValue);


                    Wearable.MessageApi.sendMessage(apiClient, remoteNodeId, PATH_CARDIAQUE_RESULT, bytes).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {


                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.i("wear ", sendMessageResult.toString() + " " + heartValue);
                        }


                    });
                }
            }
        };
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
                        Log.i("main","On result");
                        if (getConnectedNodesResult.getStatus().isSuccess() && getConnectedNodesResult.getNodes().size() > 0) {
                            remoteNodeId = getConnectedNodesResult.getNodes().get(0).getId();

                            Log.i("main","On result success");
                        }
                    }
                });
            }


            @Override
            public void onConnectionSuspended(int i) {

            }
        }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
            }
        }).addApi(Wearable.API).build();
    }

    public void setRythmeCardique(double heartValue) {
        mTextView.setText(""+heartValue);
    }

    private void launchTask(){
        task=new HeartTask(this);
        task.execute();
    }


    @Override
    protected void onResume() {
        super.onResume();
        int connectionResult = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (connectionResult != ConnectionResult.SUCCESS) {
            // Google Play Services is NOT available. Show appropriate error dialog
            GooglePlayServicesUtil.showErrorDialogFragment(connectionResult, this, 0, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
        } else {
            apiClient.connect();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.MessageApi.removeListener(apiClient, messageListener);
        apiClient.disconnect();

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
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
}

