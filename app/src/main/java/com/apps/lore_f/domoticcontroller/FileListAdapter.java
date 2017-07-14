package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

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
        ImageView fileTypeIVW = (ImageView)  convertView.findViewById(R.id.IVW___FILELISTROW___FILETYPEICON);

        final FileInfo fileInfo = getItem(position);

        fileNameTXV.setText(fileInfo.getFileName());
        fileSizeTXV.setText(fileInfo.getFileSizeString());

        if(fileInfo.getFileInfoType() == FileInfo.FileInfoType.TYPE_DIRECTORY){

            fileTypeIVW.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.open_folder));

        } else if (fileInfo.getFileInfoType() == FileInfo.FileInfoType.TYPE_FILE) {

            fileTypeIVW.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.file));

        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(fileListAdapterListener!=null) fileListAdapterListener.onItemSelected(fileInfo);

            }
        });

        return convertView;

    }

    interface FileListAdapterListener{
        void onItemSelected(FileInfo fileInfo);
    }

    FileListAdapterListener fileListAdapterListener;

    public void setFileListAdapterListener(FileListAdapterListener listener){

        fileListAdapterListener=listener;

    }

}


