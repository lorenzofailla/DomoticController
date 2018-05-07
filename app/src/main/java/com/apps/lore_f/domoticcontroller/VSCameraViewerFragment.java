package com.apps.lore_f.domoticcontroller;

import android.app.ProgressDialog;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;

import static apps.android.loref.GeneralUtilitiesLibrary.decompress;

public class VSCameraViewerFragment extends Fragment {

    private final static String TAG = "VSCameraViewerFragment";

    public boolean viewCreated = false;
    private DeviceViewActivity parent;

    public void setParent(DeviceViewActivity value) {
        this.parent = value;
    }

    private String cameraID;

    public void setCameraID(String value) {
        this.cameraID = value;
    }

    private String cameraName;

    public void setCameraName(String value) {
        this.cameraName = value;
    }

    private String cameraStatus;


    private ImageView shotView;
    private Bitmap shotImage;

    private View fragmentView;

    private boolean fullScreenMode = false;

    private ProgressDialog needToBuffer;



    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.BTN___VSCAMERAVIEW___REQUESTSHOT:
                    requestSingleShot();
                    break;

                case R.id.BTN___VSCAMERAVIEW___REQUESTVIDEOSTREAM:

                    break;

                case R.id.IVW___VSCAMERAVIEW___SHOTVIEW:
                    fullScreenMode = !fullScreenMode;
                    manageFullScreenMode();
                    break;

                case R.id.BTN___VSCAMERAVIEW___SWITCHSTATUS:
                    switchStatus();
                    break;

                case R.id.BTN___VSCAMERAVIEW___REQUESTMOTIONEVENT:
                    requestMotionEvent();
                    break;
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

    private ValueEventListener lastShotEventListener = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (dataSnapshot != null) {

                VSShotPicture shotData = dataSnapshot.getValue(VSShotPicture.class);

                if(shotData!=null) {
                    byte[] shotImageData = new byte[0];
                    try {
                        shotImageData = decompress(Base64.decode(shotData.getImgData(), Base64.DEFAULT));
                        shotImage = BitmapFactory.decodeByteArray(shotImageData, 0, shotImageData.length);

                        // adatta le dimensioni dell'immagine a quelle disponibili su schermo
                        shotView.setImageBitmap(shotImage);

                    } catch (IOException | DataFormatException e) {

                        shotView.setImageResource(R.drawable.broken);

                    }


                } else {

                    shotView.setImageResource(R.drawable.broken);

                }

            } else {

                shotView.setImageResource(R.drawable.broken);

            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };

