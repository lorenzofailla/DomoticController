package com.apps.lore_f.domoticcontroller.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.DeviceData;
import com.apps.lore_f.domoticcontroller.generic.classes.MessageStructure;
import com.apps.lore_f.domoticcontroller.services.FirebaseDBComm;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import apps.android.loref.GeneralUtilitiesLibrary;

public class DeviceInfoViewActivity extends AppCompatActivity {

    private FirebaseDBComm firebaseDBComm;
    private DatabaseReference deviceInfo;

    private Toolbar toolbar;
    private DeviceData deviceData;

    private TextView textViewSystemLoad;
    private TextView textViewFreeDiskSpace;
    private TextView textViewPublicIP;
    private TextView textViewLocalIP;
    private TextView textViewRunningSince;
    private TextView textViewLastUpdate;

    private ValueEventListener deviceStatusData = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            //deviceData=dataSnapshot.getValue(new GenericTypeIndicator<DeviceData>() {});
            deviceData=dataSnapshot.getValue(DeviceData.class);
            refreshUI();

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private String groupName;
    private String remoteDeviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info_view);

        // Bind to LocalService
        Intent intent = new Intent(this, FirebaseDBComm.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        // set up the toolbar of this activity
        toolbar = findViewById(R.id.TBR___DEVICEINFOVIEW___TOOLBAR);
        toolbar.inflateMenu(R.menu.deviceview_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.MENU_DEVICEVIEW_SHUTDOWN:
                        shutdownHost();
                        return true;

                    case R.id.MENU_DEVICEVIEW_REBOOT:
                        rebootHost();
                        return true;

                    default:
                        return false;

                }
            }

        });

        //
        textViewFreeDiskSpace = findViewById(R.id.TXV___DEVICEINFOVIEW___DISKSTATUS_VALUE);
        textViewSystemLoad = findViewById(R.id.TXV___DEVICEINFOVIEW___SYSLOAD_VALUE);
        textViewPublicIP = findViewById(R.id.TXV___DEVICEINFOVIEW___PRIVATEIP_VALUE);
        textViewLocalIP = findViewById(R.id.TXV___DEVICEINFOVIEW___PUBLICIP_VALUE);
        textViewRunningSince = findViewById(R.id.TXV___DEVICEINFOVIEW___RUNNINGSINCE_VALUE);
        textViewLastUpdate = findViewById(R.id.TXV___DEVICEINFOVIEW___LASTUPDATE_VALUE);

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

            deviceInfo = FirebaseDatabase.getInstance().getReference(String.format("Devices/%s/%s", groupName, remoteDeviceName));
            deviceInfo.addValueEventListener(deviceStatusData);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }

    };

    private void refreshUI(){

        toolbar.setTitle(deviceData.getDeviceName());
        textViewSystemLoad.setText(String.format("%d%%",deviceData.getAverageLoad()));
        textViewFreeDiskSpace.setText(String.format("%.0f MB",deviceData.getAvailableDiskSpace()));
        textViewPublicIP.setText(deviceData.getPublicIPAddress());
        textViewLocalIP.setText(deviceData.getLocalIPAddresses());
        textViewRunningSince.setText(GeneralUtilitiesLibrary.getTimeElapsed(deviceData.getRunningSince(),this));
        textViewLastUpdate.setText(GeneralUtilitiesLibrary.getTimeElapsed(deviceData.getLastUpdate(),this));

    }

    private void shutdownHost(){
        firebaseDBComm.sendCommandToDevice(new MessageStructure("__execcmd", "shutdown -h now", firebaseDBComm.getThisDeviceName()));
    }

    private void rebootHost(){
        firebaseDBComm.sendCommandToDevice(new MessageStructure("__execcmd", "reboot", firebaseDBComm.getThisDeviceName()));
    }


}
