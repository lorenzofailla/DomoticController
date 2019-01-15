package com.apps.lore_f.domoticcontroller;

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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.firebase.dataobjects.VSEvent;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import loref.android.apps.androidshapes.RoundRect;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static apps.android.loref.GeneralUtilitiesLibrary.decompress;
import static apps.android.loref.GeneralUtilitiesLibrary.getTimeElapsed;
import static apps.android.loref.GeneralUtilitiesLibrary.getTimeMillis;

public class VideoSurveillanceEventsListFragment extends Fragment {

    private final static int BG_COLOR_SELECTED = Color.argb(32, 0, 0, 127);

    private View fragmentView;

    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<VSEvent, EventsHolder> firebaseAdapter;

    private RecyclerView eventsRecyclerView;
    private File downloadDirectoryRoot;
    private DatabaseReference eventsNode;
    private Query eventsQuery;
    private String groupName;

    private String[] eventKeys;

    private int selectedPosition = -1;

    private String childKeyFilter = null;
    private String childValueFilter = null;

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.BTN___VSEVENTVIEWERFRAGMENT___FILTER_ALL:
                    childValueFilter = "";
                    childKeyFilter = "";
                    break;

                case R.id.BTN___VSEVENTVIEWERFRAGMENT___FILTER_NEWONLY:
                    childValueFilter = "true";
                    childKeyFilter = "newItem";

