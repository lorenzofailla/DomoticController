package com.apps.lore_f.domoticcontroller;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()){

                case R.id.BTN___DEVICE_SELECTION___CLOUDSTORAGE:

                    // avvia l'activity per la gestione del cloud storage
                    Intent intent = new Intent (getApplicationContext(), CloudStorageActivity.class);
                    startActivity(intent);

            }

        }

    };

    public static class DevicesHolder extends RecyclerView.ViewHolder {

        public TextView deviceNameTXV;
        public ImageButton connectToDeviceBTN;


        public DevicesHolder(View v) {
            super(v);
            deviceNameTXV = (TextView) itemView.findViewById(R.id.TXV___ROWDEVICE___DEVICENAME);

            connectToDeviceBTN = (ImageButton) itemView.findViewById(R.id.BTN___ROWDEVICE___CONNECT);

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

    }

    @Override
    protected void onResume(){

        super.onResume();

        // assegno OnClickListener
        findViewById(R.id.BTN___DEVICE_SELECTION___CLOUDSTORAGE).setOnClickListener(onClickListener);

        // cerca i dispositivi online nel database
        DatabaseReference userNode = FirebaseDatabase.getInstance().getReference("/Users/lorenzofailla");

        onlineDevices = userNode.child("Devices").orderByChild("online").equalTo(true);
        onlineDevices.addValueEventListener(valueEventListener);

    }

    @Override
    protected void onPause(){

        super.onPause();

        // rimuovo OnClickListener
        findViewById(R.id.BTN___DEVICE_SELECTION___CLOUDSTORAGE).setOnClickListener(null);

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

                holder.deviceNameTXV.setText(device.getDeviceName());

                holder.connectToDeviceBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        connectToDevice(device.getDeviceName());

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

    private void connectToDevice(String deviceName){

        Intent intent = new Intent(this, DeviceViewActivity.class);
        intent.putExtra("__DEVICE_TO_CONNECT", deviceName);

        startActivity(intent);

    }

}
