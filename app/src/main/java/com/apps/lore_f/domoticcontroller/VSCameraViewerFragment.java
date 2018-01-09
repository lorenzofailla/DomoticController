package com.apps.lore_f.domoticcontroller;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class VSCameraViewerFragment extends Fragment {

    public boolean viewCreated=false;
    public VSControlFragment parent;
    public String zmMonitorId;
    public String zmMonitorName;

    private DatabaseReference shotNode;

    private ImageView shotView;
    private Bitmap shotImage;

    private View fragmentview;

    private boolean fullScreenMode=false;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()){

                case R.id.BTN___ZMCAMERAVIEW___REQUESTSHOT:
                    requestSingleShot();
                    break;

                case R.id.BTN___ZMCAMERAVIEW___REQUESTSHOTSERIES:
                    requestShotSeries();
                    break;

                case R.id.IVW___ZMCAMERAVIEW___SHOTVIEW:
                    fullScreenMode=!fullScreenMode;
                    manageFullScreenMode();
            }

        }
    };

    public VSCameraViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            byte[] shotImageData = getByteArray(dataSnapshot);

            if (shotImageData!=null) {
                shotImage = BitmapFactory.decodeByteArray(shotImageData, 0, shotImageData.length);

                // adatta le dimensioni dell'immagine a quelle disponibili su schermo
                shotView.setImageBitmap(shotImage);


            } else {

                //shotView.setImageDrawable(R.drawable.broken);


            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_zoneminder_cameraviewer, container, false);

        TextView cameraNameTXVview =  (TextView) view.findViewById(R.id.TXV___ZMCAMERAVIEW___CAMERANAME);
        cameraNameTXVview.setText(zmMonitorName);

        shotView = (ImageView) view.findViewById(R.id.IVW___ZMCAMERAVIEW___SHOTVIEW);
        shotView.setOnClickListener(onClickListener);

        view.findViewById(R.id.BTN___ZMCAMERAVIEW___REQUESTSHOT).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___ZMCAMERAVIEW___REQUESTSHOTSERIES).setOnClickListener(onClickListener);

        shotNode= FirebaseDatabase.getInstance().getReference("/Users/lorenzofailla/Devices/"+parent.parent.remoteDeviceName+"/ZoneMinder/Monitors/"+zmMonitorId+"/Shots");
        shotNode.addChildEventListener(childEventListener);

        fragmentview=view;

        manageFullScreenMode();

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

        fragmentview.findViewById(R.id.BTN___ZMCAMERAVIEW___REQUESTSHOT).setOnClickListener(null);
        fragmentview.findViewById(R.id.BTN___ZMCAMERAVIEW___REQUESTSHOTSERIES).setOnClickListener(null);

        shotView.setOnClickListener(null);
        shotNode.removeEventListener(childEventListener);

        // elimina i dati
        shotNode.removeValue();
    }

    public void requestSingleShot(){

        parent.parent.sendCommandToDevice(
                new Message(
                        "__request_single_shot",
                        zmMonitorId,
                        parent.parent.thisDevice
                )
        );

    }

    public void requestShotSeries(){

        parent.parent.sendCommandToDevice(
                new Message(
                        "__request_shot_series",
                        zmMonitorId,
                        parent.parent.thisDevice
                )
        );

    }

    private byte[] getByteArray(DataSnapshot dataSn){

        int slots = Integer.parseInt(dataSn.child("slots").getValue().toString());
        int bytesInLastSlot = Integer.parseInt(dataSn.child("bytesinlastslot").getValue().toString());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {

            byte[] bytes = new byte[65536];

            for (int i = 1; i < slots + 1; i++) {

                String data = dataSn.child("slotData").child("" + i).getValue().toString();
                bytes = Base64.decode(data, Base64.DEFAULT);

                if (i == slots) {

                    outputStream.write(bytes,0,bytesInLastSlot);

                } else {

                    outputStream.write(bytes);

                }

            }

            outputStream.flush();
            outputStream.close();

            return outputStream.toByteArray();

        } catch (IOException e) {

        }
        return null;

    }

    private void manageFullScreenMode(){

        if(fullScreenMode){

            fragmentview.findViewById(R.id.LLO_ZMCAMERAVIEW___BUTTONSTRIP).setVisibility(View.GONE);
            fragmentview.findViewById(R.id.TXV___ZMCAMERAVIEW___CAMERANAME).setVisibility(View.GONE);

        } else {

            fragmentview.findViewById(R.id.LLO_ZMCAMERAVIEW___BUTTONSTRIP).setVisibility(View.VISIBLE);
            fragmentview.findViewById(R.id.TXV___ZMCAMERAVIEW___CAMERANAME).setVisibility(View.VISIBLE);
        }

        parent.manageFullScreenMode(fullScreenMode);


    }

}
