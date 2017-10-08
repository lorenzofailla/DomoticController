package com.apps.lore_f.domoticcontroller;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;

public class ZoneMinderEventViewerFragment extends Fragment {

    public boolean viewCreated=false;

    private View fragmentview;

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
        super.onDetach();


    }


    /*
    private void refreshAdapter(){

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<LogEntry, DeviceInfoFragment.DeviceLogHolder>(
                LogEntry.class,
                R.layout.row_holder_log_element,
                DeviceInfoFragment.DeviceLogHolder.class,
                logsNode) {

            @Override
            protected void populateViewHolder(DeviceInfoFragment.DeviceLogHolder holder, final LogEntry log, int position) {

                holder.dateTimeTXV.setText(log.getDatetime());
                holder.logTypeTXV.setText(log.getLogtype());
                holder.logDescTXV.setText(log.getLogdesc());

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
                    logRecyclerView.scrollToPosition(positionStart);
                }

            }

        });

        logRecyclerView.setLayoutManager(linearLayoutManager);
        logRecyclerView.setAdapter(firebaseAdapter);

    }
    */

}
