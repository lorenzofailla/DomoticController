package com.apps.lore_f.domoticcontroller.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;

import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.firebase.dataobjects.MotionEvent;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static apps.android.loref.GeneralUtilitiesLibrary.getTimeElapsed;

import com.apps.lore_f.domoticcontroller.R;

public class MotionEventsManagementActivity extends AppCompatActivity {

    private final static int BG_COLOR_SELECTED = Color.argb(32, 0, 0, 127);
    private final static long MAX_THUMBNAIL_DOWNLOAD_SIZE = 4194304;
    private final static String VIDEO_SUBDIR="MotionEventVideos";
    private final static String THUMB_SUBDIR="Thumbnails";

    private View fragmentView;

    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<MotionEvent, EventsHolder> firebaseAdapter;

    private RecyclerView eventsRecyclerView;
    private File downloadDirectoryRoot;
    private DatabaseReference eventsNode;
    private Query eventsQuery;
    private String groupName;

    private String[] eventKeys;

    private int selectedPosition = -1;

    private String childKeyFilter = null;
    private String childValueFilter = null;

    private static int stdPreviewImageWidth;
    private static int stdPreviewImageHeight;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.motionevent_options, menu);
    }

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
                    childKeyFilter = "NewItem";

                    break;

            }

            generateQuery();

        }

    };

    public static class EventsHolder extends RecyclerView.ViewHolder {

        public TextView eventDateTextView;
        public TextView eventMonitorNameTextView;

        public ProgressBar progressBar;
        public TextView eventCameraNameTextView;

        public ImageView eventPreviewImage;

        public ImageView newItemImage;
        public ImageView lockedItemImage;
        public ImageView eventLocationImage;

        public ConstraintLayout eventLabels;

        public ConstraintLayout eventContainer;

        public EventsHolder(View v) {
            super(v);

            eventDateTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTDATETIME);
            eventMonitorNameTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTDEVICENAME);
            eventCameraNameTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTCAMERANAME);

            progressBar = (ProgressBar) v.findViewById(R.id.PBR___VSEVENTROW___DOWNLOADPROGRESS);
            eventLabels = (ConstraintLayout) v.findViewById(R.id.CLA___VSEVENTROW___LABELS);

            eventPreviewImage = (ImageView) v.findViewById(R.id.IVW___VSEVENTROW___EVENTPREVIEW);
            newItemImage = (ImageView) v.findViewById(R.id.IVW___VSEVENTROW___NEWITEM);
            lockedItemImage = (ImageView) v.findViewById(R.id.IVW___VSEVENTROW___LOCKEDITEM);
            eventLocationImage = (ImageView) v.findViewById(R.id.IVW___VSEVENTROW___EVENTLOCATION);

            eventContainer = (ConstraintLayout) v.findViewById(R.id.CLA___VSEVENTROW___EVENT);

        }

    }

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            // definisce l'array delle keys dei record contenuti nel dataSnapshot

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

    public MotionEventsManagementActivity() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // recupera il nome del gruppo [R.string.data_group_name] dalle shared preferences
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.data_file_key), Context.MODE_PRIVATE);

        groupName=sharedPref.getString(getString(R.string.data_group_name),null);

        if(groupName==null){ // should never happen

            // nome del gruppo non impostato, lancia l'Activity GroupSelection per selezionare il gruppo a cui connettersi
            startActivity(new Intent(this, GroupSelection.class));

            // termina l'Activity corrente
            finish();
            return;

        }

        // visualizza il layout
        setContentView(R.layout.activity_motioneventsmanagement);

    }

    @Override
    protected void onResume(){

        // calls the onResume() method on the superclass
        super.onResume();

        // inizializza il nodo del database di Firebase contenente le informazioni sugli eventi
        eventsNode = FirebaseDatabase.getInstance().getReference(String.format("MotionEvents/%s", groupName));

        // inizializza il riferimento alla directory dove i file dei video saranno scaricati
        downloadDirectoryRoot = new File(Environment.getExternalStorageDirectory(), "Domotic");

        // crea la directory principale se non esiste
        if(!downloadDirectoryRoot.exists()) downloadDirectoryRoot.mkdir();

        // crea le sottodirectory se non esistono
        File videoDir=new File(downloadDirectoryRoot,VIDEO_SUBDIR);
        if(!videoDir.exists()) videoDir.mkdir();

        File thumbnailDir=new File(downloadDirectoryRoot,THUMB_SUBDIR);
        if(!thumbnailDir.exists()) thumbnailDir.mkdir();

        eventsRecyclerView = (RecyclerView) findViewById(R.id.RWV___VSEVENTVIEWERFRAGMENT___EVENTS);

        // assegna l'OnClickListener ai pulsanti
        Button filterAllButton = (Button) findViewById(R.id.BTN___VSEVENTVIEWERFRAGMENT___FILTER_ALL);
        Button filterNewButton = (Button) findViewById(R.id.BTN___VSEVENTVIEWERFRAGMENT___FILTER_NEWONLY);
        filterAllButton.setOnClickListener(buttonClickListener);
        filterNewButton.setOnClickListener(buttonClickListener);

        // innesca l'azione di click sul pulsante filtro ALL
        filterAllButton.callOnClick();

        refreshAdapter();


    }

    @Override
    public void onPause() {

        // rimuove l'assegnazione dell'OnClickListener ai pulsanti
        if (fragmentView != null) {
            Button filterAllButton = (Button) fragmentView.findViewById(R.id.BTN___VSEVENTVIEWERFRAGMENT___FILTER_ALL);
            Button filterNewButton = (Button) fragmentView.findViewById(R.id.BTN___VSEVENTVIEWERFRAGMENT___FILTER_NEWONLY);
            filterAllButton.setOnClickListener(null);
            filterNewButton.setOnClickListener(null);
        }

        // rimuove il ValueEventListener dal nodo del database di Firebase
        eventsQuery.removeEventListener(valueEventListener);

        unregisterForContextMenu(eventsRecyclerView);

        super.onPause();

    }

    private void refreshAdapter() {

        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<MotionEvent, EventsHolder>(
                MotionEvent.class,
                R.layout.row_holder_motionevent_element,
                EventsHolder.class,
                eventsQuery) {

            @Override
            protected void populateViewHolder(final EventsHolder holder, final MotionEvent event, final int position) {


                if (event.isNewItem().equals("true")) {// se è un nuovo evento, mostra l'immagine newItemImage

                    holder.newItemImage.setVisibility(VISIBLE);

                } else {

                    holder.newItemImage.setVisibility(GONE);

                }

                // gestisce la visualizzazione degli elementi in funzione del valore del campo "lockedItem"
                if (event.isLockedItem().equals("true")) {

                    holder.lockedItemImage.setVisibility(VISIBLE);

                } else {

                    holder.lockedItemImage.setVisibility(GONE);

                }

                holder.eventDateTextView.setText(getTimeElapsed(Long.parseLong(event.getTimestamp()), getApplicationContext())
                );

                holder.eventMonitorNameTextView.setText(event.getDevice());
                holder.eventCameraNameTextView.setText(event.getCameraName());

                // crea il File relativo alla posizione di download locale sul dispositivo
                final File localVideoFile = new File(downloadDirectoryRoot, VIDEO_SUBDIR+"/"+event.getVideoID());

                // crea il File relativo alla posizione di download locale sul dispositivo
                final File localThumbnailFile = new File(downloadDirectoryRoot, THUMB_SUBDIR+"/"+event.getThumbnailID());

                final String videoFileRemoteLocation = String.format("MotionEvents/%s/%s", groupName, event.getVideoID());
                final String thumbnailFileRemoteLocation = String.format("MotionEvents/%s/%s", groupName, event.getThumbnailID());

                // controlla se il File creato esiste
                if (localVideoFile.exists()) { // esiste

                    // imposta l'immagine da visualizzare
                    holder.eventLocationImage.setImageResource(R.drawable.sdcard);

                    // imposta l'OnClickListener per lanciare il video tramite un Intent
                    holder.eventPreviewImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // lancia il video
                            playVideo(localVideoFile.getAbsolutePath());
                            selectedPosition = position;

                            // segna l'evento come già letto
                            markAsRead(eventKeys[position], "false");

                        }

                    });

                } else { // non esiste

                    // imposta l'immagine da visualizzare
                    holder.eventLocationImage.setImageResource(R.drawable.cloud);

                    // imposta l'OnClickListener per scaricare il video in una cartella locale
                    holder.eventPreviewImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // mostra la progressbar
                            holder.progressBar.setVisibility(VISIBLE);
                            holder.progressBar.setIndeterminate(true);

                            FirebaseStorage.getInstance().getReference(videoFileRemoteLocation).getFile(localVideoFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                    // nasconde la progressbar
                                    holder.progressBar.setVisibility(GONE);

                                    // esegue il video
                                    playVideo(localVideoFile.getAbsolutePath());

                                    // segna l'evento come già visto
                                    markAsRead(eventKeys[position], "false");

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    // nasconde la progressbar
                                    holder.progressBar.setVisibility(GONE);

                                }

                            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                    if (holder.progressBar.isIndeterminate())
                                        holder.progressBar.setIndeterminate(false);

                                    // calcola la percentuale dello scaricamento
                                    int progress = (int) (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());

                                    // aggiorna la progressBar di conseguenza
                                    holder.progressBar.setProgress(progress);

                                }

                            });

                        }

                    });

                }

                // recupera i dati dell'immagine della preview

                // controlla che sia già stata scaricata
                if(localThumbnailFile.exists()) { // è già stata scaricata

                    updateEventPreviewImage(holder.eventPreviewImage,localThumbnailFile.getAbsolutePath());

                } else { // non è già stata scaricata

                    FirebaseStorage.getInstance().getReference(thumbnailFileRemoteLocation).getFile(localThumbnailFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            updateEventPreviewImage(holder.eventPreviewImage,localThumbnailFile.getAbsolutePath());

                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // adatta le dimensioni dell'immagine a quelle disponibili su schermo
                            holder.eventPreviewImage.setImageResource(R.drawable.broken);
                        }
                    });

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

    private void updateEventPreviewImage(ImageView image, String imagePath){
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        image.setImageBitmap(bitmap);
    }

    private void deleteEvent(File localFile, String remoteVideoLocation, String remoteThumbnailLocation, String eventKey) {

        // elimina, se esiste, il file locale

        if (localFile.exists()) {
            localFile.delete();
        }

        //        elimina, se esiste, il file remoto

        // ottiene un riferimento alla posizione di storage sul cloud
        FirebaseStorage.getInstance().getReference(remoteVideoLocation).delete();
        FirebaseStorage.getInstance().getReference(remoteThumbnailLocation).delete();

        //        elimina il nodo del database
        eventsNode.child(eventKey).removeValue();

    }

    private void markAsRead(String eventKey, String value) {

        eventsNode.child(eventKey).child("NewItem").setValue(value);

    }

    private void markAsLocked(String eventKey, String value) {

        eventsNode.child(eventKey).child("LockedItem").setValue(value);

    }

    private void playVideo(String videoFullPath) {

        // lancia il video
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(videoFullPath), "video/*");
        startActivity(intent);

    }

    private void shareVideo(String videoFullPath) {

        Uri uriPath = Uri.parse(videoFullPath);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Text");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriPath);
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
