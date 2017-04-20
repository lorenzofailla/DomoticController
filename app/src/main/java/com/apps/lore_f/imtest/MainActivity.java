package com.apps.lore_f.imtest;

import android.app.ProgressDialog;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.jivesoftware.smack.SmackException;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.List;

import apps.lore_f.instantmessaging.InstantMessaging;
import apps.lore_f.instantmessaging.InstantMessaging.InstantMessagingListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private ProgressDialog progressDialog;
    private InstantMessaging instantMessaging;

    private String remoteHostName;
    private String remoteUpTime;
    private String torrentInfo;
    private int nOfTorrents;

    private List torrentsList = new ArrayList<TorrentInfo>();

    private InstantMessagingListener instantMessagingListener = new InstantMessagingListener() {

        @Override
        public void onConnected() {

            Log.d(TAG, "connected");
            // nasconde il progress dialog
            progressDialog.dismiss();

            retrieveHostInfo();

        }

        @Override
        public void onMessageReceived(String sender, String messageBody) {

            String command = messageBody.substring(0, 23);
            Log.i(TAG, command);

            switch (command) {

                case "%%%_welcome_message_%%%":

                    remoteHostName = messageBody.substring(23);
                    Log.i(TAG, remoteHostName);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            initiateConnection();
                        }
                    });

                    break;

                case "%%%_uptime__message_%%%":

                    remoteUpTime = messageBody.substring(23);
                    Log.i(TAG, remoteUpTime);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateRemoteUpTime();
                        }
                    });

                    break;

                case "%%%_torrent_list____%%%":


                    torrentInfo = messageBody.substring(23);
                    Log.i(TAG, torrentInfo);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            refreshTorrentInfo();
                        }
                    });

                    break;
            }

        }

    };

    private void initiateConnection() {

        TextView hostNameTextView = (TextView) findViewById(R.id.TXV___MAIN___HOSTNAME);
        hostNameTextView.setText(remoteHostName);

    }

    private void updateRemoteUpTime() {

        TextView remoteUpTimeTextView = (TextView) findViewById(R.id.TXV___MAIN___HOSTUPTIME);
        remoteUpTimeTextView.setText(remoteUpTime);
    }

    private void refreshTorrentInfo() {

        TextView torrentInfoTXV = (TextView) findViewById(R.id.TXV___MAIN___GENERALINFO);
        progressDialog.dismiss();

        String[] responseLines = torrentInfo.split("\n");
        nOfTorrents = responseLines.length-2;

        ListView torrenstListLVW = (ListView) findViewById(R.id.LVW___MAIN___TORRENTSLIST);

        if (nOfTorrents>0){

            // mostra la lista dei torrent
            torrentInfoTXV .setText("Torrents: " + nOfTorrents);
            torrentsList = refreshTorrentsList(responseLines);
            TorrentsListAdapter torrentsListAdapter = new TorrentsListAdapter(this, R.layout.torrents_list_row, torrentsList);
            torrenstListLVW.setAdapter(torrentsListAdapter);

            torrenstListLVW.setVisibility(View.VISIBLE);

        } else {

            // nasconde la lista dei torrent
            torrentInfoTXV .setText("No active Torrent.");
            torrenstListLVW.setVisibility(View.GONE);

        }

    }

    private List<TorrentInfo> refreshTorrentsList(String[] rawServerResponse){

        List<TorrentInfo> tmpTorrentInfos = new ArrayList<>();

        for(int i=1; i<rawServerResponse.length-1; i++){

            tmpTorrentInfos.add(new TorrentInfo(rawServerResponse[i]));

        }

        return tmpTorrentInfos;
    }

    private void retrieveHostInfo() {

        // inizializza, prepara e mostra il progress dialog (verrà chiuso dal callback instantMessagingListener.onConnected
        progressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.PROGRESSDIALOG_INFO___RETRIEVING_INFORMATION));
        progressDialog.show();
        //// TODO: 28/mar/2017 inizializza timeout

        sendIM("Home@lorenzofailla.p1.im", "__requestWelcomeMessage");
        sendIM("Home@lorenzofailla.p1.im", "__listTorrents");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instantMessaging = new InstantMessaging("lorenzofailla.p1.im", "controller", "fornaci12Controller");
        instantMessaging.setInstantMessagingListener(instantMessagingListener);

        // inizializza, prepara e mostra il progress dialog (verrà chiuso dal callback instantMessagingListener.onConnected
        progressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.PROGRESSDIALOG_INFO___CONTACTING_REMOTE_HOST));
        progressDialog.show();

        instantMessaging.connect();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }


    public void onPause(){
        super.onPause();

        instantMessaging.removeInstantMessagingListener();

    }

    private boolean sendIM(String recipient, String message) {

        try {

            instantMessaging.sendMessage(recipient, message);
            return true;

        } catch (XmppStringprepException e) {

            e.printStackTrace();
            return false;

        } catch (SmackException.NotConnectedException e) {

            e.printStackTrace();
            return false;

        } catch (InterruptedException e) {

            e.printStackTrace();
            return false;

        }

    }

}
