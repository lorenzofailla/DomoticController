package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class TorrentViewerFragment extends Fragment {

    public String[] rawTorrentDataLines;
    public int nOfTorrents;
    public TorrentsListAdapter torrentsListAdapter;

    private List<TorrentInfo> torrents = new ArrayList<>();
    private ListView torrentsListView;

    public boolean viewCreated=false;

    public DeviceViewActivity parent;

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

        // inizializza l'handler ai controlli
        torrentsListView = (ListView) view.findViewById(R.id.LVW___TORRENTSLIST___MAIN);

        ImageButton addTorrentButton = (ImageButton) view.findViewById(R.id.BTN___TORRENTSVIEWER_ADDTORRENT);
        addTorrentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                parent.torrentAddRequest();

            }
        });

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


    public void updateContent(){

        if(rawTorrentDataLines!=null) {
            torrents = getTorrentsList(rawTorrentDataLines);

            torrentsListAdapter = new TorrentsListAdapter(getContext(), R.layout.torrents_list_row, torrents, parent);

            torrentsListView.setAdapter(torrentsListAdapter);
            torrentsListView.setVisibility(View.VISIBLE);

        } else {

            torrentsListView.setVisibility(View.INVISIBLE);

        }

    }


    private List<TorrentInfo> getTorrentsList(String[] rawDataLines) {

        List<TorrentInfo> outputList = new ArrayList<TorrentInfo>();

        for(int i=1; i<rawDataLines.length-1;i++)        {

            outputList.add(new TorrentInfo(rawDataLines[i]));

        }

        return outputList;

    }

}
