package com.apps.lore_f.domoticcontroller.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.DefaultValues;
import com.apps.lore_f.domoticcontroller.DeviceSelectionActivity;
import com.apps.lore_f.domoticcontroller.DeviceViewActivity;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.DeviceToConnect;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.LogEntry;
import com.apps.lore_f.domoticcontroller.generic.classes.Message;
import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.RemoteDevGeneralStatus;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.RemoteDevNetworkStatus;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import apps.android.loref.GeneralUtilitiesLibrary;

public class DeviceInfoFragment extends Fragment {

    private static final String TAG = "DeviceInfoFragment";

    public boolean viewCreated = false;
    private View fragmentView;

    private DeviceViewActivity parent;

    public void setParent(DeviceViewActivity p) {
        parent = p;
    }

    private RemoteDevGeneralStatus remoteDevGeneralStatus=null;
    private RemoteDevNetworkStatus remoteDevNetworkStatus=null;

    public RecyclerView logsRecyclerView;
    public LinearLayoutManager linearLayoutManager;
    public FirebaseRecyclerAdapter<LogEntry, LogsHolder> firebaseAdapter;
    private DatabaseReference logsNode;

    public static class LogsHolder extends RecyclerView.ViewHolder {

        public TextView logTimeStamp;
        public TextView logInfo;
        public TextView logDescription;

        public LogsHolder(View v) {
            super(v);

            logTimeStamp = (TextView) itemView.findViewById(R.id.TXV___LOG___TIMESTAMP);
            logInfo = (TextView) itemView.findViewById(R.id.TXV___LOG___INFO);
            logDescription = (TextView) itemView.findViewById(R.id.TXV___LOG___DESCRIPTION);

        }

    }

    private void refreshLogsAdapter() {

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<LogEntry, LogsHolder>(
                LogEntry.class,
                R.layout.row_holder_log_element,
                LogsHolder.class,
                logsNode) {

            @Override
            protected void populateViewHolder(LogsHolder holder, LogEntry log, int position) {

                holder.logTimeStamp.setText(
                        GeneralUtilitiesLibrary.getTimeElapsed(
                                Long.parseLong(
                                        log.getDatetime()
                                ), getContext()
                        )
                );

                holder.logDescription.setText(log.getLogdesc());
                holder.logInfo.setText(log.getLogtype());

            }

        };

        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int mediaCount = firebaseAdapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (mediaCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    logsRecyclerView.scrollToPosition(positionStart);
                }

            }

        });

        logsRecyclerView.setLayoutManager(linearLayoutManager);
        logsRecyclerView.setAdapter(firebaseAdapter);

    }

    private ValueEventListener logsValueEventLister = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            refreshLogsAdapter();

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };

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

                    DatabaseReference logs = FirebaseDatabase.getInstance().getReference(String.format("/Groups/%s/Logs/%s", parent.groupName, parent.remoteDeviceName));
                    logs.removeValue();

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

                case R.id.BTN___DEVICEINFOFRAGMENT___MANAGE_TCP:

                    if (parent.getTCPCommInterfaceStatus()) {
                        // device is connected to remote host via TCP.
                        // TCP Comm interface will be disconnected.

                        parent.getTcpComm().disconnect();

                    } else {
                        // device is not connected to remote host via TCP.
                        // start the TCP Comm interface connection

                        parent.startTCPInterfaceTest();
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
        Log.d(TAG, "DeviceInfoFragment constructor call.");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "DeviceInfoFragment onCreate.");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "DeviceInfoFragment onCreateView.");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_device_info, container, false);

        // inizializza l'handler alla view, in questo modo i componenti possono essere ritrovati
        fragmentView = view;

        // assegna un OnClickListener ai pulsanti
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___CLEARLOG).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___REBOOT).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___SHUTDOWN).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___MANAGE_VPN).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___MANAGE_TCP).setOnClickListener(onClickListener);

        DatabaseReference vpnStatusNode = FirebaseDatabase.getInstance().getReference(String.format("/Groups/%s/Devices/%s/VPNStatus", parent.groupName, parent.remoteDeviceName));
        vpnStatusNode.addValueEventListener(vpnStatusValueEventListener);

        logsNode = FirebaseDatabase.getInstance().getReference(String.format("/Groups/%s/Logs/%s", parent.groupName, parent.remoteDeviceName));
        logsNode.addValueEventListener(logsValueEventLister);

        // aggiorna il flag e effettua il trigger del metodo nel listener
        viewCreated = true;

        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.d(TAG, "DeviceInfoFragment onAttach.");
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // rimuove il ValueEventListener dal nodo del Database di Firebase
        DatabaseReference vpnStatusNode = FirebaseDatabase.getInstance().getReference("/Groups/%s/devices/%s/VPNStatus");
        vpnStatusNode.removeEventListener(vpnStatusValueEventListener);

        logsNode.removeEventListener(logsValueEventLister);

        // rimuove l'OnClickListener ai pulsanti
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___CLEARLOG).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___REBOOT).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___SHUTDOWN).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___MANAGE_VPN).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___MANAGE_TCP).setOnClickListener(null);

        Log.d(TAG, "DeviceInfoFragment onDetach.");

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

    public void setGeneralStatus(RemoteDevGeneralStatus status) {

        remoteDevGeneralStatus = status;

        refreshView();

    }

    public void setNetworkStatus(RemoteDevNetworkStatus status) {

        remoteDevNetworkStatus = status;

        refreshView();

    }

    public void updateTCPStatus() {

        if (fragmentView != null) {
            // inizializza la visualizzazione dello stato della connessione TCP
            String labelToShow = getString(R.string.GENERIC_PLACEHOLDER_WAITING);

            if (parent.getCurrentTCPHostAddrIndex() > 0 && parent.getTCPCommInterfaceStatus()) {

                labelToShow = getString(R.string.DEVICEVIEW_LABEL_TCP_STATUS_CONNECTED) + " to: " + parent.getCurrentTCPHostAddress();

            } else {

                labelToShow = getString(R.string.DEVICEVIEW_LABEL_TCP_STATUS_NOT_CONNECTED);

            }

            TextView tcpConnectionStatusLabel = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___TCP_STATUS_VALUE);
            tcpConnectionStatusLabel.setText(labelToShow);

        }

    }

    public void refreshView() {

        if (fragmentView != null) {

            if (remoteDevGeneralStatus != null) {

                TextView systemLoadTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___SYSLOAD_VALUE);
                TextView diskStatusTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___DISKSTATUS_VALUE);
                TextView runningSinceTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___RUNNINGSINCE_VALUE);
                TextView lastUpdateTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___LASTUPDATE_VALUE);

                systemLoadTextView.setText(remoteDevGeneralStatus.getSystemLoad());
                diskStatusTextView.setText(remoteDevGeneralStatus.getDiskStatus());
                runningSinceTextView.setText(GeneralUtilitiesLibrary.getTimeElapsed(remoteDevGeneralStatus.getRunningSince(), getContext()));
                lastUpdateTextView.setText(GeneralUtilitiesLibrary.getTimeElapsed(remoteDevGeneralStatus.getLastUpdate(), getContext()));

            }

            if (remoteDevNetworkStatus != null) {

                TextView publicIPTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___PUBLICIP_VALUE);
                TextView localIPTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___PRIVATEIP_VALUE);

                publicIPTextView.setText(remoteDevNetworkStatus.getPublicIP());
                localIPTextView.setText(remoteDevNetworkStatus.getLocalIP());
            }

        } else {

            Log.d(TAG, "View of the fragment is null.");

        }

        updateTCPStatus();

    }

}
