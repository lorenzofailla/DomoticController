package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FileViewerFragment extends Fragment {

    public String rawDirData;
    public String currentDirName;

    public boolean viewCreated=false;

    private TextView currentDirectoryTextView;
    private ListView currentDirectoryListView;

    private List<FileInfo> fileInfoList;

    public FileListAdapter.FileListAdapterListener fileListAdapterListener;

    public FileViewerFragment() {

    }

    interface FileViewerFragmentListener{

        void onViewCreated();

    }

    FileViewerFragmentListener fileViewerFragmentListener;

    public void setFileViewerFragmentListener(FileViewerFragmentListener listener){

        fileViewerFragmentListener=listener;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fileviewer_list, container, false);

        currentDirectoryTextView = (TextView) view.findViewById(R.id.TXV___FILELIST___FOLDERNAME);
        currentDirectoryListView = (ListView) view.findViewById(R.id.LVW___FILELIST___MAIN);

        updateContent();

        if(fileViewerFragmentListener!=null)
            fileViewerFragmentListener.onViewCreated();

        viewCreated=true;
        return view;

    }


    public void updateContent(){

        if (rawDirData!=null) {

            currentDirectoryTextView.setText(currentDirName);

            fileInfoList = refreshFilesList();

            FileListAdapter fileListAdapter = new FileListAdapter(getContext(), R.layout.fragment_fileviewer, fileInfoList);
            fileListAdapter.setFileListAdapterListener(fileListAdapterListener);
            currentDirectoryListView.setAdapter(fileListAdapter);
            currentDirectoryListView.setEnabled(true);
            currentDirectoryListView.setAlpha(1.0f);
        }

    }

    public void hideContent(){

        currentDirectoryTextView.setText(R.string.PROGRESSSTATUS_INFO___RETRIEVING_DIRECTORY_DATA);
        currentDirectoryListView.setEnabled(false);
        currentDirectoryListView.setAlpha(0.2f);
    }

    public void updateFileTransferProgress(double progress, long bytesTransferred){

        currentDirectoryTextView.setText(String.format("Progress: %f%%, Bytes: %d", progress, bytesTransferred));

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

        private List<FileInfo> refreshFilesList(){

        List<FileInfo> tmpFilesInfos = new ArrayList<>();
        String[] tmpFileStringLines = rawDirData.split("\n");

        /* procedura di generazione */
        for (int i=1; i<tmpFileStringLines.length; i++){

            tmpFilesInfos.add(new FileInfo(currentDirName,tmpFileStringLines[i]));

        }

        return tmpFilesInfos;

    }

}
