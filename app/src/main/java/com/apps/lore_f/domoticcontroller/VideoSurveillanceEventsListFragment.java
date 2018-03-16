package com.apps.lore_f.domoticcontroller;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.io.File;

public class VideoSurveillanceEventsListFragment extends Fragment {

    public boolean viewCreated=false;

    private View fragmentview;

    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<VSEvent, EventsHolder> firebaseAdapter;

    private RecyclerView eventsRecyclerView;

    private File downloadDirectoryRoot;

    private DatabaseReference eventsNode;
    public void setEventsNode(DatabaseReference value){
        eventsNode=value;
    }

    public static class EventsHolder extends RecyclerView.ViewHolder{

        public TextView eventDateTextView;
        public TextView eventMonitorNameTextView;
        public ImageButton viewEventButton;

        public EventsHolder (View v){
            super(v);

            eventDateTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTDATETIME);
            eventMonitorNameTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTMONITORNAME);
            viewEventButton = (ImageButton) v.findViewById(R.id.BTN___VSEVENTROW___REQUESTEVENT);

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
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_videosurveillance_eventslist, container, false);

        // inizializza il riferimento alla directory dove i file dei video saranno scaricati
        downloadDirectoryRoot = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Domotic/VideoSurveillance/DownloadedVideos");

        eventsRecyclerView = (RecyclerView) view.findViewById(R.id.RWV___ZMEVENTVIEWERFRAGMENT___EVENTS);

        if (eventsNode!=null) {
            eventsNode.addValueEventListener(valueEventListener);
        }

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

        eventsNode.removeEventListener(valueEventListener);
        super.onDetach();

    }



    private void refreshAdapter(){

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<VSEvent, EventsHolder>(
                VSEvent.class,
                R.layout.row_holder_vsevent_element,
                EventsHolder.class,
                eventsNode) {

            @Override
            protected void populateViewHolder(EventsHolder holder, final VSEvent event, int position) {

                holder.eventDateTextView.setText(String.format("%s %s",event.getDate(), event.getTime()));
                holder.eventMonitorNameTextView.setText(event.getDevice());

                File videoFile = new File(downloadDirectoryRoot, String.format("%s/%d/%s", event.getDevice(), event.getThreadID(), event.getVideoLink()));

                if (videoFile.exists()){
                    holder.viewEventButton.setImageResource(R.drawable.connect);
                } else {
                    holder.viewEventButton.setImageResource(R.drawable.cloud_download);
                    holder.viewEventButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {



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


}
