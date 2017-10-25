package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static android.view.View.GONE;

/**
 * Created by lore_f on 04/06/2017.
 */

public class FileListAdapter extends ArrayAdapter<FileInfo> {

        public FileListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<FileInfo> objects, final DeviceViewActivity parentDeviceViewActivity) {
        super(context, resource, objects);

            parentDVA = parentDeviceViewActivity;
    }

    final DeviceViewActivity parentDVA;

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.fragment_fileviewer, null);

        TextView fileNameTXV=(TextView) convertView.findViewById(R.id.TXV___FILELISTROW___FILENAME);
        TextView fileSizeTXV = (TextView) convertView.findViewById(R.id.TXV___FILELISTROW___FILESIZE);
        ImageView fileTypeIVW = (ImageView)  convertView.findViewById(R.id.IVW___FILELISTROW___FILETYPEICON);
        ImageButton uplodaFileAsDataSlot = (ImageButton) convertView.findViewById(R.id.BTN___FILELISTROW___UPLOADASDATASLOT);

        final FileInfo fileInfo = getItem(position);

        fileNameTXV.setText(fileInfo.getFileName());
        fileSizeTXV.setText(fileInfo.getFileSizeString());

        if(fileInfo.getFileInfoType() == FileInfo.FileInfoType.TYPE_DIRECTORY){

            fileTypeIVW.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.directory));
            uplodaFileAsDataSlot.setVisibility(GONE);

        } else if (fileInfo.getFileInfoType() == FileInfo.FileInfoType.TYPE_FILE) {

            fileTypeIVW.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.file));


            uplodaFileAsDataSlot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parentDVA.uploadAsDataSlot(fileInfo);

                }
            });

        }



        return convertView;

    }

}


