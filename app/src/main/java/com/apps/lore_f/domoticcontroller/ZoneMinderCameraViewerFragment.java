package com.apps.lore_f.domoticcontroller;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;

public class ZoneMinderCameraViewerFragment extends Fragment {

    public boolean viewCreated=false;
    public ZoneMinderControlFragment parent;
    public String zmMonitorId;
    public String zmMonitorName;

    private DatabaseReference shotNode;

    private ImageView shotView;
    private Bitmap shotImage;

    private View fragmentview;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()){

                case R.id.BTN___ZMCAMERAVIEW___REQUESTSHOT:
                    requestSingleShot();
                    break;
            }

        }
    };

    public ZoneMinderCameraViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            try {

                byte[] shotImageData = getByteArray(dataSnapshot);

                if (shotImageData!=null) {
                    shotImage = BitmapFactory.decodeByteArray(shotImageData, 0, shotImageData.length);
                    shotView.setImageBitmap(shotImage);

                    shotNode.removeValue();

                } else {
                    // TODO: 10-Oct-17 mostra un'immagine standard
                }

            } catch (NullPointerException e) {

                Log.i(TAG, "Cannot show datasnapshot");

            }

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

        view.findViewById(R.id.BTN___ZMCAMERAVIEW___REQUESTSHOT).setOnClickListener(onClickListener);
        shotNode= FirebaseDatabase.getInstance().getReference("/Users/lorenzofailla/Devices/"+parent.parent.remoteDeviceName+"/ZoneMinder/Monitors/"+zmMonitorId+"/Shot");
        shotNode.addValueEventListener(valueEventListener);

        fragmentview=view;

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
        shotNode.removeEventListener(valueEventListener);
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

}
