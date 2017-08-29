package com.apps.lore_f.domoticcontroller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class CloudStorageActivity extends AppCompatActivity {

    public DatabaseReference databaseReference;


    private static final String TAG = "CloudStorage";

    public RecyclerView storedFilesRecyclerView;
    public LinearLayoutManager linearLayoutManager;
    public FirebaseRecyclerAdapter<FileInCloudStorage, StoredFilesHolder> firebaseAdapter;

   private ValueEventListener valueEventListener = new ValueEventListener() {

       @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            Log.i(TAG, dataSnapshot.getValue().toString());

            // aggiorna l'adapter
            refreshAdapter();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_view);

    }

    @Override
    protected void onPause(){

        super.onPause();

        // rimuove il ValueEventListener dal nodo
        databaseReference.removeEventListener(valueEventListener);
    }

    @Override
    protected void onResume(){

        super.onResume();

        storedFilesRecyclerView = (RecyclerView) findViewById(R.id.RWV___CLOUDSTORAGEFRAGMENT___MAIN);

        // aggiunge un ValueEventListener al nodo
        databaseReference.addValueEventListener(valueEventListener);


    }


    public static class StoredFilesHolder extends RecyclerView.ViewHolder {

        public TextView fileNameTXV;
        public TextView requestorTXV;
        public TextView sizeTXV;
        public TextView nOfDownloadsTXV;

        public ImageButton downloadBTN;
        public ImageButton deleteBTN;

        public StoredFilesHolder(View v) {
            super(v);
            fileNameTXV = (TextView) itemView.findViewById(R.id.TXV___STOREDFILE___NAME);
            requestorTXV = (TextView) itemView.findViewById(R.id.TXV___STOREDFILE___REQUESTOR_VALUE);
            sizeTXV = (TextView) itemView.findViewById(R.id.TXV___STOREDFILE___SIZE_VALUE);
            nOfDownloadsTXV = (TextView) itemView.findViewById(R.id.TXV___STOREDFILE___NOFDOWNLOADS_VALUE);

            downloadBTN = (ImageButton) itemView.findViewById(R.id.BTN___STOREDFILE___DOWNLOAD);
            deleteBTN = (ImageButton) itemView.findViewById(R.id.BTN___STOREDFILE___DELETE);
        }

    }

    private void refreshAdapter(){

        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<FileInCloudStorage, StoredFilesHolder>(
                FileInCloudStorage.class,
                R.layout.row_holder_stored_file_element,
                StoredFilesHolder.class,
                databaseReference) {

            @Override
            protected void populateViewHolder(StoredFilesHolder holder, final FileInCloudStorage cloudFile, int position) {

                holder.fileNameTXV.setText(cloudFile.getFileName());
                holder.requestorTXV.setText(cloudFile.getRequestor());
                holder.sizeTXV.setText(""+cloudFile.getSize());
                holder.nOfDownloadsTXV.setText(""+cloudFile.getnOfDownloads());

                holder.downloadBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // avvia la procedura di download del file richiesto

                    }

                });

                holder.deleteBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // rimuove il file selezionato dal cloud storage

                    }
                });

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
                    storedFilesRecyclerView.scrollToPosition(positionStart);
                }

            }

        });

        storedFilesRecyclerView.setLayoutManager(linearLayoutManager);
        storedFilesRecyclerView.setAdapter(firebaseAdapter);

    }

}
