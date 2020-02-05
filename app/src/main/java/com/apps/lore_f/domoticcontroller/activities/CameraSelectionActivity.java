package com.apps.lore_f.domoticcontroller.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.CameraData;
import com.apps.lore_f.domoticcontroller.services.FirebaseDBComm;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CameraSelectionActivity extends AppCompatActivity {

    private final static long MAX_SHOTVIEW_DOWNLOAD_SIZE = 4194304;

    public RecyclerView devicesRecyclerView;
    public LinearLayoutManager linearLayoutManager;

    public FirebaseRecyclerAdapter<CameraData, DevicesHolder> firebaseAdapter;
    public Query availableDevices;

    private FirebaseDBComm firebaseDBComm;

    private String groupName;
    private String remoteDeviceName;

    private boolean filterByOwner=false;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_selection);

        // handler
        devicesRecyclerView = findViewById(R.id.RVW___CAMERA_SELECTION___CAMERALIST);

        // Bind to LocalService
        Intent intent = new Intent(this, FirebaseDBComm.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        if(getIntent().hasExtra("_filter_by_owner")){
            filterByOwner=getIntent().getBooleanExtra("_filter_by_owner", false);
        }

        toolbar = findViewById(R.id.TBR___CAMERA_SELECTION___TOOLBAR);

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

            if(filterByOwner){
                availableDevices = FirebaseDatabase.getInstance().getReference(String.format("Camera/%s", groupName)).orderByChild("Owner").equalTo(remoteDeviceName);
            } else {
                availableDevices = FirebaseDatabase.getInstance().getReference(String.format("Camera/%s", groupName)).orderByChild("Owner");
            }

            toolbar.setTitle(R.string.GENERIC_PLACEHOLDER_WAITING);

            availableDevices.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long count = dataSnapshot.getChildrenCount();
                    String res;

                    if(count==0){
                        res = getString(R.string.CAMERASELECTION_LABEL_NOAVAILABLECAMERA);
                    } else if (count==1) {
                        res = String.format(getString(R.string.CAMERASELECTION_LABEL_AVAILABLECAMERA), count);
                    } else {
                        res = String.format(getString(R.string.CAMERASELECTION_LABEL_AVAILABLECAMERAS), count);
                    }

                    toolbar.setTitle(res);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            refreshAdapter();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }

    };

    public static class DevicesHolder extends RecyclerView.ViewHolder {

        TextView textViewDeviceName;
        TextView textViewDeviceOwner;
        ImageView imageViewCameraPreview;

        ConstraintLayout constraintLayout;

        public DevicesHolder (View v){

            super(v);

            textViewDeviceName = v.findViewById(R.id.TXV___CAMERADEVICE___DEVICENAME);
            textViewDeviceOwner = v.findViewById(R.id.TXV___CAMERADEVICE___DEVICEOWNER);
            imageViewCameraPreview = v.findViewById(R.id.IVW___CAMERADEVICE___PREVIEW);

            constraintLayout = (ConstraintLayout) imageViewCameraPreview.getParent();

        }

    }

    private void refreshAdapter() {

        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<CameraData, DevicesHolder>(
                CameraData.class,
                R.layout.row_holder_camera_element,
                DevicesHolder.class,
                availableDevices) {

            @Override
            protected void populateViewHolder(final DevicesHolder holder, final CameraData deviceData, int position) {

                holder.textViewDeviceName.setText(deviceData.getName());
                holder.textViewDeviceOwner.setText(deviceData.getOwner());

                StorageReference storageRef = FirebaseStorage.getInstance().getReference("Camera/" + groupName + "/" + deviceData.getOwner() + "/" + deviceData.getID()+"/lastshot.jpg");
                storageRef.getBytes(MAX_SHOTVIEW_DOWNLOAD_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] data) {

                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        holder.imageViewCameraPreview.setImageBitmap(bitmap);

                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        holder.imageViewCameraPreview.setImageResource(R.drawable.broken);

                    }

                });

                holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(CameraSelectionActivity.this.getApplicationContext(), CameraViewActivity.class);
                        intent.putExtra("_deviceid", deviceData.getID());
                        startActivity(intent);

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

}
