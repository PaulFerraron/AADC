package com.example.canonico.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.MapView;

import java.util.List;


public class SMSFragment extends Fragment implements LocationListener {


    private List<Contact> values;
    private ContactsDataSource datasource;


    private LocationManager lm;

    private double latitude;
    private double longitude;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        View rootView = inflater.inflate(R.layout.fragment_sm, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        datasource = new ContactsDataSource(getActivity().getBaseContext());
        datasource.open();
        values = datasource.getAllContacts();
        Log.i("SMS2", "values : " + values);
        for(Contact c :values){
            Log.i("SMS2","contact : "+c);
        }
        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0,
                    this);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0,
                this);

          Button b = (Button) getView().findViewById(R.id.buttonSms);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("SMS",values.toString());
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String name=prefs.getString("example_text",null);
                Log.i("SMS2",values.toString());
                SmsTask task=new SmsTask(name,values,latitude,longitude);
                task.execute();


            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("geolocalisation","onLocationChanged");
        latitude = location.getLatitude();
        longitude = location.getLongitude();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
