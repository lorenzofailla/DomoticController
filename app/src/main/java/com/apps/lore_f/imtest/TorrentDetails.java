package com.apps.lore_f.imtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import apps.lore_f.instantmessaging.InstantMessaging;

public class TorrentDetails extends AppCompatActivity {

    private String torrentID;

    InstantMessaging instantMessaging;
    InstantMessaging.InstantMessagingListener instantMessagingListener = new InstantMessaging.InstantMessagingListener() {
        @Override
        public void onConnected() {

            // manda un messaggio per richiedere lo stato del torrent identificato da torrentID

        }

        @Override
        public void onMessageReceived(String sender, String messageBody) {



        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torrent_details);



        // recupera l'ID del torrent in questione
// inizializza instantMessaging e il suo listener


    }



}
