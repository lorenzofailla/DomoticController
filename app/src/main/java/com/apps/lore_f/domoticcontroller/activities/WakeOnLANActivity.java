package com.apps.lore_f.domoticcontroller.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.Image;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.DeviceData;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.WakeOnLANDeviceData;
import com.apps.lore_f.domoticcontroller.generic.classes.MessageStructure;
import com.apps.lore_f.domoticcontroller.services.FirebaseDBComm;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WakeOnLANActivity extends AppCompatActivity {

    public RecyclerView devicesRecyclerView;
    public LinearLayoutManager linearLayoutManager;
    public FirebaseRecyclerAdapter<WakeOnLANDeviceData, DevicesHolder> firebaseAdapter;
    public DatabaseReference availableDevices;

    private FirebaseDBComm firebaseDBComm;

    private String groupName;
    private String remoteDeviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake_on_lan);

        // handler
        devicesRecyclerView = findViewById(R.id.RVW___WAKEONLAN___DEVICESLIST);

        // Bind to LocalService
        Intent intent = new Intent(this, FirebaseDBComm.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {

        super.onResume();

    }

    @Override
    protected void onDestroy(){

        super.onDestroy();
        unbindService(connection);

    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            FirebaseDBComm.LocalBinder binder = (FirebaseDBComm.LocalBinder) service;
            firebaseDBComm = binder.getService();

            groupName = firebaseDBComm.getGroupName();
            remoteDeviceName = firebaseDBComm.getRemoteDeviceName();

            availableDevices = FirebaseDatabase.getInstance().getReference(String.format("Devices/%s/%s/StaticData/WakeOnLAN/Devices", groupName, remoteDeviceName));

            refreshAdapter();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }

    };

    public static class DevicesHolder extends RecyclerView.ViewHolder {

        TextView textViewDeviceName;
        ImageButton imageButtonWakeDevice;

        public DevicesHolder (View v){

            super(v);

            textViewDeviceName = v.findViewById(R.id.TXV___ROWWOLDEVICE___DEVICENAME);
            imageButtonWakeDevice = v.findViewById(R.id.BTN___ROWWOLDEVICE___CONNECT);

        }

    }

    private void refreshAdapter() {

        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<WakeOnLANDeviceData, DevicesHolder>(
                WakeOnLANDeviceData.class,
                R.layout.row_holder_wol_device_element,
                DevicesHolder.class,
                availableDevices) {

            @Override
            protected void populateViewHolder(DevicesHolder holder, final WakeOnLANDeviceData deviceData, int position) {

                holder.textViewDeviceName.setText(deviceData.getName());

                holder.imageButtonWakeDevice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        MessageStructure message = new MessageStructure("__wakeonlan", deviceData.getMACAddress(), firebaseDBComm.getThisDeviceName());
                        firebaseDBComm.sendCommandToDevice(message);
                        finish();
                        return;

                    }

                });
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
                    devicesRecyclerView.scrollToPosition(positionStart);
                }

            }

        });

        devicesRecyclerView.setLayoutManager(linearLayoutManager);
        devicesRecyclerView.setAdapter(firebaseAdapter);

    }

}
