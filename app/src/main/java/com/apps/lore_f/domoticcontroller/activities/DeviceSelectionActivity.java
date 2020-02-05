package com.apps.lore_f.domoticcontroller.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.lore_f.domoticcontroller.CloudStorageActivity;
import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.asynctask.TCPComm;
import com.apps.lore_f.domoticcontroller.asynctask.TCPCommListener;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.DeviceData;
import com.apps.lore_f.domoticcontroller.generic.classes.MessageStructure;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceSelectionActivity extends AppCompatActivity {

    private final static String TAG = "DeviceSelectionActivity";

    public RecyclerView devicesRecyclerView;
    public LinearLayoutManager linearLayoutManager;
    public FirebaseRecyclerAdapter<DeviceData, DevicesHolder> firebaseAdapter;

    private Query onlineDevices;
    private String groupName;
    private String selectedTCPHost;
    private int selectedTCPPort;
    private Intent selectedIntent;

    private boolean serviceBound=false;

    private TCPComm tcpComm;
    private boolean isTCPCommActive = false;
    private String replyCode;

    private TCPCommListener tcpCommListener = new TCPCommListener() {
        @Override
        public void onSocketCreated() {
            isTCPCommActive = true;
            tcpComm.sendData(composeRequest());
        }

        @Override
        public void onSocketClosed() {
            isTCPCommActive = false;
        }

        @Override
        public void onDataReceived(String data) {
            Log.i(TAG, "data received from tcp comm interface:" + data);

            try {
                JSONObject reply = new JSONObject(data);
                switch (reply.getString("header")){

                    case "__handshake_reply":

                        if (reply.getString("body").equals(replyCode)){

                            manageIntent();

                        }
                        break;
                }


            } catch (JSONException e) {
                Log.d(TAG, "NOT A JSON REPLY!" + data);
            }

        }

    };

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

                case R.id.BTN___DEVICE_SELECTION___MOTIONEVENTSMANAGEMENT:

                    // avvia l'activity per la gestione della videosorveglianza
                    intent = new Intent(getApplicationContext(), MotionEventsManagementActivity.class);
                    startActivity(intent);

                    break;

            }

        }

    };

    public static class DevicesHolder extends RecyclerView.ViewHolder {

        public TextView deviceNameTxv;

        public ImageButton buttonShowAdditionalData;
        public ImageButton buttonGoToTorrent;
        public ImageButton buttonGoToDeviceInfo;
        public ImageButton buttonGoToWakeOnLAN;
        public ImageButton buttonGoToCamera;
        public ImageButton buttonGoToThermostat;

        public TextView deviceRunningSinceTxv;
        public TextView deviceLastUpdateTxv;

        public boolean showAdditionalData = false;

        public DevicesHolder(View v) {

            super(v);
            deviceNameTxv = itemView.findViewById(R.id.TXV___ROWDEVICE___DEVICENAME);

            buttonShowAdditionalData = itemView.findViewById(R.id.BTN___ROWDEVICE___MOREINFO);
            buttonGoToTorrent = itemView.findViewById(R.id.BTN___ROWDEVICE___TORRENT);
            buttonGoToDeviceInfo = itemView.findViewById(R.id.BTN___ROWDEVICE___DEVICEINFO);
            buttonGoToWakeOnLAN = itemView.findViewById(R.id.BTN___ROWDEVICE___WAKEONLAN);
            buttonGoToCamera = itemView.findViewById(R.id.BTN___ROWDEVICE___CAMERA);
            buttonGoToThermostat = itemView.findViewById(R.id.BTN___ROWDEVICE___THERMOSTAT);

            buttonShowAdditionalData.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    setShowAdditionalData(!DevicesHolder.this.showAdditionalData);

                }

            });

        }

        public void setShowAdditionalData(boolean value) {
            this.showAdditionalData = value;

        }

        public boolean getShowAdditionalData() {
            return this.showAdditionalData;
        }

    }

    private ValueEventListener valueEventListener = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            // aggiorna il contenuto della label

            TextView availableDevicesTextView = findViewById(R.id.TXV___DEVICE_SELECTION___LABEL);

            long availableDevicesNumber = dataSnapshot.getChildrenCount();
            String message;

            if (availableDevicesNumber > 0) {
                message = String.format(getString(R.string.DEVICESELECTION_LABEL_SELECT_DEVICE), availableDevicesNumber);
            } else {
                message = getString(R.string.DEVICESELECTION_LABEL_NO_DEVICE);
            }

            availableDevicesTextView.setText(message);

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

        // recupera gli extra dalle preferenze, se il nome del gruppo non è stato specificato, l'Activity non può proseguire e viene riaperta l'Activity per la selezione del gruppo

        // recupera il nome del gruppo [R.string.data_group_name] dalle shared preferences
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.data_file_key), Context.MODE_PRIVATE);

        groupName = sharedPref.getString(getString(R.string.data_group_name), null);

        if (groupName == null) {

            // questa parte di codice non dovrebbe essere mai eseguita, viene tenuta per evitare eccezioni

            // nome del gruppo non impostato, lancia l'Activity GroupSelection per selezionare il gruppo a cui connettersi
            startActivity(new Intent(this, GroupSelection.class));

            // termina l'Activity corrente
            finish();
            return;

        }

        Toolbar myToolbar = findViewById(R.id.TBR___DEVICE_SELECTION___TOOLBAR);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle(R.string.DEVICE_SELECTION_ACTIONBAR_TITLE);

        // handler
        devicesRecyclerView = findViewById(R.id.RWV___DEVICE_SELECTION___DEVICES);

        // assegno OnClickListener
        findViewById(R.id.BTN___DEVICE_SELECTION___CLOUDSTORAGE).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___DEVICE_SELECTION___MOTIONEVENTSMANAGEMENT).setOnClickListener(onClickListener);

        // cerca i dispositivi online nel database
        DatabaseReference userNode = FirebaseDatabase.getInstance().getReference("Devices");

        onlineDevices = userNode.child(groupName).orderByChild("Online").equalTo(true);
        onlineDevices.addValueEventListener(valueEventListener);

    }

    @Override
    protected void onResume() {

        super.onResume();
        refreshAdapter();

    }

    @Override
    protected void onPause() {

        super.onPause();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        // rimuove OnClickListener
        findViewById(R.id.BTN___DEVICE_SELECTION___CLOUDSTORAGE).setOnClickListener(null);
        findViewById(R.id.BTN___DEVICE_SELECTION___MOTIONEVENTSMANAGEMENT).setOnClickListener(null);

        onlineDevices.removeEventListener(valueEventListener);

        if (isTCPCommActive) {
            tcpComm.terminate();
            tcpComm.removeListener();
        }

    }

    private void refreshAdapter() {

        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<DeviceData, DevicesHolder>(
                DeviceData.class,
                R.layout.row_holder_device_element,
                DevicesHolder.class,
                onlineDevices) {

            @Override
            protected void populateViewHolder(DevicesHolder holder, final DeviceData deviceData, int position) {

                holder.deviceNameTxv.setText(deviceData.getDeviceName());

                // gestisce la visualizzazione delle immagini in funzione della capability del dispositivo
                //
                if (deviceData.hasTransmission()) {
                    //
                    holder.buttonGoToTorrent.setVisibility(View.VISIBLE);
                    holder.buttonGoToTorrent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            selectedIntent = new Intent(DeviceSelectionActivity.this, TransmissionRemoteActivity.class);

                            selectedIntent.putExtra("__host", deviceData.getTCPServiceHost());
                            selectedIntent.putExtra("__port", deviceData.getTCPServicePort());

                            selectedTCPHost = deviceData.getTCPServiceHost();
                            selectedTCPPort = deviceData.getTCPServicePort();

                            manageTCPComm();

                        }

                    });
                }

                if (deviceData.hasWakeOnLAN()) {
                    //
                    holder.buttonGoToWakeOnLAN.setVisibility(View.VISIBLE);
                    holder.buttonGoToWakeOnLAN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            selectedIntent = new Intent(DeviceSelectionActivity.this, WakeOnLANActivity.class);

                            // Bind to LocalService
                        }

                    });

                }

                if (deviceData.hasCamera()) {
                    //

                    holder.buttonGoToCamera.setVisibility(View.VISIBLE);
                    holder.buttonGoToCamera.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            selectedIntent = new Intent(DeviceSelectionActivity.this, CameraSelectionActivity.class);
                            selectedIntent.putExtra("_filter_by_owner", true);

                            // Bind to LocalService
                        }

                    });

                }

                if (deviceData.hasThermostat()) {

                    holder.buttonGoToThermostat.setVisibility(View.VISIBLE);
                    holder.buttonGoToThermostat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            selectedIntent = new Intent(DeviceSelectionActivity.this, ThermostatActivity.class);

                            // Bind to LocalService
                        }

                    });

                }



                holder.buttonGoToDeviceInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        selectedIntent = new Intent(DeviceSelectionActivity.this, DeviceInfoViewActivity.class);
                        selectedIntent.putExtra("__host", deviceData.getTCPServiceHost());
                        selectedIntent.putExtra("__port", deviceData.getTCPServicePort());

                        selectedTCPHost = deviceData.getTCPServiceHost();
                        selectedTCPPort = deviceData.getTCPServicePort();

                        manageTCPComm();

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

    private void manageTCPComm() {

        if (!isTCPCommActive) {
            tcpComm = new TCPComm(selectedTCPHost, selectedTCPPort, tcpCommListener);
        } else {
            tcpComm.sendData(composeRequest());
        }

    }

    private String composeRequest(){

        replyCode = ""+System.currentTimeMillis();
        return new MessageStructure("__handshake",replyCode,"").getMessageAsJSONString();

    }

    private void manageIntent(){

        startActivity(selectedIntent);
        finish();

    }


}
