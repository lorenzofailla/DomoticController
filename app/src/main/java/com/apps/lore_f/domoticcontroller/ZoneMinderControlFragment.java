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

public class ZoneMinderControlFragment extends Fragment {

    public boolean viewCreated=false;

    private View fragmentview;

    public DatabaseReference camerasNode;
    public DatabaseReference alarmsNode;



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

                    ZoneMinderCameraListFragment zoneMinderCameraListFragment = new ZoneMinderCameraListFragment();
                    showFragment(zoneMinderCameraListFragment);

                    break;

                case R.id.BTN___ZMMGM___LOGS:

                    ZoneMinderEventViewerFragment zoneMinderEventViewerFragment = new ZoneMinderEventViewerFragment();
                    showFragment(zoneMinderEventViewerFragment);

                    break;

            }

        }
    };

    private void showFragment(Fragment fragment) {

        /* mostra il fragment passato in argomento */
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.VIE___ZMMGM___SUBVIEW, fragment);

        fragmentTransaction.commit();
    }

}
