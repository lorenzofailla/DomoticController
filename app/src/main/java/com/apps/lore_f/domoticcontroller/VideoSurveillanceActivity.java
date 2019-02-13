package com.apps.lore_f.domoticcontroller;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.apps.lore_f.domoticcontroller.activities.GroupSelection;
import com.google.firebase.database.FirebaseDatabase;

public class VideoSurveillanceActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private CollectionPagerAdapter collectionPagerAdapter;

    private String groupName;
    public String getGroupName(){
        return groupName;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // recupera il nome del gruppo [R.string.data_group_name] dalle shared preferences
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.data_file_key), Context.MODE_PRIVATE);

        groupName=sharedPref.getString(getString(R.string.data_group_name),null);

        if(groupName==null){

            /*
            questa parte di codice non dovrebbe essere mai eseguita, viene tenuta per evitare eccezioni
             */

            // nome del gruppo non impostato, lancia l'Activity GroupSelection per selezionare il gruppo a cui connettersi
            startActivity(new Intent(this, GroupSelection.class));

            // termina l'Activity corrente
            finish();
            return;

        }

        /* visualizza il layout */
        setContentView(R.layout.activity_videosurveillance);

    }

    protected void onResume() {
        super.onResume();

        collectionPagerAdapter =
                new CollectionPagerAdapter(
                        getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.PGR___VIDEOSURVEILLANCE___MAINPAGER);
        viewPager.setAdapter(collectionPagerAdapter);

    }

    protected void onPause() {
        super.onPause();
    }

    public class CollectionPagerAdapter extends FragmentPagerAdapter {

        private VideoSurveillanceCameraListFragment videoSurveillanceCameraListFragment;
        private VideoSurveillanceEventsListFragment videoSurveillanceEventsListFragment;

        public CollectionPagerAdapter(FragmentManager fm) {
            super(fm);

            /* inizializza i fragment */
            //
            // VideoSurveillanceCameraListFragment

            videoSurveillanceCameraListFragment = new VideoSurveillanceCameraListFragment();
            videoSurveillanceCameraListFragment.camerasNode = FirebaseDatabase.getInstance().getReference(String.format("/Groups/%s/VideoSurveillance/AvailableCameras", groupName));
            videoSurveillanceCameraListFragment.deviceName=null;

            videoSurveillanceEventsListFragment = new VideoSurveillanceEventsListFragment();
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.data_group_name), groupName);
            videoSurveillanceEventsListFragment.setArguments(bundle);

        }


        private String[] pageTitle = {
                "Camera list",
                "Events list"
        };

        private int pagesCount = 2;

        @Override
        public Fragment getItem(int i) {

            switch (i) {
                case 0:
                    return videoSurveillanceEventsListFragment;

                case 1:
                    return videoSurveillanceCameraListFragment;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return pagesCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitle[position];
        }

    }

}
