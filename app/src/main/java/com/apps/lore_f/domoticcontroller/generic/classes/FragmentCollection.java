package com.apps.lore_f.domoticcontroller.generic.classes;

import androidx.fragment.app.Fragment;

import java.util.HashMap;

public class FragmentCollection {

    private class FragmentInfo {

        private Fragment fragment;
        private FragmentType type;
        private String title;

        public FragmentInfo(Fragment fragment, FragmentType type, String title){

            this.fragment = fragment;
            this.type = type;
            this.title = title;

        }

        public Fragment getFragment(){

            return fragment;

        }

        public FragmentType getType(){

            return type;

        }

    }

    private HashMap<Integer, FragmentInfo> fragments = new HashMap<>();

    public enum FragmentType {

        DEVICE_INFO,
        DIRECTORY_NAVIGATOR,
        TORRENT_MANAGER,
        WOL_MANAGER,
        SSH_MANAGER,
        CAMERA_VIEWER

    }

    public void add(int index, Fragment fragment, FragmentType type, String title){

        fragments.put(index, new FragmentInfo(fragment, type,title));

    }

    public Fragment getFragment(int index){

        if(fragments.containsKey(index)){

            return fragments.get(index).getFragment();

        } else {

            return null;

        }

    }

    public FragmentType getFragmentType(int index){

        if(fragments.containsKey(index)){

            return fragments.get(index).getType();

        } else {

            return null;

        }

    }

    public int getFragmentsCount(){

        return fragments.size();

    }

    public void clearFragments(){

        fragments.clear();

    }

    public Fragment getFragmentByType(FragmentType type){

        for (int i = 0; i<getFragmentsCount(); i++){

            FragmentInfo info = fragments.get(i);

            if (info.getType().equals(type))
                return info.getFragment();


        }

        return null;

    }

}
