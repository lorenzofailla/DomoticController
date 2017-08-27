package com.apps.lore_f.domoticcontroller;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class CloudStorageFragment extends Fragment {

    public DatabaseReference databaseReference;

    public boolean viewCreated=false;

    private static final String TAG = "CloudStorageF";

    public RecyclerView storedFilesRecyclerView;
    public LinearLayoutManager linearLayoutManager;
    public FirebaseRecyclerAdapter<FileInCloudStorage, StoredFilesHolder> firebaseAdapter;

    interface CloudStorageFragmentListener{

        void onFileDownloadRequest(FileInCloudStorage file);

    }

    CloudStorageFragmentListener cloudStorageFragmentListener;

    public void addCloudStorageFragmentListener(CloudStorageFragmentListener listener){
        cloudStorageFragmentListener=listener;
    }

    public void removeCloudStorageFragmentListener(){
        cloudStorageFragmentListener=null;
    }

    public CloudStorageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cloud_storage, container, false);

        storedFilesRecyclerView = (RecyclerView) view.findViewById(R.id.RWV___CLOUDSTORAGEFRAGMENT___MAIN);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.i(TAG, dataSnapshot.getValue().toString());

                // aggiorna l'adapter
                refreshAdapter();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        viewCreated=true;

        return view;
    }

    public static class StoredFilesHolder extends RecyclerView.ViewHolder {

        public TextView fileNameTXV;
        public TextView requestorTXV;
        public TextView sizeTXV;
        public TextView nOfDownloadsTXV;

        public ImageButton downloadBTN;

        public StoredFilesHolder(View v) {
            super(v);
            fileNameTXV = (TextView) itemView.findViewById(R.id.TXV___STOREDFILE___NAME);
            requestorTXV = (TextView) itemView.findViewById(R.id.TXV___STOREDFILE___REQUESTOR_VALUE);
            sizeTXV = (TextView) itemView.findViewById(R.id.TXV___STOREDFILE___SIZE_VALUE);
            nOfDownloadsTXV = (TextView) itemView.findViewById(R.id.TXV___STOREDFILE___NOFDOWNLOADS_VALUE);

            downloadBTN = (ImageButton) itemView.findViewById(R.id.BTN___STOREDFILE___DOWNLOAD);

        }

    }

    private void refreshAdapter(){

        linearLayoutManager = new LinearLayoutManager(this.getContext());
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

                        if(cloudStorageFragmentListener!=null)
                            cloudStorageFragmentListener.onFileDownloadRequest(cloudFile);

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
