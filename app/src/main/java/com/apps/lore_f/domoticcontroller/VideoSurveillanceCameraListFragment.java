package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.activities.DeviceViewActivity;
import com.apps.lore_f.domoticcontroller.firebase.dataobjects.VSCameraDevice;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.io.IOException;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import static android.content.ContentValues.TAG;
import static apps.android.loref.GeneralUtilitiesLibrary.decompress;

public class VideoSurveillanceCameraListFragment extends Fragment {

    public boolean viewCreated = false;

    public DatabaseReference camerasNode = null;
    public String deviceName;
    private Query availableCameras;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<VSCameraDevice, CamerasHolder> firebaseAdapter;
    private RecyclerView camerasRecyclerView;

    public static class CamerasHolder extends RecyclerView.ViewHolder {

        public TextView cameraNameTXV;
        public TextView cameraOwnerTXV;
        public ImageView cameraStatusIVW;
        public ImageView imagePreviewIWV;

        public CamerasHolder(View v) {
            super(v);

            cameraNameTXV = (TextView) itemView.findViewById(R.id.TXV___VSCAMERADEVICE___DEVICENAME);
            cameraStatusIVW = (ImageView) itemView.findViewById(R.id.IVW___VSCAMERADEVICE___STATUS);
            imagePreviewIWV = (ImageView) itemView.findViewById(R.id.IVW___VSCAMERADEVICE___PREVIEW);
            cameraOwnerTXV = (TextView) itemView.findViewById(R.id.TXV___VSCAMERADEVICE___DEVICEOWNER);

        }

    }

    public VideoSurveillanceCameraListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_videosurveillance_cameralist, container, false);
        camerasRecyclerView = (RecyclerView) view.findViewById(R.id.RWV___CAMERALISTFRAGMENT___AVAILABLECAMERAS);

        if (deviceName != null) {

            availableCameras = camerasNode.orderByChild("OwnerDevice").equalTo(deviceName);

        } else {

            availableCameras = camerasNode.orderByChild("OwnerDevice");
        }

        refreshAdapter();

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

    }

    private void refreshAdapter() {

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<VSCameraDevice, CamerasHolder>(
                VSCameraDevice.class,
                R.layout.row_holder_vscamera_element,
                CamerasHolder.class,
                availableCameras) {

            @Override
            protected void populateViewHolder(CamerasHolder holder, final VSCameraDevice camera, int position) {

                holder.cameraNameTXV.setText(camera.getCameraName());
                holder.cameraOwnerTXV.setText(camera.getOwnerDevice());

                holder.imagePreviewIWV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(getContext(), DeviceViewActivity.class);
                        intent.putExtra("__DEVICE_TO_CONNECT", camera.getOwnerDevice());
                        intent.putExtra("__HAS_TORRENT_MANAGEMENT", false);
                        intent.putExtra("__HAS_DIRECTORY_NAVIGATION", false);
                        intent.putExtra("__HAS_WAKEONLAN", false);
                        intent.putExtra("__HAS_VIDEOSURVEILLANCE", true);
                        intent.putExtra("__CAMERA_NAMES", camera.getCameraName());
                        intent.putExtra("__CAMERA_IDS", camera.getThreadID());
                        intent.putExtra("__ACTION", "monitor");

                        startActivity(intent);

                    }

                });

                /*
                Aggiorna l'immagine imagePreviewIWV con i dati dell'ultimo frame acquisito
                 */

                HashMap<String,Object> lastShotData = camera.getLastShotData();
                if(lastShotData!=null) {
                    String imgRawData=lastShotData.get("ImgData").toString();

                    if (imgRawData != null){

                        try {

                            // recupera i dati dell'immagine
                            byte[] imageData = decompress(Base64.decode(imgRawData, Base64.DEFAULT));
                            Bitmap shotImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

                            // adatta le dimensioni dell'immagine a quelle disponibili su schermo
                            holder.imagePreviewIWV.setImageBitmap(shotImage);

                        } catch (IOException | DataFormatException e){

                            Log.d(TAG, e.getMessage());
                            holder.imagePreviewIWV.setImageResource(R.drawable.broken);

                        }

                    } else {

                        holder.imagePreviewIWV.setImageResource(R.drawable.image);

                    }

                } else {

                    holder.imagePreviewIWV.setImageResource(R.drawable.image);

                }

                /*
                Aggoprma l'immagine
                 */
                String cameraStatus=camera.getMoDetStatus();
                int resId;
                switch (cameraStatus){
                    case VideosurveillanceParameters.STATUS_PAUSED:
                        resId=R.drawable.pause;
                        break;

                    case VideosurveillanceParameters.STATUS_RUNNING:
                        resId=R.drawable.run;
                        break;

                    default:
                        resId=R.drawable.broken;
                        break;

                }

                holder.cameraStatusIVW.setImageResource(resId);

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
