package com.apps.lore_f.domoticcontroller;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class VSControlFragment extends Fragment {

    public boolean viewCreated=false;

    private View fragmentView;
    private VSControlFragment me = this;

    public DatabaseReference zoneminderDBNode;
    public DeviceViewActivity parent;

    public VSControlFragment() {
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

                    VSCameraListFragment zoneMinderCameraListFragment = new VSCameraListFragment();
                    zoneMinderCameraListFragment.camerasNode=zoneminderDBNode.child("Monitors");
                    zoneMinderCameraListFragment.parent = me;

                    showFragment(zoneMinderCameraListFragment);

                    break;

                case R.id.BTN___ZMMGM___LOGS:

                    VSEventViewerFragment vsEventViewerFragment = new VSEventViewerFragment();
                    vsEventViewerFragment.parent=me;
                    showFragment(vsEventViewerFragment);

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
