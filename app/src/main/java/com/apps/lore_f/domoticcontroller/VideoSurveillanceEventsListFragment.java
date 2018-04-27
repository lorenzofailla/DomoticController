package com.apps.lore_f.domoticcontroller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.DataFormatException;

import static android.content.ContentValues.TAG;
import static apps.android.loref.GeneralUtilitiesLibrary.decompress;

public class VideoSurveillanceEventsListFragment extends Fragment {

    public boolean viewCreated = false;

    private final static int BG_COLOR_SELECTED=Color.argb(64, 0, 0, 127);

    private View fragmentview;

    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<VSEvent, EventsHolder> firebaseAdapter;

    private ProgressDialog progressDialog;

    private RecyclerView eventsRecyclerView;
    private File downloadDirectoryRoot;
    private DatabaseReference eventsNode;
    private String groupName;

    private String[] eventKeys;

    private int selectedPosition = -1;

    public static class EventsHolder extends RecyclerView.ViewHolder {

        public TextView eventDateTextView;
        public TextView eventMonitorNameTextView;
        public ImageButton viewEventButton;
        public ImageButton deleteEventButton;
        public ProgressBar progressBar;
        public TextView eventCameraNameTextView;
        public ImageView eventPreviewImage;

        public RelativeLayout relativeLayout;
        public RelativeLayout eventData;

