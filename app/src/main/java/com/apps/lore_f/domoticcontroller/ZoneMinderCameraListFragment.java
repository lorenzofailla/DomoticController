package com.apps.lore_f.domoticcontroller;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

public class ZoneMinderCameraListFragment extends Fragment {

    public boolean viewCreated=false;

    private View fragmentview;

    public DatabaseReference camerasNode;
    public DatabaseReference alarmsNode;



    public ZoneMinderCameraListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_zoneminder_control, container, false);

        view.findViewById(R.id.BTN___ZMMGM___CAMERAMANAGER).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___ZMMGM___LOGS).setOnClickListener(onClickListener);

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

        fragmentview.findViewById(R.id.BTN___ZMMGM___CAMERAMANAGER).setOnClickListener(null);
        fragmentview.findViewById(R.id.BTN___ZMMGM___LOGS).setOnClickListener(null);

    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {

            switch(v.getId()){

                case R.id.BTN___ZMMGM___CAMERAMANAGER:

                    break;

                case R.id.BTN___ZMMGM___LOGS:
                    break;

            }

        }
    };

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

}
