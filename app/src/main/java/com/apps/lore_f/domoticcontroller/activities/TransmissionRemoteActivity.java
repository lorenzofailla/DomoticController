package com.apps.lore_f.domoticcontroller.activities;

import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.TransmissionRemoteTorrentElement;
import com.apps.lore_f.domoticcontroller.generic.classes.MessageStructure;
import com.apps.lore_f.domoticcontroller.services.FirebaseDBComm;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TransmissionRemoteActivity extends TCPCommActivity {

    public RecyclerView torrentsRecyclerView;
    public LinearLayoutManager linearLayoutManager;
    TorrentsListAdapter torrentsListAdapter;

    private Toolbar toolbar;
    private Handler handler;

    private JSONArray torrents;

    private final static long UPDATE_INTERVAL = 1000L;
    private final static String TAG="TransmissionRemoteActivity";

    private Runnable requireTorrentData = new Runnable() {

        @Override
        public void run() {

            tcpComm.sendData(new MessageStructure("__update_torrent_data", "-","-").getMessageAsJSONString());
            handler.postDelayed(this, UPDATE_INTERVAL);

        }

    };

    public class TorrentsListAdapter extends RecyclerView.Adapter<TransmissionRemoteTorrentsHolder> {

        private JSONArray torrentsJSONArray;

        // Provide a suitable constructor (depends on the kind of dataset)
        public TorrentsListAdapter(JSONArray data) {
            this.torrentsJSONArray = data;
        }

        @NonNull
        @Override
        public TransmissionRemoteTorrentsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_holder_torrent_element, parent, false);
            TransmissionRemoteTorrentsHolder transmissionRemoteTorrentsHolder = new TransmissionRemoteTorrentsHolder(v);
            return transmissionRemoteTorrentsHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull TransmissionRemoteTorrentsHolder holder, int position) {


        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return torrentsJSONArray.length();
        }

    }

    @Override
    public void onSocketCreated() {

        super.onSocketCreated();
        handler.post(requireTorrentData);

    }

    @Override
    public void onSocketClosed() {
        super.onSocketClosed();
    }

    @Override
    public void onDataReceived(String data) {
        super.onDataReceived(data);

        Log.i(TAG, "data received from tcp comm interface:" + data);

        try {

            JSONObject jsonData = new JSONObject(data);

            if(jsonData.getString("header").equals("__torrent_general_status")){
                if(jsonData.getString("body").equals("null")){

                    torrents = null;

                } else {

                    torrents = jsonData.getJSONArray("body");

                    torrentsListAdapter = new TorrentsListAdapter(torrents);
                    torrentsRecyclerView.setAdapter(torrentsListAdapter);

                }

            }

        } catch (JSONException e) {

            Log.d(TAG, "NOT A JSON REPLY!" + data);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transmission_remote);

        torrentsRecyclerView = findViewById(R.id.RVW___TRANSMISSIONREMOTE___TORRENTSLIST);
        toolbar = findViewById(R.id.TBR___TRANSMISSIONREMOTE___TOOLBAR);

        handler = new Handler();

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        torrentsRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        linearLayoutManager = new LinearLayoutManager(this);
        torrentsRecyclerView.setLayoutManager(linearLayoutManager);




    }

    @Override
    protected void onDestroy(){

        super.onDestroy();
        handler.removeCallbacks(requireTorrentData);

    }

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

                        if(tcpCommAvailable)
                            tcpComm.sendData(new MessageStructure("__add_torrent", torrentURL.getText().toString(), "-").getMessageAsJSONString());

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

                        tcpComm.sendData(new MessageStructure((delete?"__delete_torrent":"__remove_torrent"), id, "-").getMessageAsJSONString());

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
