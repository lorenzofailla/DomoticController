package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class TorrentViewerFragment extends Fragment {

    String[] rawTorrentDataLines;
    int nOfTorrents;
    private List<TorrentInfo> torrents = new ArrayList<>();
    ListView torrentsListView;

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
        View view = inflater.inflate(R.layout.fragment_torrent_viewer, container, false);

        torrentsListView = (ListView) view.findViewById(R.id.LVW___TORRENTSLIST___MAIN);

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


    public void updateContent(){

        torrents = getTorrentsList(rawTorrentDataLines);

        TorrentsListAdapter torrentsListAdapter = new TorrentsListAdapter(getContext(), R.layout.torrents_list_row, torrents);

        torrentsListView.setAdapter(torrentsListAdapter);
        torrentsListView.setVisibility(View.VISIBLE);

    }

    public void hideContent(){

        /*
        currentDirectoryTextView.setText(R.string.PROGRESSSTATUS_INFO___RETRIEVING_DIRECTORY_DATA);
        currentDirectoryListView.setEnabled(false);
        currentDirectoryListView.setAlpha(0.2f);
        */
        torrentsListView.setVisibility(View.INVISIBLE);


    }

    private List<TorrentInfo> getTorrentsList(String[] rawDataLines) {

        List<TorrentInfo> outputList = new ArrayList<TorrentInfo>();

        for(int i=1; i<rawDataLines.length-1;i++)        {

            outputList.add(new TorrentInfo(rawDataLines[i]));

        }

        return outputList;

    }
}
