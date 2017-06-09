package com.apps.lore_f.imtest;

import android.support.v4.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

    /*private ProgressDialog progressDialog;*/
    private InstantMessaging instantMessaging;

    private String remoteHostName;
    private String remoteUpTime;

    private ImageButton shutdownButton;
    private ImageButton rebootButton;
    private ImageButton startFileManagerButton;

    private TextView generalInfoTextView;

    private FileViewerFragment fileViewerFragment;

    private FileViewerFragment.FileViewerFragmentListener fileViewerFragmentListener = new FileViewerFragment.FileViewerFragmentListener() {
        @Override
        public void onItemSelected(FileInfo fileInfo) {

            if(fileInfo.getFileInfoType()== FileInfo.FileInfoType.TYPE_FILE){

                /* attiva la procedura di download del file */

            } else {

                /* è stata selezionata una directory */

                if(fileInfo.getFileName().equals(".")){
                    /* è stato selezionato '.' */
                    /* nessuna modifica a currentDirName */

                } else if (fileInfo.getFileName().equals("..") && !fileViewerFragment.currentDirName.equals("/")) {
                    /* è stato selezionato '..' */
                    /* modifica currentDirName per salire al livello di directory superiore */

                    String[] directoryArray = fileViewerFragment.currentDirName.split("/");

                    fileViewerFragment.currentDirName = "/";
                    for (int i = 0; i < directoryArray.length-1; i++) {

                        if (fileViewerFragment.currentDirName.equals("/")) {

                            fileViewerFragment.currentDirName += directoryArray[i];

                        } else {

                            fileViewerFragment.currentDirName += "/" + directoryArray[i];
                        }

                    }

                } else {

                    /* è stato selezionata una directory */
                    /* modifica currentDirName per scendere al livello di directory selezionato */
                    if(fileViewerFragment.currentDirName.equals("/")) {
                        fileViewerFragment.currentDirName += fileInfo.getFileName();
                    } else {
                        fileViewerFragment.currentDirName += "/"+fileInfo.getFileName();
                    }

                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        fileViewerFragment.hideContent();

                    }
                });

                sendIM("Home@lorenzofailla.p1.im", "__get_directory_content:::"+fileViewerFragment.currentDirName);

            }

        }

    };


    private InstantMessagingListener instantMessagingListener = new InstantMessagingListener() {

        @Override
        public void onConnected() {

            Log.d(TAG, "connected");

            /* modifica il testo del messaggio nel progressDialog */
            generalInfoTextView.setText(getString(R.string.PROGRESSDIALOG_INFO___CONTACTING_REMOTE_HOST));
            instantMessaging.createChat();

        }

        @Override
        public void onChatCreated() {

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

                            updateHostName();
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

                    break;


                case "%%%_home_directory__%%%":

                    fileViewerFragment.currentDirName = messageBody.substring(23).replace("\n","");
                    sendIM("Home@lorenzofailla.p1.im", "__get_directory_content:::"+fileViewerFragment.currentDirName);
                    break;

                case "%%%_dir_content_____%%%":

                    fileViewerFragment.rawDirData= messageBody.substring(23);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            fileViewerFragment.updateContent();

                        }
                    });


                    break;

            }

        }

    };

    private void updateHostName() {

        TextView hostNameTextView = (TextView) findViewById(R.id.TXV___MAIN___HOSTNAME);
        hostNameTextView.setText(remoteHostName);

    }

    private void updateRemoteUpTime() {

        TextView remoteUpTimeTextView = (TextView) findViewById(R.id.TXV___MAIN___HOSTUPTIME);
        remoteUpTimeTextView.setText(remoteUpTime);

        releaseControls();

    }

    /*
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
    */

    private void retrieveHostInfo() {

        /* invia un instant message con il messaggio di benvenuto */
        sendIM("Home@lorenzofailla.p1.im", "__requestWelcomeMessage");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* inizializza l'handler ai controlli */
        shutdownButton = (ImageButton) findViewById(R.id.BTN___MAIN___SHUTDOWN);
        rebootButton = (ImageButton) findViewById(R.id.BTN___MAIN___REBOOT);
        startFileManagerButton = (ImageButton) findViewById(R.id.BTN___MAIN___FILEMANAGER);
        generalInfoTextView =(TextView) findViewById(R.id.TXV___MAIN___GENERALINFO);

        lockControls();

        shutdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                shutdownHost();

            }
        });

        rebootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                rebootHost();
            }
        });

        startFileManagerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fileViewerFragment = new FileViewerFragment();
                fileViewerFragment.setFileViewerFragmentListener(fileViewerFragmentListener);



                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.VIE___MAIN___SUBVIEW, fileViewerFragment);

                fragmentTransaction.commit();

                /* invia un instant message con il messaggio di benvenuto */
                sendIM("Home@lorenzofailla.p1.im", "__get_homedir");
            }

        });

        // inizializza una nuova istanza di InstantMessaging
        instantMessaging = new InstantMessaging("lorenzofailla.p1.im", "controller", "fornaci12Controller");
        generalInfoTextView.setText(getString(R.string.PROGRESSDIALOG_INFO___CONNECTING_TO_IM_SUPPORT_SERVER));

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

    @Override
    public void onResume() {

        super.onResume();
        instantMessaging.setInstantMessagingListener(instantMessagingListener);

    }


    public void onPause(){
        super.onPause();

        if (instantMessaging!=null) {
            instantMessaging.removeInstantMessagingListener();
        }
    }

    public void onDestroy(){

        super.onDestroy();

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

    private void releaseControls(){

        /* abilita */
        shutdownButton.setEnabled(true);
        rebootButton.setEnabled(true);
        startFileManagerButton.setEnabled(true);
    }

    private void lockControls(){

        /* disabilita */
        shutdownButton.setEnabled(false);
        rebootButton.setEnabled(false);
        startFileManagerButton.setEnabled(false);

    }

    private void rebootHost(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Add the buttons
        builder.setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button

                /* invia un instant message con il comando di reboot */
                sendIM("Home@lorenzofailla.p1.im", "__reboot");
                recreate();

            }

        });
        builder.setNegativeButton(R.string.ALERTDIALOG_NO, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.setMessage(R.string.ALERTDIALOG_MESSAGE_REBOOT);

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void shutdownHost(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Add the buttons
        builder.setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button

                /* invia un instant message con il comando di reboot */
                sendIM("Home@lorenzofailla.p1.im", "__shutdown");
                finish();

            }

        });
        builder.setNegativeButton(R.string.ALERTDIALOG_NO, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.setMessage(R.string.ALERTDIALOG_MESSAGE_SHUTDOWN);

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }



}
