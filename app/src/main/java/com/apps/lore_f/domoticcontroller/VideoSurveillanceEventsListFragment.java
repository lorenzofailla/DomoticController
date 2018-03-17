package com.apps.lore_f.domoticcontroller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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

import static android.content.ContentValues.TAG;

public class VideoSurveillanceEventsListFragment extends Fragment {

    public boolean viewCreated = false;

    private View fragmentview;

    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<VSEvent, EventsHolder> firebaseAdapter;

    private ProgressDialog progressDialog;

    private RecyclerView eventsRecyclerView;
    private File downloadDirectoryRoot;
    private DatabaseReference eventsNode;
    private String groupName;

    public static class EventsHolder extends RecyclerView.ViewHolder {

        public TextView eventDateTextView;
        public TextView eventMonitorNameTextView;
        public ImageButton viewEventButton;
        public ProgressBar progressBar;

        public EventsHolder(View v) {
            super(v);

            eventDateTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTDATETIME);
            eventMonitorNameTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTMONITORNAME);
            viewEventButton = (ImageButton) v.findViewById(R.id.BTN___VSEVENTROW___REQUESTEVENT);
            progressBar = v.findViewById(R.id.PBR___VSEVENTROW___DOWNLOADPROGRESS);

        }

    }

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            refreshAdapter();

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
            protected void populateViewHolder(final EventsHolder holder, final VSEvent event, int position) {

                holder.eventDateTextView.setText(String.format("%s %s", event.getDate(), event.getTime()));
                holder.eventMonitorNameTextView.setText(event.getDevice());

                // crea il File relativo alla posizione di download locale sul dispositivo
                final File videoFile = new File(downloadDirectoryRoot, String.format("%s/%d/%s", event.getDevice(), event.getThreadID(), event.getVideoLink()));

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

                            String localLocation = String.format("Domotic/VideoSurveillance/DownloadedVideos/%s/%d", event.getDevice(), event.getThreadID());
                            String remoteLocation = String.format("Groups/%s/Devices/%s/VideoSurveillance/Events/%d/%s", groupName, event.getDevice(), event.getThreadID(), event.getVideoLink());
                            new DownloadTask(remoteLocation, localLocation, holder.progressBar).execute();

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

    private void startCloudDownloadService(ProgressBar p) {

        p.setVisibility(View.VISIBLE);
        p.setProgress(50);

    }

    private class DownloadTask extends AsyncTask<Void, Float, Void> {

        private String localPath;
        private String remotePath;
        private ProgressBar progressBar;

        public DownloadTask(String remotePath, String localPath, ProgressBar progressBar){
            this.localPath=localPath;
            this.remotePath=remotePath;
            this.progressBar=progressBar;

            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
        }

        protected Void doInBackground(Void... param){

            // ottiene un riferimento alla posizione di storage sul cloud
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(String.format("gs://domotic-28a5e.appspot.com/%s", remotePath));

            // inizializza la directory locale per il download, se la directory non esiste la crea
            File downloadDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), localPath);
            if (!downloadDirectory.exists()){
                Boolean b= downloadDirectory.mkdirs();
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
                            int progress = (int) (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());

                            // aggiorna la progressBar di conseguenza
                            progressBar.setProgress(progress);

                        }

                    });

            return null;
        }
    }


}
