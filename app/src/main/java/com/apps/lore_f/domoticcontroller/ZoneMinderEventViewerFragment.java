package com.apps.lore_f.domoticcontroller;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ZoneMinderEventViewerFragment extends Fragment {

    public boolean viewCreated=false;

    private View fragmentview;

    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<ZMEvent, EventsHolder> firebaseAdapter;

    public ZoneMinderControlFragment parent;

    private RecyclerView eventsRecyclerView;

    private DatabaseReference eventsNode;

    public static class EventsHolder extends RecyclerView.ViewHolder{

        public ImageView eventTypeImage;
        public TextView eventDateTextView;
        public TextView eventMonitorNameTextView;
        public ImageButton ViewEventButton;

        public EventsHolder (View v){
            super(v);

            eventTypeImage = (ImageView) v.findViewById(R.id.IVW___ZMEVENTROW___EVENTTYPE);
            eventDateTextView = (TextView) v.findViewById(R.id.TXV___ZMEVENTROW___EVENTDATETIME);
            eventMonitorNameTextView = (TextView) v.findViewById(R.id.TXV___ZMEVENTROW___EVENTMONITORNAME);
            ViewEventButton = (ImageButton) v.findViewById(R.id.BTN___ZMEVENTROW___VIEWEVENT);

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

    public ZoneMinderEventViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_zoneminder_eventviewer, container, false);

        eventsRecyclerView = (RecyclerView) view.findViewById(R.id.RWV___ZMEVENTVIEWERFRAGMENT___EVENTS);

        eventsNode = FirebaseDatabase.getInstance().getReference("/Users/lorenzofailla/Devices/"+parent.parent.remoteDeviceName+"/ZoneMinder/Events");
        eventsNode.addValueEventListener(valueEventListener);
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

        firebaseAdapter = new FirebaseRecyclerAdapter<ZMEvent, EventsHolder>(
                ZMEvent.class,
                R.layout.row_holder_zmevent_element,
                EventsHolder.class,
                eventsNode) {

            @Override
            protected void populateViewHolder(EventsHolder holder, final ZMEvent event, int position) {

                holder.eventDateTextView.setText(event.getStartTime());
                holder.eventMonitorNameTextView.setText("");

                if(event.getCause().equals("Motion")){
                    holder.eventTypeImage.setImageResource(R.drawable.run);
                } else {
                    holder.eventTypeImage.setImageResource(R.drawable.manual);
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
