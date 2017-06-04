package com.apps.lore_f.imtest;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

/**
 * Created by lore_f on 04/06/2017.
 */

public class FileListAdapter extends ArrayAdapter<FileInfo> {

    public FileListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<FileInfo> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.fragment_fileviewer, null);

        TextView fileNameTXV=(TextView) convertView.findViewById(R.id.TXV___FILELISTROW___FILENAME);
        TextView fileSizeTXV = (TextView) convertView.findViewById(R.id.TXV___FILELISTROW___FILESIZE);

        FileInfo fileInfo = getItem(position);

        fileNameTXV.setText(fileInfo.getFileName());
        fileSizeTXV.setText(fileInfo.getFileSizeString());

        return convertView;

    }

}


