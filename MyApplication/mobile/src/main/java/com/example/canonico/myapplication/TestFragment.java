package com.example.canonico.myapplication;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Canonico on 10/12/2015.
 */
public class TestFragment extends Fragment {
    public TestFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frament_test, container, false);

        return rootView;
    }

}