                    break;

            }

            generateQuery();

        }

    };

    public static class EventsHolder extends RecyclerView.ViewHolder {

        public TextView eventDateTextView;
        public TextView eventMonitorNameTextView;

        public ImageButton deleteEventButton;
        public ImageButton shareEventButton;
        public ImageButton lockEventButton;

        public ProgressBar progressBar;
        public TextView eventCameraNameTextView;

        public ImageView eventPreviewImage;

        public ImageView newItemImage;
        public ImageView lockedItemImage;

        public RoundRect eventContainer;
        public LinearLayout eventLabels;
        public LinearLayout eventOptions;

        public EventsHolder(View v) {
            super(v);

            eventDateTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTDATETIME);
            eventMonitorNameTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTDEVICENAME);
            eventCameraNameTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTCAMERANAME);
            shareEventButton = (ImageButton) v.findViewById(R.id.BTN___VSEVENTROW___SHAREEVENT);
            deleteEventButton = (ImageButton) v.findViewById(R.id.BTN___VSEVENTROW___DELETEEVENT);
            lockEventButton = (ImageButton) v.findViewById(R.id.BTN___VSEVENTROW___LOCKEVENT);

            progressBar = (ProgressBar) v.findViewById(R.id.PBR___VSEVENTROW___DOWNLOADPROGRESS);eventContainer = (RoundRect) v.findViewById(R.id.RRE___VSEVENTROW___CONTAINER);
            eventLabels = (LinearLayout) v.findViewById(R.id.LLA___VSEVENTROW___LABELS);
            eventOptions = (LinearLayout) v.findViewById(R.id.LLA___VSEVENTROW___OPTIONS);

            eventPreviewImage = (ImageView) v.findViewById(R.id.IVW___VSEVENTROW___EVENTPREVIEW);
            newItemImage = (ImageView) v.findViewById(R.id.IVW___VSEVENTROW___NEWITEM);
            lockedItemImage = (ImageView) v.findViewById(R.id.IVW___VSEVENTROW___LOCKEDITEM);



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

        // inizializza il riferimento alla directory dove i file dei video saranno scaricati
        downloadDirectoryRoot = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Domotic/VideoSurveillance/DownloadedVideos");

        eventsRecyclerView = (RecyclerView) view.findViewById(R.id.RWV___VSEVENTVIEWERFRAGMENT___EVENTS);

        // assegna l'OnClickListener ai pulsanti
        Button filterAllButton = (Button) view.findViewById(R.id.BTN___VSEVENTVIEWERFRAGMENT___FILTER_ALL);
        Button filterNewButton = (Button) view.findViewById(R.id.BTN___VSEVENTVIEWERFRAGMENT___FILTER_NEWONLY);
        filterAllButton.setOnClickListener(buttonClickListener);
        filterNewButton.setOnClickListener(buttonClickListener);

        // innesca l'azione di click sul pulsante filtro ALL
        filterAllButton.callOnClick();

        fragmentView = view;

        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {

        // rimuove l'assegnazione dell'OnClickListener ai pulsanti
        if (fragmentView != null) {
            Button filterAllButton = (Button) fragmentView.findViewById(R.id.BTN___VSEVENTVIEWERFRAGMENT___FILTER_ALL);
            Button filterNewButton = (Button) fragmentView.findViewById(R.id.BTN___VSEVENTVIEWERFRAGMENT___FILTER_NEWONLY);
            filterAllButton.setOnClickListener(null);
            filterNewButton.setOnClickListener(null);
        }

        // rimuove il ValueEventListener dal nodo del database di Firebase
        eventsQuery.removeEventListener(valueEventListener);

        super.onDetach();

    }

    private void refreshAdapter() {

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<VSEvent, EventsHolder>(
                VSEvent.class,
                R.layout.row_holder_vsevent_element,
                EventsHolder.class,
                eventsQuery) {

            @Override
            protected void populateViewHolder(final EventsHolder holder, final VSEvent event, final int position) {

                if (position == selectedPosition) {

                    holder.eventContainer.setBackgroundColor(BG_COLOR_SELECTED);

                } else {

                    holder.eventContainer.setBackgroundColor(Color.TRANSPARENT);

                }

                // gestisce la visualizzazione dei pulsanti di opzione

                holder.eventLabels.setVisibility(VISIBLE);
                holder.eventOptions.setVisibility(GONE);

                // se è un nuovo evento, mostra l'immagine newItemImage
                if (event.isNewItem().equals("true")) {

                    holder.newItemImage.setVisibility(VISIBLE);

                } else {

                    holder.newItemImage.setVisibility(GONE);

                }

                // gestisce la visualizzazione degli elementi in funzione del valore del campo "lockedItem"
                if (event.isLockedItem().equals("true")) {

                    holder.lockedItemImage.setVisibility(VISIBLE);
                    holder.deleteEventButton.setVisibility(GONE);

                    holder.lockEventButton.setImageResource(R.drawable.unlock);
                    holder.lockEventButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            markAsLocked(eventKeys[position], "false");
                        }
                    });

                } else {

                    holder.lockedItemImage.setVisibility(GONE);
                    holder.deleteEventButton.setVisibility(VISIBLE);

                    holder.lockEventButton.setImageResource(R.drawable.lock);
                    holder.lockEventButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            markAsLocked(eventKeys[position], "true");
                        }
                    });
                }

                holder.eventDateTextView.setText(
                        getTimeElapsed(
                                getTimeMillis(
                                        String.format("%s %s", event.getDate(), event.getTime()),
                                        "yyyy-MM-dd HH.mm.ss"), getContext()
                        )
                );

                holder.eventMonitorNameTextView.setText(event.getDevice());
                holder.eventCameraNameTextView.setText(event.getCameraName());

                // crea il File relativo alla posizione di download locale sul dispositivo
                final File videoFile = new File(downloadDirectoryRoot, String.format("%s/%s/%s", event.getDevice(), event.getThreadID(), event.getVideoLink()));
                final String remoteLocation = String.format("Groups/%s/Devices/%s/VideoSurveillance/Events/%s/%s", groupName, event.getDevice(), event.getThreadID(), event.getVideoLink());

                // controlla se il File creato esiste
                if (videoFile.exists()) {
                    //
                    // esiste

                    // imposta l'OnClickListener per lanciare il video tramite un Intent
                    holder.eventContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // lancia il video
                            playVideo(videoFile.getAbsolutePath());
                            selectedPosition = position;

                            // segna l'evento come già letto
                            markAsRead(eventKeys[position], "false");

                        }

                    });

                    /*
                    mostra il pulsante per condividere il video dell'evento
                     */
                    holder.shareEventButton.setVisibility(VISIBLE);
                    holder.shareEventButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shareVideo(videoFile.getAbsolutePath());
                        }
                    });

                } else {
                    //
                    // non esiste

                    // imposta l'OnClickListener per scaricare il video in una cartella locale
                    holder.eventContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String localLocation = String.format("Domotic/VideoSurveillance/DownloadedVideos/%s/%s", event.getDevice(), event.getThreadID());

                            new DownloadTask(remoteLocation, localLocation, holder.progressBar, eventKeys[position]).execute();

                            selectedPosition = position;

                        }

                    });

                    /*
                    nasconde il pulsante per condividere il video dell'evento
                     */
                    holder.shareEventButton.setVisibility(GONE);

                }

                holder.deleteEventButton.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View view) {

                        deleteEvent(videoFile, remoteLocation, eventKeys[position]);

                        if (position == selectedPosition) {
                            selectedPosition = -1;
                        }

                    }

                });

                // definisce l'immagine dell'evento
                String eventImageData = event.getEventPictureData();
                if (eventImageData != null) {

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
                                Space s = (Space) fragmentView.findViewById(R.id.SPC___VSEVENTROW___BOTTOM_LEFT);

                                if (holder.eventLabels.getVisibility() == VISIBLE) {

                                    // ingrandisce l'immagine
                                    holder.eventLabels.setVisibility(GONE);
                                    holder.eventOptions.setVisibility(VISIBLE);

                                    RoundRect container = (RoundRect) fragmentView.findViewById(R.id.RRE___VSEVENTROW___CONTAINER);
                                    double imgRatio = 1.0 * imgLayoutParams.height / imgLayoutParams.width;
                                    int w = container.getNetWidth();
                                    imgLayoutParams.width = w;
                                    imgLayoutParams.height = (int) (w * imgRatio);

                                } else {

                                    // gestisce la visualizzazione dei pulsanti di opzione

                                    holder.eventLabels.setVisibility(VISIBLE);
                                    holder.eventOptions.setVisibility(GONE);

                                    // riporta l'immagine al valore originale
                                    imgLayoutParams.width = (int) getResources().getDimension(R.dimen.std_event_preview_thumbnail_width);
                                    imgLayoutParams.height = (int) getResources().getDimension(R.dimen.std_event_preview_thumbnail_height);
                                }

                            }

                        });

                    } catch (IOException | DataFormatException e) {

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

        p.setVisibility(VISIBLE);
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
        private String eventKey;

        public DownloadTask(String remotePath, String localPath, ProgressBar progressBar, String eventKey) {
            this.localPath = localPath;
            this.remotePath = remotePath;
            this.progressBar = progressBar;
            this.eventKey = eventKey;

            progressBar.setIndeterminate(true);
            progressBar.setVisibility(VISIBLE);
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
            final String localFileUrl = downloadDirectory.getPath() + File.separator + storageRef.getName();
            File localFile = new File(localFileUrl);

            storageRef.getFile(localFile)
                    .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            //
                            // scaricamento completato

                            // nasconde la progressbar
                            progressBar.setVisibility(GONE);

                            // esegue il video
                            playVideo(localFileUrl);

                            // segna l'evento come già visto
                            markAsRead(eventKey, "false");


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

    private void markAsRead(String eventKey, String value) {

        eventsNode.child(eventKey).child("newItem").setValue(value);

    }

    private void markAsLocked(String eventKey, String value) {

        eventsNode.child(eventKey).child("lockedItem").setValue(value);

    }

    private void playVideo(String videoFullPath) {

        // lancia il video
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoFullPath));
        intent.setDataAndType(Uri.parse(videoFullPath), "video/avi");
        startActivity(intent);

    }

    private void shareVideo(String videoFullPath) {

        Uri uriPath = Uri.parse(videoFullPath);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Text");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriPath);
        shareIntent.setType("video/avi");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "send"));

    }

    private void generateQuery() {


        if (!childKeyFilter.equals("")) {

            //eventsNode.orderByChild(childKeyFilter).equalTo(childValueFilter).addValueEventListener(valueEventListener);
            eventsQuery = eventsNode.orderByChild(childKeyFilter).equalTo(childValueFilter);

        } else {

            eventsQuery = eventsNode.orderByKey();
        }

        eventsQuery.addValueEventListener(valueEventListener);

    }

}
