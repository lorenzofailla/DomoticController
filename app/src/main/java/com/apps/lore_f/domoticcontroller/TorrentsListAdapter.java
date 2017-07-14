package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 105053228 on 20/apr/2017.
 */

public class TorrentsListAdapter extends ArrayAdapter<TorrentInfo> {

    public TorrentsListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<TorrentInfo> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.torrents_list_row, null);

        TextView torrentNameTXV=(TextView) convertView.findViewById(R.id.TXV___TORRENTSLISTROW___TORRENTNAME);
        TextView torrentStatusTXV = (TextView) convertView.findViewById(R.id.TXV___TORRENTSLISTROW___TORRENTSTATUS);

        TorrentInfo torrentInfo = getItem(position);

        torrentNameTXV.setText(torrentInfo.getName());
        torrentStatusTXV.setText(torrentInfo.getStatus());

        return convertView;

    }

}
