package com.example.canonico.myapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.nio.ByteBuffer;


public class CardiaqueFragment extends Fragment {
    private final String PATH_CARDIAQUE = "/rythme";
    private final String PATH_CARDIAQUE_RESULT = "/rythme/value";
    private GoogleApiClient apiClient;
    private String remoteNodeId;
    private View buttonCardiaque;
    private NodeApi.NodeListener nodeListener;
    private TextView mTextView;
    private MessageApi.MessageListener messageListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_cardiaque, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        buttonCardiaque = getView().findViewById(R.id.buttonCardiaque);
        mTextView = (TextView) getView().findViewById(R.id.textView3);
        messageListener = new MessageApi.MessageListener() {
            @Override
            public void onMessageReceived(MessageEvent messageEvent) {
                if (messageEvent.getPath().equals(PATH_CARDIAQUE_RESULT)) {
                    Double v = ByteBuffer.wrap(messageEvent.getData()).getDouble();
                    mTextView.setText("" + v);
                }

            }
        };
        nodeListener = new NodeApi.NodeListener() {


            @Override
            public void onPeerConnected(Node node) {
                remoteNodeId = node.getId();
                Log.i("mobile", "peer connected");
            }

            @Override
            public void onPeerDisconnected(Node node) {

            }
        };


        apiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext()).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                Log.i("wear ", "onConnected");

                // Register Node and Message listeners
                Wearable.NodeApi.addListener(apiClient, nodeListener);
                Wearable.MessageApi.addListener(apiClient, messageListener);

                // If there is a connected node, get it's id that is used when sending messages
                Wearable.NodeApi.getConnectedNodes(apiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        if (getConnectedNodesResult.getStatus().isSuccess() && getConnectedNodesResult.getNodes().size() > 0) {
                            remoteNodeId = getConnectedNodesResult.getNodes().get(0).getId();
                            Log.i("wear ", "remoteNodeId " + remoteNodeId);


                        }
                    }
                });
            }


            @Override
            public void onConnectionSuspended(int i) {
                Log.i("wear ", "susp  ");

            }
        }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                Log.i("wear ", "fail  ");
            }
        }).addApi(Wearable.API).build();


        buttonCardiaque.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("wear ", "click button");
                Log.i("wear ", "remoteNodeId : " + remoteNodeId);
                Wearable.MessageApi.sendMessage(apiClient, remoteNodeId, PATH_CARDIAQUE, null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {


                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        Log.i("wear ", sendMessageResult.toString());
                    }


                });
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
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

    @Override
    public void onPause() {
        super.onPause();
        Wearable.NodeApi.removeListener(apiClient, nodeListener);
        //Wearable.MessageApi.removeListener(apiClient, messageListener);
        apiClient.disconnect();
    }
}

