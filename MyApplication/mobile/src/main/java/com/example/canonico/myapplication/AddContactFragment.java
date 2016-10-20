package com.example.canonico.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.Random;


public class AddContactFragment extends Fragment {
    private ContactsDataSource datasource;
    public AddContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        datasource = new ContactsDataSource(getActivity().getBaseContext());
        datasource.open();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_contact, container, false);

     }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button b = (Button) getView().findViewById(R.id.buttonAdd);
        final EditText editNum = (EditText) getView().findViewById(R.id.editTextNum);
        final EditText editNom = (EditText) getView().findViewById(R.id.editTextName);


        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Contact contact = null;
                String name=editNom.getText().toString();
                String num=editNum.getText().toString();
                Log.i("database", "avant if");
                if(name!=null && name!="" && num!=null && num!="" ){
                    int nextInt = new Random().nextInt(3);
                    contact=datasource.createContact(name,num);
                    AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
                    adb.setTitle("Ajout");
                    adb.setMessage("Le contact a bien été ajouté");
                    adb.show();
                    Log.i("database", "fin if");
                }
            }
        });
    }

}