    private ValueEventListener deviceStatusEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            cameraStatus=dataSnapshot.getValue().toString();
            updateCameraStatus();

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_videosurveillance_cameraviewer, container, false);

        TextView cameraNameTXVview = view.findViewById(R.id.TXV___VSCAMERAVIEW___CAMERANAME);
        cameraNameTXVview.setText(cameraName);

        shotView = view.findViewById(R.id.IVW___VSCAMERAVIEW___SHOTVIEW);
        shotView.setOnClickListener(onClickListener);

        view.findViewById(R.id.BTN___VSCAMERAVIEW___REQUESTSHOT).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___VSCAMERAVIEW___REQUESTVIDEOSTREAM).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___VSCAMERAVIEW___SWITCHSTATUS).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___VSCAMERAVIEW___REQUESTMOTIONEVENT).setOnClickListener(onClickListener);

        view.findViewById(R.id.BTN___VSCAMERAVIEW___REQUESTVIDEOSTREAM).setEnabled(false);

        /* define the database nodes, and attach the value listeners*/

        DatabaseReference shotNode;
        DatabaseReference statusNode;
        DatabaseReference streamingFPSNode;
        DatabaseReference streamingRootNode;
        DatabaseReference streamingDataNode;
        Query streamingDataNodeOrdered;

        shotNode = FirebaseDatabase.getInstance().getReference(String.format("Groups/%s/VideoSurveillance/AvailableCameras/%s-%s/LastShotData", parent.groupName,parent.remoteDeviceName,cameraID));
        statusNode = FirebaseDatabase.getInstance().getReference(String.format("Groups/%s/VideoSurveillance/AvailableCameras/%s-%s/MoDetStatus", parent.groupName,parent.remoteDeviceName,cameraID));

        statusNode.addValueEventListener(deviceStatusEventListener);
        shotNode.addValueEventListener(lastShotEventListener);

        /* store the view object in a global variable */
        fragmentView = view;

        manageFullScreenMode();

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

        parent.sendCommandToDevice(
                new Message(
                        "__stop_cameravideostreaming",
                        this.cameraID,
                        parent.thisDevice
                )

        );

        fragmentView.findViewById(R.id.BTN___VSCAMERAVIEW___REQUESTSHOT).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___VSCAMERAVIEW___REQUESTVIDEOSTREAM).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___VSCAMERAVIEW___SWITCHSTATUS).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___VSCAMERAVIEW___REQUESTMOTIONEVENT).setOnClickListener(null);

        shotView.setOnClickListener(null);

        /* define the database nodes, and remove the value listeners*/

        DatabaseReference shotNode;
        DatabaseReference statusNode;

        shotNode = FirebaseDatabase.getInstance().getReference(String.format("Groups/%s/VideoSurveillance/AvailableCameras/%s-%s/LastShotData", parent.groupName,parent.remoteDeviceName,cameraID));
        statusNode = FirebaseDatabase.getInstance().getReference(String.format("Groups/%s/VideoSurveillance/AvailableCameras/%s-%s/MoDetStatus", parent.groupName,parent.remoteDeviceName,cameraID));

        statusNode.removeEventListener(deviceStatusEventListener);
        shotNode.removeEventListener(lastShotEventListener);

    }

    public void requestSingleShot() {
        parent.sendCommandToDevice(
                new Message("__request_shot",cameraID,parent.thisDevice)
        );

    }

    public void requestShotSeries() {
        parent.sendCommandToDevice(
                new Message("__request_shots",cameraID,parent.thisDevice)
        );
    }

    private void manageFullScreenMode() {

        if (fullScreenMode) {

            fragmentView.findViewById(R.id.LLO_VSCAMERAVIEW___BUTTONSTRIP).setVisibility(View.GONE);
            fragmentView.findViewById(R.id.TXV___VSCAMERAVIEW___CAMERANAME).setVisibility(View.GONE);

        } else {

            fragmentView.findViewById(R.id.LLO_VSCAMERAVIEW___BUTTONSTRIP).setVisibility(View.VISIBLE);
            fragmentView.findViewById(R.id.TXV___VSCAMERAVIEW___CAMERANAME).setVisibility(View.VISIBLE);
        }

    }

    private void updateCameraStatus(){

        if(fragmentView ==null)
            return;

        int resourceToShow;

        switch(cameraStatus){

            case VideosurveillanceParameters.STATUS_PAUSED:
                resourceToShow = R.drawable.run;
                break;

            case VideosurveillanceParameters.STATUS_RUNNING:
                resourceToShow = R.drawable.pause;
                break;

            default:
                resourceToShow = R.drawable.broken;
                break;
        }

        ImageButton statusSwitch = fragmentView.findViewById(R.id.BTN___VSCAMERAVIEW___SWITCHSTATUS);
        statusSwitch.setImageResource(resourceToShow);

    }

    private void switchStatus(){

        if(parent==null)
            return;

        String commandHeader;

        switch(cameraStatus){

            case VideosurveillanceParameters.STATUS_PAUSED:
                commandHeader="__start_modet";
                break;

            case VideosurveillanceParameters.STATUS_RUNNING:
                commandHeader="__stop_modet";
                break;

            default:
                commandHeader="";
                break;
        }

        if(!commandHeader.equals("")){

            parent.sendCommandToDevice(
                    new Message(
                            commandHeader,
                            this.cameraID,
                            parent.thisDevice
                    )

            );

        }

    }

    private void requestMotionEvent(){

        parent.sendCommandToDevice(
                new Message(
                        "__request_motion_event",
                        this.cameraID,
                        parent.thisDevice
                )

        );


    }

}
