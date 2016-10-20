package com.example.canonico.myapplication;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telecom.TelecomManager;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.List;

/**
 * Created by Canonico on 11/12/2015.
 */
public class SmsTask extends AsyncTask {
    SmsManager smsManager;
    String name;
    double latitude;
    double longitude;
    private List<Contact> values;
    public SmsTask(String name,List<Contact> values,double latitude,double longitude){
        this.name=name;
        this.values=values;
        this.latitude=latitude;
        this.longitude=longitude;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        smsManager = SmsManager.getDefault();
        Log.i("SMS2", "avant for ");
       for(Contact c : values) {
           Log.i("SMS2", "contact : "+c);
           //sendSMS("0638650547", "CECI
           // EST UN MESSAGE AUTOMATIQUE. "+name+" vient de tomber, il risque de mourir dans d'atroces souffrances, envoyez '#J'ARRIVE' si vous souhaitez lui venir en aide." +
         //          name +" compte sur vous"+ c.getName());

            String msg= "CECI EST UN MESSAGE AUTOMATIQUE. " +name+ " vient de tomber envoyez OK si vous comptez venir."+ " On compte sur vous "+c.getName()+" latitude :  "+latitude+ " longitude : "+longitude;
           //msg="test"+ c.getName();

           sendSMS(c.getNum(),msg);
        }
        Log.i("SMS2", "apres for ");
        return null;
    }

    public void sendSMS(String phone,String message){
        smsManager.sendTextMessage(phone, null,message, null, null);
    }
}
