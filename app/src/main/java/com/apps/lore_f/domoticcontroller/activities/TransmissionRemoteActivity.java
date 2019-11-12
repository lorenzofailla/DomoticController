package com.apps.lore_f.domoticcontroller.activities;

import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.TransmissionRemoteTorrentElement;
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

    private ImageButton imageButtonRefreshData;
    private ImageButton imageButtonAddTorrent;

    private String groupName;
    private String remoteDeviceName;

    private View.OnClickListener onClickListenerMainButtons = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.BTN___TRANSMISSIONREMOTE___REFRESH:
                    if (firebaseDBComm != null) {
                        firebaseDBComm.sendCommandToDevice(new MessageStructure("__refresh_torrent_data", "null", firebaseDBComm.getThisDeviceName()));
                    }
                    break;

                case R.id.BTN___TRANSMISSIONREMOTE___ADDTORRENT:
                    if (firebaseDBComm != null) {
                        torrentAddRequest();
                    }
                    break;

            }

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmission_remote);

        // handler
        torrentsRecyclerView = findViewById(R.id.RVW___TRANSMISSIONREMOTE___TORRENTSLIST);
        imageButtonRefreshData = findViewById(R.id.BTN___TRANSMISSIONREMOTE___REFRESH);
        imageButtonAddTorrent = findViewById(R.id.BTN___TRANSMISSIONREMOTE___ADDTORRENT);

        // OnClickListeners
        imageButtonRefreshData.setOnClickListener(onClickListenerMainButtons);
        imageButtonAddTorrent.setOnClickListener(onClickListenerMainButtons);

        // Bind to LocalService
        Intent intent = new Intent(this, FirebaseDBComm.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {

        super.onResume();

    }

    @Override
    protected void onPause() {

        super.onPause();

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
        TextView textTorrentETA;


        ImageButton imageButtonSwitchStatus;
        ImageButton imageButtonRemoveTorrent;
        ImageButton imageButtonDeleteTorrent;


        ProgressBar progressBarCompletionPercentage;

        public TransmissionRemoteTorrentsHolder (View v){

            super(v);

            textTorrentName = v.findViewById(R.id.TXV___TORRENTSLISTROW___TORRENTNAME);
            textTorrentStatus = v.findViewById(R.id.TXV___TORRENTSLISTROW___TORRENTSTATUS);
            textTorrentHave = v.findViewById(R.id.TXV___TORRENTSLISTROW___TORRENTHAVE);
            textTorrentUpSpeed = v.findViewById(R.id.TXV___TORRENTSLISTROW___UPSPEED);
            textTorrentDownSpeed = v.findViewById(R.id.TXV___TORRENTSLISTROW___DOWNSPEED);
            textTorrentETA = v.findViewById(R.id.TXV___TORRENTSLISTROW___TORRENTETA);

            imageButtonSwitchStatus = v.findViewById(R.id.BTN___TORRENTSLISTROW___SWITCHSTATUS);
            imageButtonRemoveTorrent = v.findViewById(R.id.BTN___TORRENTSLISTROW___REMOVETORRENT);
            imageButtonDeleteTorrent = v.findViewById(R.id.BTN___TORRENTSLISTROW___DELETETORRENT);

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
                holder.textTorrentETA.setText(torrentData.getEta());
                holder.textTorrentDownSpeed.setText(String.format("%.2f kB/s", torrentData.getDown()));
                holder.textTorrentUpSpeed.setText(String.format("%.2f kB/s", torrentData.getUp()));

                try {

                    holder.progressBarCompletionPercentage.setProgress(Integer.parseInt(torrentData.getDone().replace("%", "")));
                    holder.progressBarCompletionPercentage.setIndeterminate(false);

                } catch (NumberFormatException e) {

                    holder.progressBarCompletionPercentage.setIndeterminate(true);

                }


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

                holder.imageButtonRemoveTorrent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        torrentRemoveRequest(torrentData.getId(), false);
                    }
                });

                holder.imageButtonDeleteTorrent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        torrentRemoveRequest(torrentData.getId(), true);
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

    public void torrentAddRequest() {

        final EditText torrentURL = new EditText(this);

        // Gets a handle to the clipboard service.
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboard.getPrimaryClip().getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)){
            torrentURL.setText(clipboard.getPrimaryClip().getItemAt(0).getText());
        }

        new AlertDialog.Builder(this)
                .setMessage(R.string.ALERTDIALOG_MESSAGE_ADD_TORRENT)
                .setView(torrentURL)
                .setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        firebaseDBComm.sendCommandToDevice(new MessageStructure("__add_torrent", torrentURL.getText().toString(), firebaseDBComm.getThisDeviceName()));

                    }

                })
                .setNegativeButton(R.string.ALERTDIALOG_NO, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })

                .setTitle(R.string.ALERTDIALOG_TITLE_ADD_TORRENT)
                .create()
                .show();

    }

    public void torrentRemoveRequest(final String id, final boolean delete) {

        new AlertDialog.Builder(this)
                .setMessage((delete?R.string.ALERTDIALOG_MESSAGE_CONFIRM_TORRENT_REMOVAL_AND_DATA_ERASE:R.string.ALERTDIALOG_MESSAGE_CONFIRM_TORRENT_REMOVAL))
                .setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        firebaseDBComm.sendCommandToDevice(new MessageStructure((delete?"__delete_torrent":"__remove_torrent"), id, firebaseDBComm.getThisDeviceName()));

                    }

                })
                .setNegativeButton(R.string.ALERTDIALOG_NO, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })

                .setTitle(R.string.ALERTDIALOG_TITLE_CONFIRM_TORRENT_REMOVAL)
                .create()
                .show();

    }

}
