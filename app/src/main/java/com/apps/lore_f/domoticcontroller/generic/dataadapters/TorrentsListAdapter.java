package com.apps.lore_f.domoticcontroller.generic.dataadapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.DeviceViewActivity;
import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.generic.dataobjects.TorrentInfo;

import java.util.List;

/**
 * Created by 105053228 on 20/apr/2017.
 */

public class TorrentsListAdapter extends ArrayAdapter<TorrentInfo> {

    private static final String TAG="TorrentsListAdapter";

    public TorrentsListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<TorrentInfo> objects, final DeviceViewActivity parentDeviceViewActivity) {
        super(context, resource, objects);

        parentDVA = parentDeviceViewActivity;

    }

    final DeviceViewActivity parentDVA;

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.row_holder_torrent_element, null);

        /* inizializza l'handler ai vari controlli */
        TextView torrentNameTXV=(TextView) convertView.findViewById(R.id.TXV___TORRENTSLISTROW___TORRENTNAME);
        TextView torrentStatusTXV = (TextView) convertView.findViewById(R.id.TXV___TORRENTSLISTROW___TORRENTSTATUS);
        TextView torrentHaveTXV = (TextView) convertView.findViewById(R.id.TXV___TORRENTSLISTROW___TORRENTHAVE);

        TextView upSpeed = (TextView) convertView.findViewById(R.id.TXV___TORRENTSLISTROW___UPSPEED);
        TextView downSpeed = (TextView) convertView.findViewById(R.id.TXV___TORRENTSLISTROW___DOWNSPEED);

        ImageButton switchStatusBtn = (ImageButton) convertView.findViewById(R.id.BTN___TORRENTSLISTROW___SWITCHSTATUS);
        ImageButton removeTorrentBtn = (ImageButton) convertView.findViewById(R.id.BTN___TORRENTSLISTROW___REMOVETORRENT);

        ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.PBR___TORRENTSLISTROW___TORRENTCOMPLETIONPERC);

        final TorrentInfo torrentInfo = getItem(position);

        // imposta la visualizzazione degli elementi
        torrentNameTXV.setText(torrentInfo.getName());
        torrentStatusTXV.setText(torrentInfo.getStatus());

        String torrentData = new StringBuilder().append(torrentInfo.getDone())
                .append(" - ")
                .append(torrentInfo.getHave())
                .append(" (")
                .append(torrentInfo.getEta())
                .append(").")
                .toString();


        torrentHaveTXV.setText(torrentData);

        upSpeed.setText(String.format("%.1f kb/s",torrentInfo.getUp()));
        downSpeed.setText(String.format("%.1f kb/s",torrentInfo.getDown()));

        try {

            int completed = Integer.parseInt(torrentInfo.getDone().replace("*", "").replace("%", "").trim());
            progressBar.setIndeterminate(false);
            progressBar.setProgress(completed);

        } catch (java.lang.NumberFormatException e) {

            progressBar.setIndeterminate(true);

        }

        // imposta l'immagine del pulsante ImageButton switchStatusBtn in funzione dello stato
        if (torrentInfo.getStatus().equals(TorrentInfo.STATUS_STOPPED)){

            // torrent fermo, assegna l'immagine play
            switchStatusBtn.setImageResource(R.drawable.play);

        } else {

            // torrent non fermato, assegna l'immagine pause
            switchStatusBtn.setImageResource(R.drawable.pause);
        }

        // assegna i listener ai pulsanti ImageButton
        switchStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (torrentInfo.getStatus().equals(TorrentInfo.STATUS_STOPPED)){

                    parentDVA.torrentStartRequest(torrentInfo.getId());

                } else{

                    parentDVA.torrentStopRequest(torrentInfo.getId());

                }

            }

        });

        removeTorrentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                parentDVA.torrentRemoveRequest(torrentInfo.getId());

            }

        });

        return convertView;

    }

}
