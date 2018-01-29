package com.apps.lore_f.domoticcontroller;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class VideoSurveillanceActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private CollectionPagerAdapter collectionPagerAdapter;

    private String userName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* recupera gli extra dall'intent */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            userName = extras.getString("__USER");

        } else {

            finish();
            return;

        }

        /* visualizza il layout */
        setContentView(R.layout.activity_videosurveillance);

    }

    protected void onResume(){
        super.onResume();

        collectionPagerAdapter =
                new CollectionPagerAdapter(
                        getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.PGR___VIDEOSURVEILLANCE___MAINPAGER);
        viewPager.setAdapter(collectionPagerAdapter);

    }

    protected void onPause(){
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
            videoSurveillanceCameraListFragment.camerasNode =

                    videoSurveillanceEventsListFragment = new VideoSurveillanceEventsListFragment();
        }



        private String[] pageTitle={
                "Camera list",
                "Events list"
        };

        private int pagesCount=2;

        @Override
        public Fragment getItem(int i) {

            switch(i){
                case 0:
                    return videoSurveillanceCameraListFragment;

                case 1:
                    return videoSurveillanceEventsListFragment;

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
