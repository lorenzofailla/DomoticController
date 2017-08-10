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
    public TorrentsListAdapter.TorrentsListAdapterListener localTorrentsListAdapterListener;

    private List<TorrentInfo> torrents = new ArrayList<>();
    private ListView torrentsListView;

    public TorrentViewerFragment() {
        // Required empty public constructor
    }

    interface TorrentsViewerListener{

        void onAddTorrentRequest();

    }

    TorrentsViewerListener torrentsViewerListener;

    public void setTorrentsViewerListener(TorrentsViewerListener listener){

        torrentsViewerListener=listener;

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

                // apre la finestra di dialogo per aggiungere un torrent
                if(torrentsViewerListener!=null) torrentsViewerListener.onAddTorrentRequest();

            }
        });
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

        torrentsListAdapter = new TorrentsListAdapter(getContext(), R.layout.torrents_list_row, torrents);
        if (localTorrentsListAdapterListener!=null) torrentsListAdapter.setTorrentsListAdapterListener(localTorrentsListAdapterListener);

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
