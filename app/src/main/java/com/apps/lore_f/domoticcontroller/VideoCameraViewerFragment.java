package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.activities.DeviceViewActivity;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.CameraFrame;
import com.apps.lore_f.domoticcontroller.generic.classes.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class VideoCameraViewerFragment extends Fragment {

    private final static String TAG = "VideoCameraViewerFragment";
    private final static long MAX_SHOTVIEW_DOWNLOAD_SIZE = 4194304;

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

    private String cameraFullID;

    public void setCameraFullID(String value) {
        this.cameraFullID = value;
    }

    private String cameraStatus;

    private String cameraStreamPort = "";

    private ImageView shotView;
    private Bitmap shotImage;

    private View fragmentView;

    private boolean fullScreenMode = false;

    private String liveBroadcastStatus = "";
    private String liveBroadcastID = "";

    private boolean liveBroadcastRequested;

    private boolean isNotificationEnabled;

    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.BTN___VSCAMERAVIEW___REQUESTSHOT:
                    requestSingleShot();
                    break;

                case R.id.BTN___VSCAMERAVIEW___REQUESTVIDEOSTREAM:

                    /*
                    se è attivo uno stream, attiva la visualizzazione del componente youtube
                    altrimenti, manda un messaggio all'host remoto per l'inizializzazione di uno stream
                     */

                    if (liveBroadcastStatus.equals("ready") && !liveBroadcastID.equals("")) {

                        liveBroadcastRequested = false;
                        startYouTubeLiveViewActivity();


                    } else if (liveBroadcastStatus.equals("idle")) {

                        liveBroadcastRequested = true;

                        // richiede un nuovo live stream all'host remoto
                        parent.sendCommandToDevice(new Message("__start_streaming_request", cameraID, parent.thisDevice));

                    }

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

                case R.id.BTN___VSCAMERAVIEW___NOTIFICATIONSTATUS:
                    break;


            }

        }

    };

    public VideoCameraViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private ValueEventListener cameraStreamPortEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (dataSnapshot != null) {

                setCameraStreamPort(dataSnapshot.getValue().toString());

            } else {

                setCameraStreamPort("");

            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };

    private ValueEventListener lastShotEventListener = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (dataSnapshot != null) {

                CameraFrame shotData = dataSnapshot.getValue(CameraFrame.class);

                if (shotData != null) {

                    StorageReference storageRef = FirebaseStorage.getInstance().getReference("Videocameras/" + parent.groupName + "/" + cameraFullID + "/shotview.jpg");
                    storageRef.getBytes(MAX_SHOTVIEW_DOWNLOAD_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            refreshFrame(bytes);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            shotView.setImageResource(R.drawable.broken);

                            Log.d(TAG, "Videocameras/" + parent.groupName + "/" + cameraFullID + "/shotview.jpg");

                        }
                    });

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

            if (dataSnapshot != null) {
                cameraStatus = dataSnapshot.getValue().toString();
                updateCameraStatus();
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener liveBroadcastStatusListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot != null) {
                liveBroadcastStatus = dataSnapshot.getValue().toString();
                manageLiveBroadcastStatus();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };

    private ValueEventListener liveBroadcastAddressListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot != null) {
                liveBroadcastID = dataSnapshot.getValue().toString();
                manageLiveBroadcastStatus();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_videosurveillance_cameraviewer, container, false);

        TextView cameraNameTXVview = (TextView) view.findViewById(R.id.TXV___VSCAMERAVIEW___CAMERANAME);
        cameraNameTXVview.setText(cameraName);

        shotView = (ImageView) view.findViewById(R.id.IVW___VSCAMERAVIEW___SHOTVIEW);
        shotView.setOnClickListener(onClickListener);

        view.findViewById(R.id.BTN___VSCAMERAVIEW___REQUESTSHOT).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___VSCAMERAVIEW___REQUESTVIDEOSTREAM).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___VSCAMERAVIEW___SWITCHSTATUS).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___VSCAMERAVIEW___REQUESTMOTIONEVENT).setOnClickListener(onClickListener);

        view.findViewById(R.id.BTN___VSCAMERAVIEW___REQUESTVIDEOSTREAM).setEnabled(false);

        /* define the database nodes, and attach the value listeners*/

        DatabaseReference shotNode;
        DatabaseReference statusNode;
        DatabaseReference cameraStreamPortNode;
        DatabaseReference youTubeLiveBroadcastStatusNode;
        DatabaseReference youTubeLiveBroadcastAddressNode;

        String videoCamerasRootNode = String.format("VideoCameras/%s/%s-%s/", parent.groupName, parent.remoteDeviceName, cameraID);

        shotNode = FirebaseDatabase.getInstance().getReference(videoCamerasRootNode + "LastShotData");
        statusNode = FirebaseDatabase.getInstance().getReference(videoCamerasRootNode + "MoDetStatus");
        cameraStreamPortNode = FirebaseDatabase.getInstance().getReference(videoCamerasRootNode + "StreamPort");

        youTubeLiveBroadcastStatusNode = FirebaseDatabase.getInstance().getReference(videoCamerasRootNode + "LiveStreamingBroadcastStatus");
        youTubeLiveBroadcastAddressNode = FirebaseDatabase.getInstance().getReference(videoCamerasRootNode + "LiveStreamingBroadcastData");

        statusNode.addValueEventListener(deviceStatusEventListener);

        shotNode.addValueEventListener(lastShotEventListener);

        cameraStreamPortNode.addValueEventListener(cameraStreamPortEventListener);

        youTubeLiveBroadcastStatusNode.addValueEventListener(liveBroadcastStatusListener);
        youTubeLiveBroadcastAddressNode.addValueEventListener(liveBroadcastAddressListener);

        /* store the view object in a global variable */
        fragmentView = view;

        manageFullScreenMode();

        liveBroadcastRequested = false;

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
        DatabaseReference cameraStreamPortNode;
        DatabaseReference youTubeLiveBroadcastStatusNode;
        DatabaseReference youTubeLiveBroadcastAddressNode;

        shotNode = FirebaseDatabase.getInstance().getReference(String.format("Groups/%s/VideoSurveillance/AvailableCameras/%s-%s/LastShotData", parent.groupName, parent.remoteDeviceName, cameraID));
        statusNode = FirebaseDatabase.getInstance().getReference(String.format("Groups/%s/VideoSurveillance/AvailableCameras/%s-%s/MoDetStatus", parent.groupName, parent.remoteDeviceName, cameraID));
        cameraStreamPortNode = FirebaseDatabase.getInstance().getReference(DefaultValues.GROUPNODE + "/" + parent.groupName + "/" + DefaultValues.VIDEOSURVEILLANCENODE + "/" + DefaultValues.AVAILABLECAMSNODE + "/" + parent.remoteDeviceName + "-" + cameraID + "/" + "StreamPort");

        youTubeLiveBroadcastStatusNode = FirebaseDatabase.getInstance().getReference(String.format("Groups/%s/VideoSurveillance/AvailableCameras/%s-%s/LiveStreamingBroadcastStatus", parent.groupName, parent.remoteDeviceName, cameraID));
        youTubeLiveBroadcastAddressNode = FirebaseDatabase.getInstance().getReference(String.format("Groups/%s/VideoSurveillance/AvailableCameras/%s-%s/LiveStreamingBroadcastData", parent.groupName, parent.remoteDeviceName, cameraID));

        statusNode.removeEventListener(deviceStatusEventListener);
        shotNode.removeEventListener(lastShotEventListener);
        cameraStreamPortNode.removeEventListener(cameraStreamPortEventListener);


        youTubeLiveBroadcastStatusNode.removeEventListener(liveBroadcastStatusListener);
        youTubeLiveBroadcastAddressNode.removeEventListener(liveBroadcastAddressListener);

    }

    public void requestSingleShot() {

        parent.sendCommandToDevice(
                new Message("__request_shot", cameraID, parent.thisDevice)
        );

    }

    public void requestShotSeries() {
        parent.sendCommandToDevice(
                new Message("__request_shots", cameraID, parent.thisDevice)
        );
    }

    private void manageFullScreenMode() {

        if (fullScreenMode) {

            fragmentView.findViewById(R.id.LLO_VSCAMERAVIEW___BUTTONSTRIP).setVisibility(GONE);
            fragmentView.findViewById(R.id.TXV___VSCAMERAVIEW___CAMERANAME).setVisibility(GONE);

        } else {

            fragmentView.findViewById(R.id.LLO_VSCAMERAVIEW___BUTTONSTRIP).setVisibility(VISIBLE);
            fragmentView.findViewById(R.id.TXV___VSCAMERAVIEW___CAMERANAME).setVisibility(VISIBLE);
        }

    }

    private void updateCameraStatus() {

        if (fragmentView == null)
            return;

        int resourceToShow;

        switch (cameraStatus) {

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

        ImageButton statusSwitch = (ImageButton) fragmentView.findViewById(R.id.BTN___VSCAMERAVIEW___SWITCHSTATUS);
        statusSwitch.setImageResource(resourceToShow);

    }

    private void switchStatus() {

        if (parent == null)
            return;

        String commandHeader;

        switch (cameraStatus) {

            case VideosurveillanceParameters.STATUS_PAUSED:
                commandHeader = "__start_modet";
                break;

            case VideosurveillanceParameters.STATUS_RUNNING:
                commandHeader = "__stop_modet";
                break;

            default:
                commandHeader = "";
                break;
        }

        if (!commandHeader.equals("")) {

            parent.sendCommandToDevice(
                    new Message(
                            commandHeader,
                            this.cameraID,
                            parent.thisDevice
                    )

            );

        }

    }

    private void requestMotionEvent() {

        parent.sendCommandToDevice(
                new Message(
                        "__request_motion_event",
                        this.cameraID,
                        parent.thisDevice
                )

        );


    }

    private void setCameraStreamPort(String value) {
        cameraStreamPort = value;
        manageLiveBroadcastStatus();
    }

    public void manageLiveBroadcastStatus() {

        /*
        gestisce l'immagine da mostrare e l'abilitazione del pulsante "BTN___VSCAMERAVIEW___REQUESTVIDEOSTREAM" in funzione dello stato del live broadcast registrato sul nodo di Firebase
         */

        int drawableToShow = R.drawable.waiting_data;
        boolean liveStreamRequestEnabled = false;

        switch (liveBroadcastStatus) {

            case "idle":
                drawableToShow = R.drawable.live;
                liveStreamRequestEnabled = true;
                break;

            case "creating":
                drawableToShow = R.drawable.live_yellow;
                liveStreamRequestEnabled = true;
                break;

            case "ready":
                drawableToShow = R.drawable.live_green;
                liveStreamRequestEnabled = true;

                if (liveBroadcastRequested)
                    startYouTubeLiveViewActivity();

                break;

            case "not available":
                drawableToShow = R.drawable.block;
                liveStreamRequestEnabled = false;
                break;

            default:
                drawableToShow = R.drawable.waiting_data;
                liveStreamRequestEnabled = false;
                break;

        }

        /*
        se è disponibile la view, imposta l'immagine e l'abilitazione del pulsante
         */

        if (fragmentView != null) {

            ImageButton imageButton = (ImageButton) fragmentView.findViewById(R.id.BTN___VSCAMERAVIEW___REQUESTVIDEOSTREAM);
            imageButton.setImageResource(drawableToShow);
            imageButton.setEnabled(liveStreamRequestEnabled);

        }

    }

    private void startYouTubeLiveViewActivity() {

        Intent intent = new Intent(getContext(), YouTubeLiveViewActivity.class);
        intent.putExtra("__live_broadcast_ID", liveBroadcastID);
        startActivity(intent);

    }

    public void refreshFrame(byte[] data) {

        Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (shotView != null) {

            // adatta le dimensioni dell'immagine a quelle disponibili su schermo
            shotView.setImageBitmap(image);

        }

    }

}
