package com.apps.lore_f.domoticcontroller.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.TransmissionRemoteTorrentElement;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.WakeOnLANDeviceData;
import com.apps.lore_f.domoticcontroller.generic.classes.MessageStructure;
import com.apps.lore_f.domoticcontroller.services.FirebaseDBComm;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TransmissionRemoteActivity extends AppCompatActivity {

    public RecyclerView torrentsRecyclerView;
    public LinearLayoutManager linearLayoutManager;
    public FirebaseRecyclerAdapter<TransmissionRemoteTorrentElement, TransmissionRemoteTorrentsHolder> firebaseAdapter;
    public DatabaseReference torrentsList;

    private FirebaseDBComm firebaseDBComm;

    private String groupName;
    private String remoteDeviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmission_remote);

    }

    @Override
    protected void onResume() {

        super.onResume();

        // handler
        torrentsRecyclerView = findViewById(R.id.RVW___TRANSMISSIONREMOTE___TORRENTSLIST);

        // Bind to LocalService
        Intent intent = new Intent(this, FirebaseDBComm.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onPause() {

        super.onPause();

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

            torrentsList = FirebaseDatabase.getInstance().getReference(String.format("Devices/%s/%s/StatusData/Transmission/Torrents", groupName, remoteDeviceName));
            firebaseDBComm.sendCommandToDevice(new MessageStructure("__refresh_torrent_data", "null",firebaseDBComm.getThisDeviceName()));

            refreshAdapter();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }

    };

    public static class TransmissionRemoteTorrentsHolder extends RecyclerView.ViewHolder {

        TextView textTorrentName;
        TextView textTorrentStatus;
        TextView textTorrentHave;
        TextView textTorrentUpSpeed;
        TextView textTorrentDownSpeed;

        ImageButton imageButtonSwitchStatus;
        ImageButton imageButtonRemoveTorrent;

        ProgressBar progressBarCompletionPercentage;

        public TransmissionRemoteTorrentsHolder (View v){

            super(v);

            textTorrentName = v.findViewById(R.id.TXV___TORRENTSLISTROW___TORRENTNAME);
            textTorrentStatus = v.findViewById(R.id.TXV___TORRENTSLISTROW___TORRENTSTATUS);
            textTorrentHave = v.findViewById(R.id.TXV___TORRENTSLISTROW___TORRENTHAVE);
            textTorrentUpSpeed = v.findViewById(R.id.TXV___TORRENTSLISTROW___UPSPEED);
            textTorrentDownSpeed = v.findViewById(R.id.TXV___TORRENTSLISTROW___DOWNSPEED);

            imageButtonSwitchStatus = v.findViewById(R.id.BTN___TORRENTSLISTROW___SWITCHSTATUS);
            imageButtonRemoveTorrent = v.findViewById(R.id.BTN___TORRENTSLISTROW___REMOVETORRENT);

            progressBarCompletionPercentage = v.findViewById(R.id.PBR___TORRENTSLISTROW___TORRENTCOMPLETIONPERC);

        }

    }

    private void refreshAdapter() {

        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<TransmissionRemoteTorrentElement, TransmissionRemoteTorrentsHolder>(
                TransmissionRemoteTorrentElement.class,
                R.layout.row_holder_torrent_element,
                TransmissionRemoteTorrentsHolder.class,
                torrentsList) {

            @Override
            protected void populateViewHolder(TransmissionRemoteTorrentsHolder holder, final TransmissionRemoteTorrentElement torrentData, int position) {

                holder.textTorrentName.setText(torrentData.getName());
                holder.textTorrentHave.setText(torrentData.getHave());
                holder.textTorrentStatus.setText(torrentData.getStatus());
                holder.textTorrentDownSpeed.setText(String.format("%.2f ", torrentData.getDown()));
                holder.textTorrentUpSpeed.setText(String.format("%.2f ", torrentData.getUp()));

                if (torrentData.getStatus().equals("Stopped")){

                    holder.imageButtonSwitchStatus.setImageResource(R.drawable.play);

                } else {

                    holder.imageButtonSwitchStatus.setImageResource(R.drawable.pause);

                }

                holder.imageButtonSwitchStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        firebaseDBComm.sendCommandToDevice(new MessageStructure((torrentData.getStatus().equals("Stopped")?"__start_torrent":"__stop_torrent"),torrentData.getId(),firebaseDBComm.getThisDeviceName()));

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
                    torrentsRecyclerView.scrollToPosition(positionStart);
                }

            }

        });

        torrentsRecyclerView.setLayoutManager(linearLayoutManager);
        torrentsRecyclerView.setAdapter(firebaseAdapter);

    }

}
