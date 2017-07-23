package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class TorrentViewerFragment extends Fragment {

    String[] rawTorrentDataLines;
    int nOfTorrents;
    private List<TorrentInfo> torrents = new ArrayList<>();

    public TorrentViewerFragment() {
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
        return inflater.inflate(R.layout.fragment_torrent_viewer, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    public void updateContent(){

    }

    public void hideContent(){

        /*
        currentDirectoryTextView.setText(R.string.PROGRESSSTATUS_INFO___RETRIEVING_DIRECTORY_DATA);
        currentDirectoryListView.setEnabled(false);
        currentDirectoryListView.setAlpha(0.2f);
        */

    }

    private List<TorrentInfo> getTorrentsList(String[] rawDataLines) {


        List<TorrentInfo> outputList = new ArrayList<TorrentInfo>();


        return outputList;

    }
}
