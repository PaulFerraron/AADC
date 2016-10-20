package com.example.canonico.myapplication;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;


public class AlarmFragment extends Fragment {
    private final String PATH_ALARM = "/alarm";
    private final String PATH_STOP = "/stop";

    private EditText receivedMessagesEditText;
    private View buttonAlarm;
    private View buttonStop;
    private GoogleApiClient apiClient;
    private NodeApi.NodeListener nodeListener;
    private String remoteNodeId;
    private MessageApi.MessageListener messageListener;
    private Handler handler;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;


    public AlarmFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().startService(new Intent(getActivity(), MyServicePhone.class));
        handler = new Handler();
        PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "My Tag");
        wl.acquire();

        buttonAlarm = getActivity().findViewById(R.id.button_alarm);
        buttonStop = getActivity().findViewById(R.id.button_stop);
        mediaPlayer = MediaPlayer.create(getActivity().getApplicationContext(), R.raw.alarm);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Wearable.MessageApi.sendMessage(apiClient, remoteNodeId, PATH_ALARM, null);
            }
        }, 500);
        vibrator = (Vibrator) getActivity().getSystemService(getActivity().getApplicationContext().VIBRATOR_SERVICE);

        playAlarm();
        startVibrate();
        buttonAlarm.setEnabled(true);
        buttonStop.setEnabled(true);

        // Set message1Button onClickListener to send message 1
        buttonAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Wearable.MessageApi.sendMessage(apiClient, remoteNodeId, PATH_ALARM, null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        playAlarm();
                        startVibrate();

                        if (sendMessageResult.getStatus().isSuccess()) {
                            Toast.makeText(getActivity().getApplication(), getString(R.string.message1_sent), Toast.LENGTH_SHORT).show();

                        } else
                            Toast.makeText(getActivity().getApplication(), getString(R.string.error_message1), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopAlarm();
                stopVibrate();


                Wearable.MessageApi.sendMessage(apiClient, remoteNodeId, PATH_STOP, null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (sendMessageResult.getStatus().isSuccess()) {
                            Toast.makeText(getActivity().getApplication(), getString(R.string.message1_sent), Toast.LENGTH_SHORT).show();

                        } else
                            Toast.makeText(getActivity().getApplication(), getString(R.string.error_message1), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });


        // Create NodeListener that enables buttons when a node is connected and disables buttons when a node is disconnected
        nodeListener = new NodeApi.NodeListener() {
            @Override
            public void onPeerConnected(Node node) {
                remoteNodeId = node.getId();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        buttonAlarm.setEnabled(true);
                        buttonStop.setEnabled(true);
                        Toast.makeText(getActivity().getApplication(), getString(R.string.peer_connected), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onPeerDisconnected(Node node) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        buttonAlarm.setEnabled(false);
                        buttonStop.setEnabled(false);
                        Toast.makeText(getActivity().getApplication(), getString(R.string.peer_disconnected), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        // Create MessageListener that receives messages sent from a wearable
        messageListener = new MessageApi.MessageListener() {
            @Override
            public void onMessageReceived(MessageEvent messageEvent) {
                if (messageEvent.getPath().equals(PATH_ALARM)) {
                    playAlarm();
                    startVibrate();

                } else if (messageEvent.getPath().equals(PATH_STOP)) {
                    stopAlarm();
                    stopVibrate();

                }
            }
        };

        // Create GoogleApiClient
        apiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext()).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
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
                            buttonAlarm.setEnabled(true);
                            buttonStop.setEnabled(true);
                        }
                    }
                });
            }

            @Override
            public void onConnectionSuspended(int i) {
                buttonAlarm.setEnabled(false);
                buttonStop.setEnabled(false);
            }
        }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                if (connectionResult.getErrorCode() == ConnectionResult.API_UNAVAILABLE)
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.wearable_api_unavailable), Toast.LENGTH_LONG).show();
            }
        }).addApi(Wearable.API).build();
    }


    void playAlarm() {

        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);

        handler.post(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        });



    }


    void stopAlarm() {
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();

                }

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alarm, container, false);
    }


    @Override
    public void onResume() {
        super.onResume();

        // Check is Google Play Services available
        int connectionResult = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity().getApplicationContext());

        if (connectionResult != ConnectionResult.SUCCESS) {
            // Google Play Services is NOT available. Show appropriate error dialog
            GooglePlayServicesUtil.showErrorDialogFragment(connectionResult, getActivity(), 0, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    getActivity().finish();
                }
            });
        } else {
            apiClient.connect();
        }
    }

    public void onPause() {
        // Unregister Node and Message listeners, disconnect GoogleApiClient and disable buttons
        Wearable.NodeApi.removeListener(apiClient, nodeListener);
        Wearable.MessageApi.removeListener(apiClient, messageListener);
        apiClient.disconnect();
        buttonAlarm.setEnabled(false);
        buttonStop.setEnabled(false);
        super.onPause();
    }

    void startVibrate() {
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (vibrator.hasVibrator()) {
                    long[] pattern = {0, 400, 200};
                    vibrator.vibrate(pattern, 0);
                }
            }
        });
    }


    void stopVibrate() {
        handler.post(new Runnable() {
            @Override
            public void run() {


                vibrator.cancel();


            }
        });
    }



}
