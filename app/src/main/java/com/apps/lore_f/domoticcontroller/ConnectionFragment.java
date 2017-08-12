package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import apps.lore_f.instantmessaging.InstantMessaging;

public class ConnectionFragment extends Fragment {

    public enum ConnectionStatus{
        CONNECTED,
        IN_PROGRESS,
        NOT_CONNECTED
    }

    public ConnectionStatus connectionStatus;

    private  View frgView;

    private TextView mainTXV;
    private ProgressBar progressBar;

    interface ConnectionFragmentListener{

        void onViewCreated(ConnectionFragment fragment);

    }

    ConnectionFragmentListener connectionFragmentListener;

    public void setConnectionFragmentListener(ConnectionFragmentListener listener){

        connectionFragmentListener=listener;

    }

    public void removeConnectionFragmentListener(){

        connectionFragmentListener=null;

    }

    public ConnectionFragment() {
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
        View view = inflater.inflate(R.layout.fragment_connection, container, false);

        /* inizializza gli handler ai drawable */
        mainTXV = (TextView) view.findViewById(R.id.TXV___CONNECTIONFRAGMENT___MAIN);
        progressBar =(ProgressBar) view.findViewById(R.id.PBR___CONNECTIONFRAGMENT);

        if(connectionFragmentListener!=null) connectionFragmentListener.onViewCreated(this);

        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public void setLabelText(String message){

         mainTXV.setText(message);

    }

    public void setLabelText(int message){

        mainTXV.setText(message);

    }



}
