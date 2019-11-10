package com.apps.lore_f.domoticcontroller.fragments;

public class TorrentViewerFragment /*extends Fragment*/ {

    /*private static final String TAG = "TorrentViewerFragment";
    public int nOfTorrents;

    private ListView torrentsListView;
    private TextView mainInfoTextView;
    public boolean viewCreated = false;

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
        fragmentView = view;

        parent = (DeviceViewActivity) getActivity();

        // inizializza l'handler ai controlli
        torrentsListView = view.findViewById(R.id.LVW___TORRENTSLIST___MAIN);
        mainInfoTextView = view.findViewById(R.id.TXV___TORRENTSVIEWER_INFO);

        ImageButton addTorrentButton = view.findViewById(R.id.BTN___TORRENTSVIEWER_ADDTORRENT);
        addTorrentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                parent.torrentAddRequest();

            }
        });

        ImageButton refreshButton = view.findViewById(R.id.BTN___TORRENTSVIEWER_REFRESH);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                parent.requestTorrentsData();

            }
        });

        refreshButton.callOnClick();

        viewCreated = true;
        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

        ImageButton addTorrentButton = fragmentView.findViewById(R.id.BTN___TORRENTSVIEWER_ADDTORRENT);
        ImageButton refreshButton = fragmentView.findViewById(R.id.BTN___TORRENTSVIEWER_REFRESH);

        addTorrentButton.setOnClickListener(null);
        refreshButton.setOnClickListener(null);

    }

    public void updateContent(String dataJSON) {

        if (!viewCreated)
            return;

        List<TorrentInfo> torrents = new ArrayList<TorrentInfo>();
        String infoMessage;

        try {

            JSONObject data = new JSONObject(dataJSON);

            JSONObject generalData = data.getJSONObject("TorrentData");

            if (generalData.has("Torrents")) {

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

            Log.e(TAG, "Unable to update the view. JSON data: " + dataJSON);

        }

    }
*/
}
