package com.apps.lore_f.domoticcontroller.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.ThermostatData;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.TransmissionRemoteTorrentElement;
import com.apps.lore_f.domoticcontroller.generic.classes.MessageStructure;
import com.apps.lore_f.domoticcontroller.services.FirebaseDBComm;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ThermostatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseDBComm firebaseDBComm;
    private String groupName;
    private String remoteDeviceName;
    private LinearLayoutManager linearLayoutManager;
    public FirebaseRecyclerAdapter<ThermostatData, ThermostatsHolder> firebaseAdapter;
    private DatabaseReference thermostatsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermostat);

        recyclerView = findViewById(R.id.RVW___THERMOSTAT_SELECTION___THERMOSTATS);

        // Bind to LocalService
        Intent intent = new Intent(this, FirebaseDBComm.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

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

            thermostatsList=FirebaseDatabase.getInstance().getReference(String.format("Devices/%s/%s/StatusData/Thermostat", groupName, remoteDeviceName));
            refreshAdapter();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }

    };

    public static class ThermostatsHolder extends RecyclerView.ViewHolder{

        public TextView textViewThermostatName;
        public TextView textViewRoomTemp;
        public TextView textViewSetTemp;
        public ImageButton imageButtonSwitchPower;
        public ImageButton imageButtonSwitchStatus;
        public ImageView imageViewHeating;
        public ImageView imageViewFreeze;
        public ImageView imageViewMode;




        public ThermostatsHolder(@NonNull View itemView) {
            super(itemView);

            textViewRoomTemp = itemView.findViewById(R.id.TXV___THERMOSTATDEVICE___DEVICEACTUALROOMTEMP);
            textViewSetTemp = itemView.findViewById(R.id.TXV___THERMOSTATDEVICE___DEVICESETPOINTTEMP);
            textViewThermostatName = itemView.findViewById(R.id.TXV___THERMOSTATDEVICE___DEVICENAME);
            imageButtonSwitchPower = itemView.findViewById(R.id.BTN___THERMOSTATDEVICE___ACTIVATIONSTATUS);
            imageButtonSwitchStatus = itemView.findViewById(R.id.BTN___THERMOSTATDEVICE___OPERATIVE_MODE);
            imageViewFreeze = itemView.findViewById(R.id.IVW___THERMOSTATDEVICE___FREEZE);
            imageViewHeating = itemView.findViewById(R.id.IVW___THERMOSTATDEVICE___HEATING);
            imageViewMode = itemView.findViewById(R.id.IVW___THERMOSTATDEVICE___MODE);

        }

    }

    private void refreshAdapter() {

        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<ThermostatData, ThermostatsHolder>(
                ThermostatData.class,
                R.layout.row_holder_thermostat_element,
                ThermostatsHolder.class,
                thermostatsList) {

            @Override
            protected void populateViewHolder(ThermostatsHolder holder, final ThermostatData thermostatData, int position) {

                holder.textViewThermostatName.setText(thermostatData.getName());
                holder.textViewRoomTemp.setText(String.format("%.1f °C", thermostatData.getRoomTemperature()));
                holder.textViewSetTemp.setText(String.format("%.1f °C", thermostatData.getSetTemperature()));

                int heatingResource=thermostatData.isHeating()?R.drawable.flame:R.drawable.flame_faded;
                int freezeResource=thermostatData.isFreezeActive()?R.drawable.freeze:R.drawable.freeze_faded;
                int modeResource=thermostatData.getMode()==0?R.drawable.manual:R.drawable.automatic;

                holder.imageViewFreeze.setImageResource(freezeResource);
                holder.imageViewHeating.setImageResource(heatingResource);
                holder.imageViewMode.setImageResource(modeResource);

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
                    recyclerView.scrollToPosition(positionStart);
                }

            }

        });

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(firebaseAdapter);

    }

}
