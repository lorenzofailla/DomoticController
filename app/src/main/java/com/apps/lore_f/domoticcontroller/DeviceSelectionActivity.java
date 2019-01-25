package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.firebase.dataobjects.DeviceToConnect;
import com.apps.lore_f.domoticcontroller.generic.classes.DeviceDataParser;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import apps.android.loref.GeneralUtilitiesLibrary;

public class DeviceSelectionActivity extends AppCompatActivity {

    private final static String TAG = "DeviceSelectionActivity";

    public RecyclerView devicesRecyclerView;
    public LinearLayoutManager linearLayoutManager;
    public FirebaseRecyclerAdapter<DeviceToConnect, DevicesHolder> firebaseAdapter;

    private Query onlineDevices;

    private String groupName;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent intent;
            switch (view.getId()) {

                case R.id.BTN___DEVICE_SELECTION___CLOUDSTORAGE:

                    // avvia l'activity per la gestione del cloud storage
                    intent = new Intent(getApplicationContext(), CloudStorageActivity.class);
                    startActivity(intent);

                    break;

                case R.id.BTN___DEVICE_SELECTION___VIDEOSURVEILLANCE:

                    // avvia l'activity per la gestione della videosorveglianza
                    intent = new Intent(getApplicationContext(), VideoSurveillanceActivity.class);
                    startActivity(intent);

                    break;

            }

        }

    };

    public static class DevicesHolder extends RecyclerView.ViewHolder {

        public TextView deviceNameTxv;

        public ImageButton connectToDeviceBtn;
        public ImageButton buttonShowAdditionalData;

        public ImageView torrentImg;
        public ImageView directoryNaviImg;
        public ImageView videoSurveillanceImg;
        public ImageView wakeOnLanImg;

        public TextView deviceRunningSinceTxv;
        public TextView deviceLastUpdateTxv;

        public ConstraintLayout deviceLastUpdateData;
        public ConstraintLayout deviceAdditionalData;

        public boolean showAdditionalData = false;

        public DevicesHolder(View v) {

            super(v);
            deviceNameTxv = (TextView) itemView.findViewById(R.id.TXV___ROWDEVICE___DEVICENAME);

            connectToDeviceBtn = (ImageButton) itemView.findViewById(R.id.BTN___ROWDEVICE___CONNECT);
            torrentImg = (ImageView) itemView.findViewById(R.id.IMG___ROWDEVICE___TORRENT);
            directoryNaviImg = (ImageView) itemView.findViewById(R.id.IMG___ROWDEVICE___DIRNAVI);
            videoSurveillanceImg = (ImageView) itemView.findViewById(R.id.IMG___ROWDEVICE___VIDEOSURVEILLANCE);
            wakeOnLanImg = (ImageView) itemView.findViewById(R.id.IMG___ROWDEVICE___WAKEONLAN);
            deviceLastUpdateTxv = (TextView) itemView.findViewById(R.id.TXV___ROWDEVICE___STATUS_LASTUPDATE);
            deviceRunningSinceTxv = (TextView) itemView.findViewById(R.id.TXV___ROWDEVICE___STATUS_RUNNINGSINCE);

            deviceLastUpdateData = (ConstraintLayout) itemView.findViewById(R.id.CLA___ROWDEVICE___LASTUPDATE);
            deviceAdditionalData = (ConstraintLayout) itemView.findViewById(R.id.CLA___ROWDEVICE___ADDITIONALDATA);
            buttonShowAdditionalData = (ImageButton) itemView.findViewById(R.id.BTN___ROWDEVICE___EXPANDDATA);

            buttonShowAdditionalData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    setShowAdditionalData(!DevicesHolder.this.showAdditionalData);

                }

            });

        }

        public void setShowAdditionalData(boolean value) {
            this.showAdditionalData = value;

            if (this.showAdditionalData) {

                deviceAdditionalData.setVisibility(View.VISIBLE);
                buttonShowAdditionalData.setImageResource(R.drawable.less);

            } else {

                deviceAdditionalData.setVisibility(View.GONE);
                buttonShowAdditionalData.setImageResource(R.drawable.more);

            }

        }

        public boolean getShowAdditionalData() {
            return this.showAdditionalData;
        }

    }

    private ValueEventListener valueEventListener = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            try {

                Log.i(TAG, dataSnapshot.getValue().toString());

            } catch (NullPointerException e) {

                Log.i(TAG, "Cannot show datasnapshot");

            }

            /*
            aggiorna il contenuto della label
             */

            TextView availableDevicesTextView = (TextView) findViewById(R.id.TXV___DEVICE_SELECTION___LABEL);

            long availableDevicesNumber = dataSnapshot.getChildrenCount();
            String message;

            if (availableDevicesNumber > 0) {
                message = String.format(getString(R.string.DEVICESELECTION_LABEL_SELECT_DEVICE), availableDevicesNumber);
            } else {
                message = getString(R.string.DEVICESELECTION_LABEL_NO_DEVICE);
            }

            availableDevicesTextView.setText(message);

            // aggiorna l'adapter
            refreshAdapter();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, databaseError.getMessage());
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_selection);

        /*
        recupera gli extra dalle preferenze, se il nome del gruppo non è stato specificato, l'Activity non può proseguire e viene riaperta l'Activity per la selezione del gruppo
         */

        // recupera il nome del gruppo [R.string.data_group_name] dalle shared preferences
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.data_file_key), Context.MODE_PRIVATE);

        groupName = sharedPref.getString(getString(R.string.data_group_name), null);

        if (groupName == null) {

            /*
            questa parte di codice non dovrebbe essere mai eseguita, viene tenuta per evitare eccezioni
             */

            // nome del gruppo non impostato, lancia l'Activity GroupSelection per selezionare il gruppo a cui connettersi
            startActivity(new Intent(this, GroupSelection.class));

            // termina l'Activity corrente
            finish();
            return;

        }

        FirebaseMessaging.getInstance().subscribeToTopic(groupName);

    }

    @Override
    protected void onResume() {

        super.onResume();

        // handler
        devicesRecyclerView = (RecyclerView) findViewById(R.id.RWV___DEVICE_SELECTION___DEVICES);

        // assegno OnClickListener
        findViewById(R.id.BTN___DEVICE_SELECTION___CLOUDSTORAGE).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___DEVICE_SELECTION___VIDEOSURVEILLANCE).setOnClickListener(onClickListener);

        // cerca i dispositivi online nel database
        DatabaseReference userNode = FirebaseDatabase.getInstance().getReference(String.format("/Groups/%s", groupName));

        onlineDevices = userNode.child("Devices").orderByChild("Online").equalTo(true);
        onlineDevices.addValueEventListener(valueEventListener);

    }

    @Override
    protected void onPause() {

        super.onPause();

        // rimuove OnClickListener
        findViewById(R.id.BTN___DEVICE_SELECTION___CLOUDSTORAGE).setOnClickListener(null);
        findViewById(R.id.BTN___DEVICE_SELECTION___VIDEOSURVEILLANCE).setOnClickListener(null);

        onlineDevices.removeEventListener(valueEventListener);

    }

    private void refreshAdapter() {

        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<DeviceToConnect, DevicesHolder>(
                DeviceToConnect.class,
                R.layout.row_holder_device_element,
                DevicesHolder.class,
                onlineDevices) {

            @Override
            protected void populateViewHolder(DevicesHolder holder, final DeviceToConnect device, int position) {

                holder.deviceNameTxv.setText(device.getDeviceName());

                final DeviceDataParser deviceData = new DeviceDataParser(device.getStatusData(), device.getNetworkData(), device.getStaticData());

                // gestisce la visualizzazione delle immagini in funzione della capability del dispositivo
                //
                if (deviceData.isHasTorrent()) {
                    //
                    holder.torrentImg.setVisibility(View.VISIBLE);

                }

                if (deviceData.isHasFileManager()) {
                    //
                    holder.directoryNaviImg.setVisibility(View.VISIBLE);

                }

                if (deviceData.isHasVideoSurveillance()) {
                    //
                    holder.videoSurveillanceImg.setVisibility(View.VISIBLE);

                }

                if (deviceData.isHasWakeOnLan()) {
                    //
                    holder.wakeOnLanImg.setVisibility(View.VISIBLE);

                }

                if (device.getOnline()) {

                    holder.connectToDeviceBtn.setEnabled(true);

                    holder.connectToDeviceBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            connectToDevice(
                                    device.getDeviceName(),
                                    device.getStaticData(),
                                    deviceData.getLastUpdate() != -1 && ((System.currentTimeMillis() - deviceData.getLastUpdate()) > DefaultValues.LAST_UPDATE_TOO_FAR)
                            );

                        }

                    });

                } else {

                    holder.connectToDeviceBtn.setEnabled(false);
                    holder.connectToDeviceBtn.setImageResource(R.drawable.shutdown);

                }

                holder.connectToDeviceBtn.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return false;
                    }
                });

                // aggiorna la label con l'informazione sull'ultimo update

                if (deviceData.getLastUpdate() != -1 && ((System.currentTimeMillis() - deviceData.getLastUpdate()) > DefaultValues.LAST_UPDATE_TOO_FAR)) {

                    String message = getString(R.string.DEVICEELEMENT_LABEL_LAST_UPDATE) + GeneralUtilitiesLibrary.getTimeElapsed(deviceData.getLastUpdate(), getApplicationContext(), false);
                    holder.deviceLastUpdateTxv.setText(message);
                    holder.deviceLastUpdateData.setVisibility(View.VISIBLE);

                } else {

                    holder.deviceLastUpdateData.setVisibility(View.GONE);

                }

                // aggiorna la label con l'informazione sul tempo di funzionamento del server

                long runningSince = deviceData.getRunningSince();
                ;

                if (runningSince != -1) {

                    String message = getString(R.string.DEVICEELEMENT_LABEL_RUNNING_SINCE) + GeneralUtilitiesLibrary.getTimeElapsed(runningSince, getApplicationContext(), false);
                    holder.deviceRunningSinceTxv.setText(message);

                }

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

    private void connectToDevice(
            String deviceName,
            String staticData,
            boolean criticalConnection
    ) {

        Intent intent = new Intent(this, DeviceViewActivity.class);

        if (criticalConnection) {
            intent.putExtra(DeviceViewActivity.CONNECTIONMETHOD_TAG, DeviceViewActivity.CONNECTIONMETHOD_FIREBASE_CRITICAL);
        } else {
            intent.putExtra(DeviceViewActivity.CONNECTIONMETHOD_TAG, DeviceViewActivity.CONNECTIONMETHOD_FIREBASE);
        }
        intent.putExtra(DeviceViewActivity.DEVICE_TO_CONNECT_TAG, deviceName);
        intent.putExtra(DeviceViewActivity.STATICDATA_JSON_TAG, staticData);

        startActivity(intent);

    }

    private void connectToDeviceTCP(
            String ipAddressesList) {

        Intent intent = new Intent(this, DeviceViewActivity.class);
        intent.putExtra(DeviceViewActivity.CONNECTIONMETHOD_TAG, DeviceViewActivity.CONNECTIONMETHOD_TCP);
        intent.putExtra(DeviceViewActivity.SESSIONMODE_TAG, DeviceViewActivity.SESSIONMODE_NEW);

        startActivity(intent);

    }

}
