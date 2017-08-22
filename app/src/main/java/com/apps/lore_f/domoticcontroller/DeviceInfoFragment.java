package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import static com.google.android.gms.internal.zzagz.runOnUiThread;

public class DeviceInfoFragment extends Fragment {

    public String upTime;
    public String availableServices;
    public String freeSpace;

    public boolean viewCreated = false;
    private View fragmentView;

    interface DeviceInfoFragmentListener{

        void onViewCreated();

    }

    private DeviceInfoFragmentListener deviceInfoFragmentListener;

    public void setDeviceInfoFragmentListener(DeviceInfoFragmentListener listener){
        deviceInfoFragmentListener=listener;
    }

    public void removeDeviceInfoFragmentListener(){
        deviceInfoFragmentListener=null;

    }


    public DeviceInfoFragment() {
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
        View view = inflater.inflate(R.layout.fragment_device_info, container, false);

        // inizializza l'handler alla view, in questo modo i componenti possono essere ritrovati
        fragmentView=view;

        // aggiorna il flag e effettua il trigger del metodo nel listener
        viewCreated = true;
        if(deviceInfoFragmentListener!=null) deviceInfoFragmentListener.onViewCreated();

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

        TextView upTimeReplyTXV = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEVIEW___UPTIME_VALUE);
        upTimeReplyTXV.setText(upTime);

        TextView availableServicesTXV = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEVIEW___AVAILABLESERVICES_VALUE);
        availableServicesTXV.setText(availableServices);

        TextView freeSpaceTXV = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEVIEW___FREEDISKSPACE_VALUE);
        freeSpaceTXV.setText(freeSpace);

    }



}
