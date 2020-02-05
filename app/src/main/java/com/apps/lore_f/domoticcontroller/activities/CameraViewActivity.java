package com.apps.lore_f.domoticcontroller.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.CameraData;
import com.apps.lore_f.domoticcontroller.services.FirebaseDBComm;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import apps.android.loref.GeneralUtilitiesLibrary;

public class CameraViewActivity extends AppCompatActivity {

    private final static long MAX_SHOTVIEW_DOWNLOAD_SIZE = 4194304;


    private FirebaseDBComm firebaseDBComm;
    private Toolbar toolbar;
    private ImageView imageViewCameraFrame;
    private TextView textViewFrameTimestamp;

    private String groupName;
    private String remoteDeviceName;
    private String cameraID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);

        toolbar = findViewById(R.id.TBR___CAMERA_VIEW___TOOLBAR);
        imageViewCameraFrame = findViewById(R.id.IVW___CAMERA_VIEW_PREVIEW);
        textViewFrameTimestamp = findViewById(R.id.TXV___CAMERA_VIEW___SHOTTIME_VALUE);

        if(getIntent().hasExtra("_deviceid")){
            cameraID=getIntent().getStringExtra("_deviceid");
        } else {
            finish();
            return;
        }

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

            toolbar.setTitle(R.string.GENERIC_PLACEHOLDER_WAITING);

            FirebaseDatabase.getInstance().getReference(String.format("/Camera/%s/%s_%s",groupName,remoteDeviceName,cameraID)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    CameraData deviceData = dataSnapshot.getValue(CameraData.class);

                    toolbar.setTitle(deviceData.getName());
                    textViewFrameTimestamp.setText(GeneralUtilitiesLibrary.getTimeElapsed(deviceData.getFrameTimestamp(), getApplicationContext()));

                    StorageReference storageRef = FirebaseStorage.getInstance().getReference("Camera/" + groupName + "/" + deviceData.getOwner() + "/" + deviceData.getID()+"/lastshot.jpg");
                    storageRef.getBytes(MAX_SHOTVIEW_DOWNLOAD_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] data) {

                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            imageViewCameraFrame.setImageBitmap(bitmap);

                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            imageViewCameraFrame.setImageResource(R.drawable.broken);

                        }

                    });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }

    };
}
