package com.apps.lore_f.domoticcontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class DeviceSelectionActivity extends AppCompatActivity {

    private final static String TAG = "DeviceSelectionActivity";

    public RecyclerView devicesRecyclerView;
    public LinearLayoutManager linearLayoutManager;
    public FirebaseRecyclerAdapter<DeviceToConnect, DevicesHolder> firebaseAdapter;

    private Query onlineDevices;

    private String userName;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent intent;
            switch (view.getId()){

                case R.id.BTN___DEVICE_SELECTION___CLOUDSTORAGE:

                    // avvia l'activity per la gestione del cloud storage
                    intent = new Intent (getApplicationContext(), CloudStorageActivity.class);
                    startActivity(intent);

                    break;

                case R.id.BTN___DEVICE_SELECTION___VIDEOSURVEILLANCE:

                    // avvia l'activity per la gestione della videosorveglianza
                    intent = new Intent (getApplicationContext(), VideoSurveillanceActivity.class);
                    intent.putExtra("__USER", )
                    startActivity(intent);

                    break;

            }

        }

    };

    public static class DevicesHolder extends RecyclerView.ViewHolder {

        public TextView deviceNameTxv;
        public ImageButton connectToDeviceBtn;

        public ImageView torrentImg;
        public ImageView directoryNaviImg;
        public ImageView videoSurveillanceImg;
        public ImageView wakeOnLanImg;


        public DevicesHolder(View v) {
            super(v);
            deviceNameTxv = (TextView) itemView.findViewById(R.id.TXV___ROWDEVICE___DEVICENAME);

            connectToDeviceBtn = (ImageButton) itemView.findViewById(R.id.BTN___ROWDEVICE___CONNECT);
            torrentImg = (ImageView)  itemView.findViewById(R.id.IMG___ROWDEVICE___TORRENT);
            directoryNaviImg = (ImageView)  itemView.findViewById(R.id.IMG___ROWDEVICE___DIRNAVI);
            videoSurveillanceImg = (ImageView) itemView.findViewById(R.id.IMG___ROWDEVICE___VIDEOSURVEILLANCE);
            wakeOnLanImg = (ImageView) itemView.findViewById(R.id.IMG___ROWDEVICE___WAKEONLAN);

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

            // aggiorna l'adapter
            refreshAdapter();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_selection);

        devicesRecyclerView = (RecyclerView) findViewById(R.id.RWV___DEVICE_SELECTION___DEVICES);

        /* imposta il nome utente */
        userName=


    }

    @Override
    protected void onResume(){

        super.onResume();

        // assegno OnClickListener
        findViewById(R.id.BTN___DEVICE_SELECTION___CLOUDSTORAGE).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___DEVICE_SELECTION___VIDEOSURVEILLANCE).setOnClickListener(onClickListener);

        // cerca i dispositivi online nel database
        DatabaseReference userNode = FirebaseDatabase.getInstance().getReference(String.format("/Users/%s", userName));

        onlineDevices = userNode.child("Devices").orderByChild("online").equalTo(true);
        onlineDevices.addValueEventListener(valueEventListener);

    }

    @Override
    protected void onPause(){

        super.onPause();

        // rimuove OnClickListener
        findViewById(R.id.BTN___DEVICE_SELECTION___CLOUDSTORAGE).setOnClickListener(null);
        findViewById(R.id.BTN___DEVICE_SELECTION___VIDEOSURVEILLANCE).setOnClickListener(null);

        onlineDevices.removeEventListener(valueEventListener);

    }

    private void refreshAdapter(){

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

                // gestisce la visualizzazione delle immagini in funzione della capability del dispositivo
                //
                if(device.getHasTorrentManagement()){
                    //
                    holder.torrentImg.setVisibility(View.VISIBLE);

                }

                if(device.getHasDirectoryNavigation()){
                    //
                    holder.directoryNaviImg.setVisibility(View.VISIBLE);

                }

                if(device.getHasVideoSurveillanceManagement()){
                    //
                    holder.videoSurveillanceImg.setVisibility(View.VISIBLE);

                }

                if(device.getHasWakeOnLan()){
                    //
                    holder.wakeOnLanImg.setVisibility(View.VISIBLE);

                }

                holder.connectToDeviceBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        connectToDevice(
                                device.getDeviceName(),
                                device.getHasTorrentManagement(),
                                device.getHasDirectoryNavigation(),
                                device.getHasWakeOnLan()
                        );

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

    private void connectToDevice(String deviceName, boolean torrent, boolean dirNavi, boolean wakeOnLan){

        Intent intent = new Intent(this, DeviceViewActivity.class);
        intent.putExtra("__DEVICE_TO_CONNECT", deviceName);
        intent.putExtra("__HAS_TORRENT_MANAGEMENT", torrent);
        intent.putExtra("__HAS_DIRECTORY_NAVIGATION", dirNavi);
        intent.putExtra("__HAS_WAKEONLAN", wakeOnLan);

        startActivity(intent);

    }

}