        public EventsHolder(View v) {
            super(v);

            eventDateTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTDATETIME);
            eventMonitorNameTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTDEVICENAME);
            eventCameraNameTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTCAMERANAME);
            deleteEventButton = v.findViewById(R.id.BTN___VSEVENTROW___DELETEEVENT);
            viewEventButton = (ImageButton) v.findViewById(R.id.BTN___VSEVENTROW___REQUESTEVENT);
            progressBar = v.findViewById(R.id.PBR___VSEVENTROW___DOWNLOADPROGRESS);
            eventPreviewImage = v.findViewById(R.id.IVW___VSEVENTROW___EVENTPREVIEW);

            relativeLayout = v.findViewById(R.id.RLA___VSEVENTROW___EVENT);
            eventData = v.findViewById(R.id.RLA___VSEVENTROW___EVENTDATA);
        }

    }

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            refreshAdapter();

            /*
            definisce l'array delle keys dei record contenuti nel dataSnapshot
             */

            if (dataSnapshot != null) {

                List<String> keysList = new ArrayList<String>();

                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    keysList.add(item.getKey());
                }

                eventKeys = keysList.toArray(new String[0]);

            } else {

                eventKeys = null;

            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }

    };

    public VideoSurveillanceEventsListFragment() {


    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_videosurveillance_eventslist, container, false);

        // recupera i parametri
        Bundle bundle = getArguments();
        this.groupName = bundle.getString(getString(R.string.data_group_name));

        // inizializza il nodo del database di Firebase contenente le informazioni sugli eventi
        eventsNode = FirebaseDatabase.getInstance().getReference(String.format("Groups/%s/VideoSurveillance/Events", groupName));
        eventsNode.addValueEventListener(valueEventListener);

        // inizializza il riferimento alla directory dove i file dei video saranno scaricati
        downloadDirectoryRoot = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Domotic/VideoSurveillance/DownloadedVideos");

        eventsRecyclerView = view.findViewById(R.id.RWV___VSEVENTVIEWERFRAGMENT___EVENTS);

        fragmentview = view;

        viewCreated = true;
        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {

        eventsNode.removeEventListener(valueEventListener);
        super.onDetach();

    }


    private void refreshAdapter() {

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<VSEvent, EventsHolder>(
                VSEvent.class,
                R.layout.row_holder_vsevent_element,
                EventsHolder.class,
                eventsNode) {

            @Override
            protected void populateViewHolder(final EventsHolder holder, final VSEvent event, final int position) {

                if(position==selectedPosition) {

                    holder.relativeLayout.setBackgroundColor(BG_COLOR_SELECTED);
                }

                holder.eventDateTextView.setText(String.format("%s %s", event.getDate(), event.getTime()));
                holder.eventMonitorNameTextView.setText(event.getDevice());
                holder.eventCameraNameTextView.setText(event.getCameraName());

                // crea il File relativo alla posizione di download locale sul dispositivo
                final File videoFile = new File(downloadDirectoryRoot, String.format("%s/%s/%s", event.getDevice(), event.getThreadID(), event.getVideoLink()));
                final String remoteLocation = String.format("Groups/%s/Devices/%s/VideoSurveillance/Events/%s/%s", groupName, event.getDevice(), event.getThreadID(), event.getVideoLink());

                // controlla se il File creato esiste
                if (videoFile.exists()) {
                    //
                    // esiste

                    // imposta l'immagine sul pulsante
                    holder.viewEventButton.setImageResource(R.drawable.connect);

                    // imposta l'OnClickListener per lanciare il video tramite un Intent
                    holder.viewEventButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // lancia il video
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoFile.getAbsolutePath()));
                            intent.setDataAndType(Uri.parse(videoFile.getAbsolutePath()), "video/*");
                            startActivity(intent);

                            selectedPosition=position;

                        }
                    });

                } else {
                    //
                    // non esiste

                    // imposta l'immagine sul pulsante
                    holder.viewEventButton.setImageResource(R.drawable.cloud_download);

                    // imposta l'OnClickListener per scaricare il video in una cartella locale
                    holder.viewEventButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String localLocation = String.format("Domotic/VideoSurveillance/DownloadedVideos/%s/%s", event.getDevice(), event.getThreadID());

                            new DownloadTask(remoteLocation, localLocation, holder.progressBar).execute();

                            selectedPosition=position;

                        }

                    });

                }

                holder.deleteEventButton.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View view) {

                        deleteEvent(videoFile, remoteLocation, eventKeys[position]);

                        if(position==selectedPosition) {
                            selectedPosition = -1;
                        }

                    }

                });

                // definisce l'immagine dell'evento
                String eventImageData = event.getEventPictureData();
                if(eventImageData!=null) {

                        try {

                            // recupera i dati dell'immagine
                            byte[] imageData = decompress(Base64.decode(eventImageData, Base64.DEFAULT));
                            Bitmap shotImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

                            // adatta le dimensioni dell'immagine a quelle disponibili su schermo
                            holder.eventPreviewImage.setImageBitmap(shotImage);

                            holder.eventPreviewImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    ViewGroup.LayoutParams imgLayoutParams = holder.eventPreviewImage.getLayoutParams();

                                    if(holder.eventData.getVisibility()==View.VISIBLE){
                                        holder.eventData.setVisibility(View.GONE);

                                        LinearLayout imgParent = (LinearLayout) holder.eventPreviewImage.getParent();
                                        double imgRatio = 1.0*imgLayoutParams.height/imgLayoutParams.width;

                                        imgLayoutParams.width=imgParent.getWidth();
                                        imgLayoutParams.height=(int) (imgParent.getWidth()*imgRatio);

                                    } else {

                                        holder.eventData.setVisibility(View.VISIBLE);

                                        imgLayoutParams.width=(int) getResources().getDimension(R.dimen.std_event_preview_thumbnail_width);
                                        imgLayoutParams.height=(int) getResources().getDimension(R.dimen.std_event_preview_thumbnail_height);
                                    }

                                }

                            });

                        } catch (IOException | DataFormatException e){

                            Log.d(TAG, e.getMessage());
                            holder.eventPreviewImage.setImageResource(R.drawable.broken);

                        }

                } else {

                    holder.eventPreviewImage.setImageResource(R.drawable.ic_image_black_24dp);

                }


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
                    eventsRecyclerView.scrollToPosition(positionStart);
                }

            }

        });

        eventsRecyclerView.setLayoutManager(linearLayoutManager);
        eventsRecyclerView.setAdapter(firebaseAdapter);

    }

    private void startCloudDownloadService(ProgressBar p) {

        p.setVisibility(View.VISIBLE);
        p.setProgress(50);

    }

    private void deleteEvent(File localFile, String remoteLocation, String eventKey) {

        /*
        elimina, se esiste, il file locale
         */

        if (localFile.exists()) {
            localFile.delete();
        }

        /*
        elimina, se esiste, il file remoto
         */

        // ottiene un riferimento alla posizione di storage sul cloud
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(String.format("gs://domotic-28a5e.appspot.com/%s", remoteLocation));
        storageRef.delete();

        /*
        elimina il nodo del database
         */
        eventsNode.child(eventKey).removeValue();

    }

    private class DownloadTask extends AsyncTask<Void, Float, Void> {

        private String localPath;
        private String remotePath;
        private ProgressBar progressBar;

        public DownloadTask(String remotePath, String localPath, ProgressBar progressBar) {
            this.localPath = localPath;
            this.remotePath = remotePath;
            this.progressBar = progressBar;

            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
        }

        protected Void doInBackground(Void... param) {

            // ottiene un riferimento alla posizione di storage sul cloud
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(String.format("gs://domotic-28a5e.appspot.com/%s", remotePath));

            // inizializza la directory locale per il download, se la directory non esiste la crea
            File downloadDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), localPath);
            if (!downloadDirectory.exists()) {
                Boolean b = downloadDirectory.mkdirs();
                Log.i(TAG, b.toString());
            }

            // inizializza il file locale per il download
            File localFile = new File(downloadDirectory.getPath() + File.separator + storageRef.getName());

            storageRef.getFile(localFile)
                    .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            //
                            // scaricamento completato

                            // nasconde la progressbar
                            progressBar.setVisibility(View.GONE);

                            // forza il refresh dell'Adapter
                            refreshAdapter();

                        }

                    })

                    .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            //
                            // scaricamento in corso

                            if (progressBar.isIndeterminate())
                                progressBar.setIndeterminate(false);

                            // calcola la percentuale dello scaricamento
                            int progress = (int) (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());

                            // aggiorna la progressBar di conseguenza
                            progressBar.setProgress(progress);

                        }

                    });

            return null;
        }

    }

    private void markAsRead(int position){

    }


}
