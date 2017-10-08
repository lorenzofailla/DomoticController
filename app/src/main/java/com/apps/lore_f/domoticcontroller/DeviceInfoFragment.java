package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.google.android.gms.internal.zzagz.runOnUiThread;

public class DeviceInfoFragment extends Fragment {

    public String upTime;
    public String freeSpace;

    public boolean viewCreated = false;
    private View fragmentView;

    private static final String TAG = "DeviceInfoFragment";

    private RecyclerView logRecyclerView;

    public LinearLayoutManager linearLayoutManager;
    public FirebaseRecyclerAdapter<LogEntry, DeviceLogHolder> firebaseAdapter;

    public DatabaseReference logsNode;

    interface DeviceInfoFragmentListener{

        void onViewCreated();
        void onRebootRemoteDeviceRequest();
        void onShutdownRemoteDeviceRequest();

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch(view.getId()){

                case R.id.BTN___DEVICEINFOFRAGMENT___REBOOT:

                    if(deviceInfoFragmentListener!=null)
                        deviceInfoFragmentListener.onRebootRemoteDeviceRequest();
                    break;

                case R.id.BTN___DEVICEINFOFRAGMENT___SHUTDOWN:
                    if(deviceInfoFragmentListener!=null)
                        deviceInfoFragmentListener.onShutdownRemoteDeviceRequest();
                    break;

                case R.id.BTN___DEVICEINFOFRAGMENT___CLEARLOG:
                    if(logsNode!=null)
                        logsNode.removeValue();

                    break;

            }

        }
    };

    public static class DeviceLogHolder extends RecyclerView.ViewHolder {

        public TextView dateTimeTXV;
        public TextView logTypeTXV;
        public TextView logDescTXV;

        public DeviceLogHolder(View v) {
            super(v);

            dateTimeTXV = (TextView) itemView.findViewById(R.id.TXV___LOG___DATETIME);
            logTypeTXV = (TextView) itemView.findViewById(R.id.TXV___LOG___LOGTYPE);
            logDescTXV = (TextView) itemView.findViewById(R.id.TXV___LOG___LOGDESC);

        }

    }

    private DeviceInfoFragmentListener deviceInfoFragmentListener;

    public void setDeviceInfoFragmentListener(DeviceInfoFragmentListener listener){
        deviceInfoFragmentListener=listener;
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
            if(viewCreated)
                refreshAdapter();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };


    public DeviceInfoFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_device_info, container, false);

        // inizializza l'handler alla view, in questo modo i componenti possono essere ritrovati
        fragmentView=view;

        logRecyclerView = (RecyclerView) view.findViewById(R.id.RWV___DEVICEINFOFRAGMENT___LOG);

        // assegna un OnClickListener ai pulsanti
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___CLEARLOG).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___REBOOT).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___SHUTDOWN).setOnClickListener(onClickListener);

        // aggiorna il flag e effettua il trigger del metodo nel listener
        viewCreated = true;
        if(deviceInfoFragmentListener!=null) deviceInfoFragmentListener.onViewCreated();

        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(logsNode!=null)
            logsNode.addValueEventListener(valueEventListener);

    }

    @Override
    public void onDetach() {
        super.onDetach();

        if(logsNode!=null)
            logsNode.removeEventListener(valueEventListener);

        // rimuove l'OnClickListener ai pulsanti
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___CLEARLOG).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___REBOOT).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___SHUTDOWN).setOnClickListener(null);

    }

    public void updateView(){

        TextView upTimeReplyTXV = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEVIEW___UPTIME_VALUE);
        upTimeReplyTXV.setText(upTime);

        TextView freeSpaceTXV = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEVIEW___FREEDISKSPACE_VALUE);
        freeSpaceTXV.setText(freeSpace);

    }

    private void refreshAdapter(){

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<LogEntry, DeviceLogHolder>(
                LogEntry.class,
                R.layout.row_holder_log_element,
                DeviceLogHolder.class,
                logsNode) {

            @Override
            protected void populateViewHolder(DeviceLogHolder holder, final LogEntry log, int position) {

                holder.dateTimeTXV.setText(log.getDatetime());
                holder.logTypeTXV.setText(log.getLogtype());
                holder.logDescTXV.setText(log.getLogdesc());

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
                    logRecyclerView.scrollToPosition(positionStart);
                }

            }

        });

        logRecyclerView.setLayoutManager(linearLayoutManager);
        logRecyclerView.setAdapter(firebaseAdapter);

    }

}
