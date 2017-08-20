package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import apps.lore_f.instantmessaging.InstantMessaging;

import static com.google.android.gms.internal.zzagz.runOnUiThread;

public class ConnectionFragment extends Fragment {

    public enum ConnectionStatus{
        NOT_CONNECTED,
        IN_PROGRESS,
        READY
    }

    public ConnectionStatus connectionStatus=null;

    public String currentAction;

    private View fragmentView;

    interface ConnectionFragmentListener{

        void onViewCreated(ConnectionFragment fragment);
        void onConnectRequest();
        void onReconnectRequest();

    }

    private View.OnClickListener reconnectRequest = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (connectionFragmentListener!=null) connectionFragmentListener.onReconnectRequest();

        }

    };

    private View.OnClickListener connectRequest = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (connectionFragmentListener!=null) connectionFragmentListener.onConnectRequest();

        }

    };


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

        /* inizializza l'handler alla view, in questo modo i componenti possono essere ritrovati */
        fragmentView=view;

        /* lancia il callback che informa che la view è stata creata */
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

    public void updateView(){

        if (connectionStatus!=null) {

            final int showProgressBar;
            final int showConnectionButton;
            final View.OnClickListener clickListener;
            final int connectionButtonText;

            switch (connectionStatus) {

                case NOT_CONNECTED:

                    showProgressBar=View.INVISIBLE;
                    showConnectionButton=View.VISIBLE;
                    connectionButtonText=R.string.CONNECTIONFGM_BTN_CONNECT;
                    clickListener=connectRequest;

                    break;

                case IN_PROGRESS:

                    showProgressBar=View.VISIBLE;
                    showConnectionButton=View.INVISIBLE;
                    connectionButtonText=R.string.EMPTY;
                    clickListener=null;
                    break;

                case READY:

                    showProgressBar=View.INVISIBLE;
                    showConnectionButton=View.VISIBLE;
                    connectionButtonText=R.string.CONNECTIONFGM_BTN_RECONNECT;
                    clickListener=reconnectRequest;

                    break;

                default:
                    showProgressBar=View.INVISIBLE;
                    showConnectionButton=View.INVISIBLE;
                    connectionButtonText=R.string.EMPTY;
                    clickListener=null;


            }

            runOnUiThread(

                    new Runnable() {

                        @Override
                        public void run() {

                            /* imposta la visibilità dei componenti */
                            fragmentView.findViewById(R.id.PBR___CONNECTIONFRAGMENT).setVisibility(showProgressBar);

                            Button connectionActionButton = (Button) fragmentView.findViewById(R.id.BTN___CONNECTIONFRAGMENT___CONNECTIONACTION);
                            connectionActionButton.setVisibility(showConnectionButton);

                            if(showConnectionButton==View.VISIBLE) {
                                connectionActionButton.setText(connectionButtonText);
                                connectionActionButton.setOnClickListener(clickListener);
                            }

                            /* imposta il testo della TextView */
                            TextView mainTXV = (TextView)fragmentView.findViewById(R.id.TXV___CONNECTIONFRAGMENT___MAIN);
                            mainTXV.setText(currentAction);

                        }

                    }

            );

        }

    }



}
