package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.firebase.dataobjects.RemoteDevGeneralStatus;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.RemoteDevNetworkStatus;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import apps.android.loref.GeneralUtilitiesLibrary;

public class DeviceInfoFragment extends Fragment {

    public String systemLoad;
    public String freeSpace;

    public boolean viewCreated = false;
    private View fragmentView;

    private static final String TAG = "DeviceInfoFragment";

    private DeviceViewActivity parent;
    public void setParent(DeviceViewActivity p){
        parent = p;
    }

    // VPN management
    private boolean isVPNConnected;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.BTN___DEVICEINFOFRAGMENT___REBOOT:

                    parent.rebootHost();
                    break;

                case R.id.BTN___DEVICEINFOFRAGMENT___SHUTDOWN:

                    parent.shutdownHost();
                    break;

                case R.id.BTN___DEVICEINFOFRAGMENT___CLEARLOG:

                    break;

                case R.id.BTN___DEVICEINFOFRAGMENT___MANAGE_VPN:

                    if (isVPNConnected) {
                        // il dispositivo remoto è connesso alla VPN.
                        // richiede all'Activity parent di mandare un comando per disconnettere dalla VPN

                        parent.sendCommandToDevice(
                                new Message(
                                        "__disconnect_vpn",
                                        null,
                                        parent.thisDevice
                                )
                        );

                    } else {

                        // il dispositivo remoto non è connesso alla VPN.
                        // richiede all'Activity parent di mandare un comando per connettere alla VPN

                        parent.sendCommandToDevice(
                                new Message(
                                        "__connect_vpn",
                                        null,
                                        parent.thisDevice
                                )
                        );

                    }
                    break;

            }

        }
    };

    private ValueEventListener vpnStatusValueEventListener = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (dataSnapshot != null)
                if (dataSnapshot.getValue() != null)
                    manageVPNStatus(dataSnapshot.getValue().toString());

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };


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
        fragmentView = view;

        // inizializza la visualizzazione dello stato della connessione TCP
        int labelToShow = R.string.GENERIC_PLACEHOLDER_WAITING;

        if(parent.getIsTCPCommInterfaceAvailable()){

            labelToShow = R.string.DEVICEVIEW_LABEL_TCP_STATUS_CONNECTED;

        } else {

            labelToShow = R.string.DEVICEVIEW_LABEL_TCP_STATUS_NOT_CONNECTED;

        }

        TextView tcpConnectionStatusLabel = (TextView) view.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___TCP_STATUS_VALUE);
        tcpConnectionStatusLabel.setText(labelToShow);

        // assegna un OnClickListener ai pulsanti
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___CLEARLOG).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___REBOOT).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___SHUTDOWN).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___MANAGE_VPN).setOnClickListener(onClickListener);

        DatabaseReference vpnStatusNode = FirebaseDatabase.getInstance().getReference(String.format("/Groups/%s/Devices/%s/VPNStatus",parent.groupName, parent.remoteDeviceName));
        vpnStatusNode.addValueEventListener(vpnStatusValueEventListener);

        // aggiorna il flag e effettua il trigger del metodo nel listener
        viewCreated = true;

        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

    @Override
    public void onDetach() {
        super.onDetach();

        // rimuove il ValueEventListener dal nodo del Database di Firebase
        DatabaseReference vpnStatusNode = FirebaseDatabase.getInstance().getReference("/Groups/%s/devices/%s/VPNStatus");
        vpnStatusNode.removeEventListener(vpnStatusValueEventListener);

        // rimuove l'OnClickListener ai pulsanti
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___CLEARLOG).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___REBOOT).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___SHUTDOWN).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___MANAGE_VPN).setOnClickListener(null);

    }

    private void manageVPNStatus(String status) {

        if (fragmentView == null) {
            return;
        }

        int drawableToShow;
        String textToShow;
        boolean switchButtonEnabled;

        switch (status) {

            case "<not-available>":

                // vpn is not available
                drawableToShow = R.drawable.broken;
                textToShow = getString(R.string.DEVICEVIEW_LABEL_VPN_STATUS_NOT_AVAILABLE);
                switchButtonEnabled = false;
                break;

            case "<not-connected>":
                drawableToShow = R.drawable.vpn_black;
                textToShow = getString(R.string.DEVICEVIEW_LABEL_VPN_STATUS_NOT_CONNECTED);
                switchButtonEnabled = true;
                isVPNConnected = false;
                break;

            default:
                drawableToShow = R.drawable.vpn_green;
                textToShow = String.format("%s IP: %s.", getString(R.string.DEVICEVIEW_LABEL_VPN_STATUS_CONNECTED), status);
                switchButtonEnabled = true;
                isVPNConnected = true;
                break;
        }

        ImageButton switchButton = (ImageButton) fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___MANAGE_VPN);
        TextView vpnStatusTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___VPN_STATUS_VALUE);

        switchButton.setEnabled(switchButtonEnabled);
        switchButton.setImageResource(drawableToShow);
        vpnStatusTextView.setText(textToShow);

    }

    public void setGeneralStatus(RemoteDevGeneralStatus status){

        if(fragmentView!=null){

            TextView systemLoadTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___SYSLOAD_VALUE);
            TextView diskStatusTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___DISKSTATUS_VALUE);
            TextView runningSinceTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___RUNNINGSINCE_VALUE);
            TextView lastUpdateTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___LASTUPDATE_VALUE);

            systemLoadTextView.setText(status.getSystemLoad());
            diskStatusTextView.setText(status.getDiskStatus());
            runningSinceTextView.setText(GeneralUtilitiesLibrary.getTimeElapsed(status.getRunningSince(), getContext()));
            lastUpdateTextView.setText(GeneralUtilitiesLibrary.getTimeElapsed(status.getLastUpdate(), getContext()));

        }

    };

    public void setNetworkStatus(RemoteDevNetworkStatus status){
        // TODO: 09/09/2018 implementare
    };

}
