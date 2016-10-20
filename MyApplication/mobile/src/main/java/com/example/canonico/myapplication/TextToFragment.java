package com.example.canonico.myapplication;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.Locale;


public class TextToFragment extends Fragment implements
        TextToSpeech.OnInitListener{
    private TextToSpeech tts;
    private Button btnSpeak;
    private EditText txtText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_text_to, container, false);

        return rootView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tts = new TextToSpeech(getActivity(),this);

        btnSpeak = (Button) getView().findViewById(R.id.buttonSpeak);
        txtText = (EditText) getView().findViewById(R.id.editText);
       btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                speakOut();


            }
        });
    }
    @Override
    public void onInit(int status) {
        Log.i("main activity", "OnInit");
        if (status == TextToSpeech.SUCCESS) {
            Log.i("main activity","OnInit success");
            int result = tts.setLanguage(Locale.FRANCE);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
               // btnSpeak.setEnabled(true);
                Log.i("main activity", "apres call");
               // speakOut();
            }

        } else {
            Log.i("main activity","OnInit failed");
            Log.e("TTS", "Initilization Failed!");
        }
    }


    private void speakOut() {

        String text = txtText.getText().toString();

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}
