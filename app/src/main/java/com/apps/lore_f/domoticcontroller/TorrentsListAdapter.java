package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 105053228 on 20/apr/2017.
 */

public class TorrentsListAdapter extends ArrayAdapter<TorrentInfo> {

    public TorrentsListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<TorrentInfo> objects) {
        super(context, resource, objects);
    }

    interface TorrentsListAdapterListener{

        void onStartRequest(int torrentID);
        void onStopRequest(int torrentID);
        void onRemoveRequest(int torrentID);

    }

    TorrentsListAdapterListener torrentsListAdapterListener;

    public void setTorrentsListAdapterListener(TorrentsListAdapterListener listener){
        torrentsListAdapterListener=listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.torrents_list_row, null);

        /* inizializza l'handler ai vari controlli */
        TextView torrentNameTXV=(TextView) convertView.findViewById(R.id.TXV___TORRENTSLISTROW___TORRENTNAME);
        TextView torrentStatusTXV = (TextView) convertView.findViewById(R.id.TXV___TORRENTSLISTROW___TORRENTSTATUS);
        TextView torrentHaveTXV = (TextView) convertView.findViewById(R.id.TXV___TORRENTSLISTROW___TORRENTHAVE);

        ImageButton switchStatusBtn = (ImageButton) convertView.findViewById(R.id.BTN___TORRENTSLISTROW___SWITCHSTATUS);
        ImageButton removeTorrentBtn = (ImageButton) convertView.findViewById(R.id.BTN___TORRENTSLISTROW___DELETETORRENT);

        final TorrentInfo torrentInfo = getItem(position);

        torrentNameTXV.setText(torrentInfo.getName());
        torrentStatusTXV.setText(torrentInfo.getStatusString());
        torrentHaveTXV.setText(torrentInfo.getDone() + " - " + torrentInfo.getHave());

        /* imposta l'immagine del pulsante ImageButton switchStatusBtn in funzione dello stato */
        if (torrentInfo.getStatus()== TorrentInfo.TorrentStatus.STOPPED){

            // torrent fermo, assegna l'immagine play
            switchStatusBtn.setImageResource(R.drawable.ic_media_play_dark);

        } else {

            // torrent non fermato, assegna l'immagine pause
            switchStatusBtn.setImageResource(R.drawable.ic_media_pause_dark);
        }


        /* assegna i listener ai pulsanti ImageButton */
        switchStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (torrentInfo.getStatus()== TorrentInfo.TorrentStatus.STOPPED){

                    if (torrentsListAdapterListener!=null) torrentsListAdapterListener.onStartRequest(torrentInfo.getID());

                } else{

                    if (torrentsListAdapterListener!=null) torrentsListAdapterListener.onStopRequest(torrentInfo.getID());

                }

            }

        });

        removeTorrentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (torrentsListAdapterListener!=null) torrentsListAdapterListener.onRemoveRequest(torrentInfo.getID());

            }

        });


        return convertView;

    }

}
