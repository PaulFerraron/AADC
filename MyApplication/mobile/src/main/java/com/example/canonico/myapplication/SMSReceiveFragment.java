package com.example.canonico.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SMSReceiveFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    public SMSReceiveFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_smsreceive, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("test1", "avant");
        BroadcastReceiver smsReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // Get the data (SMS data) bound to intent
                Bundle bundle = intent.getExtras();

                SmsMessage[] msgs = null;

                String str = "";

                if (bundle != null) {
                    // Retrieve the SMS Messages received
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];

                    // For every SMS message received
                    for (int i=0; i < msgs.length; i++) {
                        // Convert Object array
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        // Sender's phone number
                        str += "SMS from " + msgs[i].getOriginatingAddress() + " : ";
                        // Fetch the text message
                        str += msgs[i].getMessageBody().toString();
                        // Newline <img src="http://codetheory.in/wp-includes/images/smilies/simple-smile.png" alt=":-)" class="wp-smiley" style="height: 1em; max-height: 1em;">
                        str += "\n";
                    }

                    // Display the entire SMS Message
                    Log.d("test1", str);
                }
            }

        };

        Log.i("test1", "apres");
    }
}