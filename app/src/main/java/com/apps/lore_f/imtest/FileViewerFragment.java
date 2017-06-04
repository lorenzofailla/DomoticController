package com.apps.lore_f.imtest;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FileViewerFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    List<FileInfo> fileInfoList;

    public FileViewerFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fileviewer_list, container, false);

        ListView filesListLVW = (RecyclerView) view.findViewById(R.id.LVW___MAIN___TORRENTSLIST);

        if (nOfTorrents>0){

            // mostra la lista dei torrent
            torrentInfoTXV .setText("Torrents: " + nOfTorrents);
            torrentsList = refreshTorrentsList(responseLines);
            TorrentsListAdapter torrentsListAdapter = new TorrentsListAdapter(this, R.layout.torrents_list_row, torrentsList);
            torrenstListLVW.setAdapter(torrentsListAdapter);


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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(FileInfo item);
    }

    private List<TorrentInfo> refreshFilesList(String[] rawServerResponse){

        List<FileInfo> tmpFilesInfos = new ArrayList<>();

        /* procedura di generazione */

        return tmpFilesInfos;

    }

}
