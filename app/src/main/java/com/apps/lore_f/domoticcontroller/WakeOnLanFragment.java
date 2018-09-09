package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.firebase.dataobjects.WOLDeviceEntry;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WakeOnLanFragment extends Fragment {

    public boolean viewCreated = false;
    private View fragmentView;

    private static final String TAG = "WakeOnLanFragment";

    private RecyclerView wolDevicesRecyclerView;

    private DatabaseReference wolDevicesNode;

    public LinearLayoutManager linearLayoutManager;
    public FirebaseRecyclerAdapter<WOLDeviceEntry, WOLDevicesHolder> firebaseAdapter;

    public DeviceViewActivity parent;

    public static class WOLDevicesHolder extends RecyclerView.ViewHolder {

        public TextView deviceNameTXV;
        public ImageButton connectBTN;

        public WOLDevicesHolder(View v) {
            super(v);

            deviceNameTXV = (TextView) itemView.findViewById(R.id.TXV___ROWWOLDEVICE___DEVICENAME);
            connectBTN = (ImageButton) itemView.findViewById(R.id.BTN___ROWWOLDEVICE___CONNECT);

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
            if(viewCreated)
                refreshAdapter();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };


    public WakeOnLanFragment() {
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
        View view = inflater.inflate(R.layout.fragment_wol_devices, container, false);

        // inizializza l'handler alla view, in questo modo i componenti possono essere ritrovati
        fragmentView=view;

        wolDevicesRecyclerView = (RecyclerView) view.findViewById(R.id.RWV___WOLDEVICESFRAGMENT___DEVICES);

        wolDevicesNode = FirebaseDatabase.getInstance().getReference("Groups/lorenzofailla/Devices/" + parent.remoteDeviceName + "/WOLDevices");
        wolDevicesNode.addValueEventListener(valueEventListener);

        // aggiorna il flag e effettua il trigger del metodo nel listener
        viewCreated = true;

        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

    @Override
    public void onDetach() {
        super.onDetach();

        if(wolDevicesNode!=null)
            wolDevicesNode.removeEventListener(valueEventListener);

    }


    private void refreshAdapter(){

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<WOLDeviceEntry, WOLDevicesHolder>(
                WOLDeviceEntry.class,
                R.layout.row_holder_wol_device_element,
                WOLDevicesHolder.class,
                wolDevicesNode) {

            @Override
            protected void populateViewHolder(WOLDevicesHolder holder, final WOLDeviceEntry wolDevice, int position) {

                holder.deviceNameTXV.setText(wolDevice.getName());
                holder.connectBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        parent.sendCommandToDevice(new Message("__wakeonlan", wolDevice.getId(),parent.thisDevice));

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
                    wolDevicesRecyclerView.scrollToPosition(positionStart);
                }

            }

        });

        wolDevicesRecyclerView.setLayoutManager(linearLayoutManager);
        wolDevicesRecyclerView.setAdapter(firebaseAdapter);

    }

}
