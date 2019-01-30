package com.apps.lore_f.domoticcontroller.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.DeviceViewActivity;
import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.generic.dataadapters.TorrentsListAdapter;
import com.apps.lore_f.domoticcontroller.generic.dataadapters.WOLDevicesListAdapter;
import com.apps.lore_f.domoticcontroller.generic.dataobjects.TorrentInfo;
import com.apps.lore_f.domoticcontroller.generic.dataobjects.WOLDeviceInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TorrentViewerFragment extends Fragment {

public int nOfTorrents;

    private static final String TAG="TorrentViewerFragment";

    private ListView torrentsListView;
    private TextView mainInfoTextView;

    public boolean viewCreated=false;

    public DeviceViewActivity parent;

    private View fragmentView;

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
        fragmentView=view;

        // inizializza l'handler ai controlli
        torrentsListView = (ListView) view.findViewById(R.id.LVW___TORRENTSLIST___MAIN);
        mainInfoTextView = (TextView) view.findViewById(R.id.TXV___TORRENTSVIEWER_INFO);

        ImageButton addTorrentButton = (ImageButton) view.findViewById(R.id.BTN___TORRENTSVIEWER_ADDTORRENT);
        addTorrentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                parent.torrentAddRequest();

            }
        });

        ImageButton refreshButton = (ImageButton) view.findViewById(R.id.BTN___TORRENTSVIEWER_REFRESH);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                parent.requestTorrentsData();

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

        ImageButton addTorrentButton = (ImageButton) fragmentView.findViewById(R.id.BTN___TORRENTSVIEWER_ADDTORRENT);
        ImageButton refreshButton = (ImageButton) fragmentView.findViewById(R.id.BTN___TORRENTSVIEWER_REFRESH);

        addTorrentButton.setOnClickListener(null);
        refreshButton.setOnClickListener(null);

    }

    public void updateContent(String dataJSON){

        if(!viewCreated)
            return;

        List<TorrentInfo> torrents = new ArrayList<TorrentInfo>();
        String infoMessage;

        try {

            JSONObject data = new JSONObject(dataJSON);

            JSONObject generalData = data.getJSONObject("TorrentData");

            if(generalData.has("Torrents")) {

                JSONArray torrentsData = generalData.getJSONArray("Torrents");

                for (int i = 0; i < torrentsData.length(); i++) {
                    torrents.add(new TorrentInfo(torrentsData.getString(i)));
                }

                infoMessage = getString(R.string.TORRENTSMANAGER_LABEL_AVAILABLETORRENTS) + " " + torrents.size();

            } else {

                infoMessage = getString(R.string.TORRENTSMANAGER_LABEL_NOTORRENTS);

            }

            TorrentsListAdapter adapter = new TorrentsListAdapter(getContext(), R.layout.row_holder_torrent_element, torrents, this.parent);
            torrentsListView.setAdapter(adapter);

            mainInfoTextView.setText(infoMessage);


        } catch (JSONException e) {

            Log.e(TAG, "Unable to update the view. JSON data: "+ dataJSON);

        }

    }

}
