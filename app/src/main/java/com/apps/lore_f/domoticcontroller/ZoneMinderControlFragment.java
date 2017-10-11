package com.apps.lore_f.domoticcontroller;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ZoneMinderControlFragment extends Fragment {

    public boolean viewCreated=false;

    private View fragmentView;
    private ZoneMinderControlFragment me = this;

    public DatabaseReference zoneminderDBNode;
    public DeviceViewActivity parent;

    public ZoneMinderControlFragment() {
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

        fragmentView=view;

        manageFullScreenMode(false);

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

        fragmentView.findViewById(R.id.BTN___ZMMGM___CAMERAMANAGER).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___ZMMGM___LOGS).setOnClickListener(null);

    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {

            switch(v.getId()){

                case R.id.BTN___ZMMGM___CAMERAMANAGER:

                    ZoneMinderCameraListFragment zoneMinderCameraListFragment = new ZoneMinderCameraListFragment();
                    zoneMinderCameraListFragment.camerasNode=zoneminderDBNode.child("Monitors");
                    zoneMinderCameraListFragment.parent = me;

                    showFragment(zoneMinderCameraListFragment);

                    break;

                case R.id.BTN___ZMMGM___LOGS:

                    ZoneMinderEventViewerFragment zoneMinderEventViewerFragment = new ZoneMinderEventViewerFragment();
                    showFragment(zoneMinderEventViewerFragment);

                    break;

            }

        }
    };

    public void showFragment(Fragment fragment) {

        /* mostra il fragment passato in argomento */
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.VIE___ZMMGM___SUBVIEW, fragment);

        fragmentTransaction.commit();

    }

    public void manageFullScreenMode(boolean sts){

        if(sts){

            fragmentView.findViewById(R.id.LLO_ZMMGM_BUTTONSTRIP).setVisibility(GONE);

        } else {

            fragmentView.findViewById(R.id.LLO_ZMMGM_BUTTONSTRIP).setVisibility(VISIBLE);
        }

    }

}
