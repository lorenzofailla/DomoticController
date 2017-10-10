package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ZoneMinderCameraListFragment extends Fragment {

    public ZoneMinderControlFragment parent;
    public boolean viewCreated=false;

    private View fragmentview;

    public DatabaseReference camerasNode;
    private Query availableCameras;

    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<ZMCameraDevice, CamerasHolder> firebaseAdapter;
    private RecyclerView camerasRecyclerView;



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

    public static class CamerasHolder extends RecyclerView.ViewHolder {

        public TextView cameraNameTXV;
        public ImageView cameraConnectBTN;


        public CamerasHolder(View v) {
            super(v);

            cameraNameTXV = (TextView) itemView.findViewById(R.id.TXV___ZMCAMERADEVICE___DEVICENAME);
            cameraConnectBTN = (ImageView) itemView.findViewById(R.id.BTN___ZMCAMERADEVICE___CONNECT);

        }

    }

    public ZoneMinderCameraListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_zoneminder_cameralist, container, false);

        fragmentview=view;

        camerasRecyclerView = (RecyclerView) view.findViewById(R.id.RWV___CAMERALISTFRAGMENT___AVAILABLECAMERAS);
        availableCameras = camerasNode.orderByChild("Available").equalTo(true);
        availableCameras.addValueEventListener(valueEventListener);

        viewCreated=true;
        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

        availableCameras.removeEventListener(valueEventListener);

    }

    private void refreshAdapter(){

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<ZMCameraDevice, CamerasHolder>(
                ZMCameraDevice.class,
                R.layout.row_holder_zmcamera_element,
                CamerasHolder.class,
                availableCameras) {

            @Override
            protected void populateViewHolder(CamerasHolder holder, final ZMCameraDevice camera, int position) {

                holder.cameraNameTXV.setText(camera.getName());
                holder.cameraConnectBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        ZoneMinderCameraViewerFragment zoneMinderCameraViewerFragment = new ZoneMinderCameraViewerFragment();
                        zoneMinderCameraViewerFragment.parent = parent;
                        zoneMinderCameraViewerFragment.zmMonitorId = camera.getId();
                        zoneMinderCameraViewerFragment.zmMonitorName = camera.getName();

                        parent.showFragment(zoneMinderCameraViewerFragment);

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
                    camerasRecyclerView.scrollToPosition(positionStart);
                }

            }

        });

        camerasRecyclerView.setLayoutManager(linearLayoutManager);
        camerasRecyclerView.setAdapter(firebaseAdapter);

    }


}
