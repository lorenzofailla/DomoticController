package com.apps.lore_f.domoticcontroller;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;

import apps.lore_f.instantmessaging.InstantMessaging;
import apps.lore_f.instantmessaging.InstantMessaging.InstantMessagingListener;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String HOME_ADDRESS="lorenzofailla-home@alpha-labs.net";
    private static final String TAG = "MainActivity";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient googleApiClient;

    // Firebase Auth
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    /*private ProgressDialog progressDialog;*/
    private InstantMessaging instantMessaging;

    private String remoteHostName;
    private String remoteUpTime;

    private TextView generalInfoTextView;

    private FileViewerFragment fileViewerFragment;
    private TorrentViewerFragment torrentViewerFragment;
    private ConnectionFragment connectionFragment;

    private String pendingDownloadFileName;

    private boolean connectionInProgressFlag=false;
    private boolean disconnectIfConnected=false;

    private Handler handler = new Handler();

    private Runnable hostReplyTimeout = new Runnable() {
        @Override
        public void run() {

            /* messaggio utente */
            AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext())
                    .setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setNegativeButton(R.string.ALERTDIALOG_NO, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).create();

        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch(v.getId()){

                case R.id.BTN___MAIN___SHUTDOWN:

                    shutdownHost();

                    break;

                case R.id.BTN___MAIN___REBOOT:

                    rebootHost();

                    break;

                case R.id.BTN___MAIN___FILEMANAGER:

                    startFileManager();

                    break;

                case R.id.BTN___MAIN___TORRENTMANAGER:

                    startTorrentManager();

                    break;

            }

        }

    };

    private TorrentsListAdapter.TorrentsListAdapterListener torrentsListAdapterListener = new TorrentsListAdapter.TorrentsListAdapterListener() {
        @Override
        public void onStartRequest(int torrentID) {

            //Toast.makeText(MainActivity.this, "Ricevuta richiesta di start per ID:"+ torrentID, Toast.LENGTH_SHORT).show();

            /* invia il commando all'host remoto via IM */
            sendIM(HOME_ADDRESS, "__start_torrent:::"+torrentID);

        }

        @Override
        public void onStopRequest(int torrentID) {

            //Toast.makeText(MainActivity.this, "Ricevuta richiesta di stop per ID:"+ torrentID, Toast.LENGTH_SHORT).show();

            /* invia il commando all'host remoto via IM */
            sendIM(HOME_ADDRESS, "__stop_torrent:::"+torrentID);

        }

        @Override
        public void onRemoveRequest(final int torrentID) {

            removeTorrent(torrentID);

        }

    };

    private FileViewerFragment.FileViewerFragmentListener fileViewerFragmentListener = new FileViewerFragment.FileViewerFragmentListener() {
        @Override
        public void onItemSelected(FileInfo fileInfo) {

            if(fileInfo.getFileInfoType()== FileInfo.FileInfoType.TYPE_FILE){

                /* attiva la procedura di download del file */
                pendingDownloadFileName=fileInfo.getFileName();
                sendIM(HOME_ADDRESS, "__get_file:::"+fileInfo.getFileRoorDir()+"/"+fileInfo.getFileName());

            } else {

                /* è stata selezionata un'entità diversa da un file (directory, '.' o '..') */

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

                sendIM(HOME_ADDRESS, "__get_directory_content:::"+fileViewerFragment.currentDirName);

            }

        }

    };

    private void updateGeneralInfoTextView (final String message){

        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        generalInfoTextView.setText(message);
                    }
                }
        );

    }

    private void updateGeneralInfoTextView (final int messageRId){

        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        generalInfoTextView.setText(messageRId);
                    }
                }
        );

    }

    private InstantMessagingListener instantMessagingListener = new InstantMessagingListener() {

        @Override
        public void onConnected() {

            Log.d(TAG, "connected");

            /* modifica il l'aspetto del fragment */
            if(connectionFragment!=null){

                connectionFragment.currentAction=getString(R.string.PROGRESSSTATUS_INFO___LOGGING_IN_TO_XMPP_SERVER);
                connectionFragment.connectionStatus= ConnectionFragment.ConnectionStatus.IN_PROGRESS;
                connectionFragment.updateView();

            }

            instantMessaging.login();

        }

        @Override
        public void onDisconnected() {

            lockControls();

            /* modifica il l'aspetto del fragment */
            if(connectionFragment!=null){

                connectionFragment.connectionStatus= ConnectionFragment.ConnectionStatus.NOT_CONNECTED;
                connectionFragment.updateView();

            }

        }

        @Override
        public void onConnectionError(Exception e) {

            /* modifica l'aspetto del fragment */
            if(connectionFragment!=null){

                connectionFragment.connectionStatus= ConnectionFragment.ConnectionStatus.NOT_CONNECTED;
                connectionFragment.updateView();

            }

        }

        @Override
        public void onLogIn() {

            Log.d(TAG, "logged in");

            /* modifica il l'aspetto del fragment */
            if(connectionFragment!=null){

                connectionFragment.currentAction = getString(R.string.PROGRESSSTATUS_INFO___CREATINGFILETRANSFERMANAGER);
                connectionFragment.connectionStatus = ConnectionFragment.ConnectionStatus.IN_PROGRESS;
                connectionFragment.updateView();

            }

            instantMessaging.createChat();

        }

        @Override
        public void onChatCreated() {

            Log.d(TAG, "chat created");

           /* modifica l'aspetto del fragment */
            if(connectionFragment!=null){

                connectionFragment.currentAction = getString(R.string.PROGRESSSTATUS_INFO___CREATINGFILETRANSFERMANAGER);
                connectionFragment.connectionStatus= ConnectionFragment.ConnectionStatus.IN_PROGRESS;
                connectionFragment.updateView();

            }

            instantMessaging.createFileTransferManager();

        }

        @Override
        public void onFileTransferManagerCreated() {

            Log.d(TAG, "file transfer manager created");

            /* modifica l'aspetto del fragment */
            if(connectionFragment!=null){

                connectionFragment.currentAction = getString(R.string.PROGRESSSTATUS_INFO___HANDSHAKING);
                connectionFragment.connectionStatus= ConnectionFragment.ConnectionStatus.IN_PROGRESS;
                connectionFragment.updateView();

            }

            retrieveHostInfo();
        }

        @Override
        public void onMessageReceived(String sender, String messageBody) {

            Log.i(TAG, messageBody);
            String command = messageBody.substring(0, 23);

            switch (command) {

                case "%%%_welcome_message_%%%":

                    remoteHostName = messageBody.substring(23);
                    Log.i(TAG, remoteHostName);

                    /* modifica il l'aspetto del fragment */
                    if(connectionFragment!=null){
                        connectionFragment.currentAction=getString(R.string.PROGRESSSTATUS_INFO___CONNECTED_TO_HOST).replace("%hostname%", remoteHostName);
                        connectionFragment.connectionStatus= ConnectionFragment.ConnectionStatus.READY;
                        connectionFragment.updateView();

                    }

                    releaseControls();
                    break;

                case "%%%_uptime__message_%%%":

                    remoteUpTime = messageBody.substring(23);
                    updateRemoteUpTime();

                    break;

                case "%%%_torrent_list____%%%":

                    Log.i(TAG,messageBody.substring(23));

                    torrentViewerFragment.nOfTorrents = messageBody.substring(23).split("\n").length-2;

                    if(torrentViewerFragment.nOfTorrents>0) {

                        updateGeneralInfoTextView(torrentViewerFragment.nOfTorrents + " " +getString(R.string.PROGRESSSTATUS_INFO___ACTIVE_TORRENTS));
                        torrentViewerFragment.rawTorrentDataLines = messageBody.substring(23).split("\n");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                torrentViewerFragment.updateContent();
                            }
                        });


                    } else {

                        updateGeneralInfoTextView(R.string.PROGRESSSTATUS_INFO___NO_ACTIVE_TORRENT);
                        torrentViewerFragment.rawTorrentDataLines = null;
                        torrentViewerFragment.hideContent();

                    }

                    break;


                case "%%%_home_directory__%%%":

                    fileViewerFragment.currentDirName = messageBody.substring(23).replace("\n","");
                    sendIM(HOME_ADDRESS, "__get_directory_content:::"+fileViewerFragment.currentDirName);
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

                case "%%%_command_reply___%%%":
                    Log.i(TAG,messageBody.substring(23));
                    break;

                case "%%%_torrent_started_%%%":
                case "%%%_torrent_stopped_%%%":
                case "%%%_torrent_removed_%%%":
                case "%%%_torrent_added___%%%":
                    startTorrentManager();

                    break;



            }

        }

        @Override
        public void onFileTransferRequest(FileTransferRequest request) {

            /* inizializza il file su cui verrà effettuato il download della risorsa remota */
            File downloadDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Raspberry-pi remote");
            if (!downloadDirectory.exists()){
                downloadDirectory.mkdir();
            }

            File downloadFileResource = new File(downloadDirectory.getPath() + File.separator + pendingDownloadFileName);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    fileViewerFragment.hideContent();

                }
            });

            instantMessaging.acceptFileTransfer(downloadFileResource);

        }

        @Override
        public void onFileTranferUpdate(double progress, long bytesWritten) {

            final double progressToShow = progress;
            final long bytesWrittenToShow = bytesWritten;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    fileViewerFragment.updateFileTransferProgress(progressToShow, bytesWrittenToShow);

                }
            });

        }

        @Override
        public void onFileTransferCompleted() {


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    fileViewerFragment.updateContent();

                }
            });

        }

        @Override
        public void onFileTransferCompletedWithError() {

        }

    };


    private void updateHostName() {

        TextView hostNameTextView = (TextView) findViewById(R.id.TXV___MAIN___HOSTNAME);
        hostNameTextView.setText(remoteHostName);

    }

    private void updateRemoteUpTime() {

        updateGeneralInfoTextView(remoteUpTime);

    }

    private void retrieveHostInfo() {

        /* invia un instant message con il messaggio di benvenuto */
        sendIM(HOME_ADDRESS, "__requestWelcomeMessage");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addApi(AppIndex.API)
                .build();

        /* controlla l'auth Firebase */
        // inizializza il FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();
        // ottiene l'user corrente
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            // autenticazione non effettuata

            // lancia la SignInActivity e termina l'attività corrente
            startActivity(new Intent(this, GoogleSignInActivity.class));
            finish();
            return;

        }

        /* disabilita i controlli */
        lockControls();

        /* inizializza l'handler ai controlli */
        findViewById(R.id.BTN___MAIN___SHUTDOWN).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___MAIN___REBOOT).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___MAIN___FILEMANAGER).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___MAIN___RECONNECT).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___MAIN___TORRENTMANAGER).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___MAIN___SSH_LINK_REFRESH).setOnClickListener(onClickListener);

        generalInfoTextView =(TextView) findViewById(R.id.TXV___MAIN___GENERALINFO);

        // inizializza una nuova istanza di InstantMessaging
        instantMessaging = new InstantMessaging("alpha-labs.net", "lorenzofailla-controller", "fornaci12Controller", "authorized-controller");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainactivity_menu, menu);

        return true;

    }

    private void startFileManager(){

        fileViewerFragment = new FileViewerFragment();
        fileViewerFragment.setFileViewerFragmentListener(fileViewerFragmentListener);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.VIE___MAIN___SUBVIEW, fileViewerFragment);

        fragmentTransaction.commit();

        /* aggiorna la text view */
        updateGeneralInfoTextView(R.string.PROGRESSSTATUS_INFO___RETRIEVING_DIRECTORY_DATA);

        /* invia un instant message con la richiesta della directorry iniziale */
        sendIM(HOME_ADDRESS, "__get_homedir");

    }

    private void startTorrentManager(){

        torrentViewerFragment = new TorrentViewerFragment();
        torrentViewerFragment.localTorrentsListAdapterListener = torrentsListAdapterListener;
        torrentViewerFragment.setTorrentsViewerListener(new TorrentViewerFragment.TorrentsViewerListener() {
            @Override
            public void onAddTorrentRequest() {

                addTorrent();

            }

        });

        showFragment(torrentViewerFragment);

        /* aggiorna la text view */
        updateGeneralInfoTextView(R.string.PROGRESSSTATUS_INFO___RETRIEVING_TORRENT_DATA);

        /* invia un instant message con la richiesta della lista dei torrents */
        sendIM(HOME_ADDRESS, "__listTorrents");

    }

    private void startConnectionFragment(){

        /* associa il listener all'InstantMessaging */
        instantMessaging.setInstantMessagingListener(instantMessagingListener);

        /* inizializza un'istanza della classe ConnectionFragment */
        connectionFragment = new ConnectionFragment();

        connectionFragment.setConnectionFragmentListener(new ConnectionFragment.ConnectionFragmentListener() {

            @Override
            public void onViewCreated(ConnectionFragment fragment) {

                /* modifica l'aspetto del fragment */
                fragment.currentAction=getString(R.string.PROGRESSSTATUS_INFO___CONNECTING_TO_XMPP_SERVER);
                fragment.connectionStatus = ConnectionFragment.ConnectionStatus.IN_PROGRESS;
                fragment.updateView();

                /* inizializza la connessione */
                instantMessaging.connect();

            }

            @Override
            public void onConnectRequest() {

            }

            @Override
            public void onReconnectRequest() {

            }

        });

        showFragment(connectionFragment);


    }

    private void showFragment(Fragment fragment) {

        /* mostra il fragment passato in argomento */
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.VIE___MAIN___SUBVIEW, fragment);

        fragmentTransaction.commit();
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
        googleApiClient.connect();
        AppIndex.AppIndexApi.start(googleApiClient, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(googleApiClient, getIndexApiAction());
        googleApiClient.disconnect();
    }

    @Override
    public void onResume() {

        super.onResume();

        startConnectionFragment();

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

    private void sendIM(String recipient, String message) {

        try {

            instantMessaging.sendMessage(recipient, message);

        } catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException e) {

            Toast.makeText(this, e.getMessage(),Toast.LENGTH_SHORT).show();

        }

    }

    private void releaseControls(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                /* abilita */
                findViewById(R.id.BTN___MAIN___SHUTDOWN).setEnabled(true);
                findViewById(R.id.BTN___MAIN___REBOOT).setEnabled(true);
                findViewById(R.id.BTN___MAIN___FILEMANAGER).setEnabled(true);
                findViewById(R.id.BTN___MAIN___RECONNECT).setEnabled(true);
                findViewById(R.id.BTN___MAIN___TORRENTMANAGER).setEnabled(true);
                findViewById(R.id.BTN___MAIN___SSH_LINK_REFRESH).setEnabled(true);

            }
        });


    }

    private void lockControls(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                /* disabilita */
                findViewById(R.id.BTN___MAIN___SHUTDOWN).setEnabled(false);
                findViewById(R.id.BTN___MAIN___REBOOT).setEnabled(false);
                findViewById(R.id.BTN___MAIN___FILEMANAGER).setEnabled(false);
                findViewById(R.id.BTN___MAIN___RECONNECT).setEnabled(false);
                findViewById(R.id.BTN___MAIN___TORRENTMANAGER).setEnabled(false);
                findViewById(R.id.BTN___MAIN___SSH_LINK_REFRESH).setEnabled(false);
            }

        });

    }

    private void rebootHost(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Add the buttons
        builder.setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button

                /* invia un instant message con il comando di reboot */
                sendIM(HOME_ADDRESS, "__reboot");
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
                sendIM(HOME_ADDRESS, "__shutdown");
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.sign_out_menuEntry:

                // è stato selezionata l'opzione di sign out dal menu
                firebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(googleApiClient);

                startActivity(new Intent(this, GoogleSignInActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void removeTorrent(final int id){

        new AlertDialog.Builder(this)
                .setMessage(R.string.ALERTDIALOG_MESSAGE_CONFIRM_TORRENT_REMOVAL)
                .setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendIM(HOME_ADDRESS, "__remove_torrent:::"+id);
                    }
                })
                .setNegativeButton(R.string.ALERTDIALOG_NO, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setTitle(R.string.ALERTDIALOG_TITLE_CONFIRM_TORRENT_REMOVAL)
                .create()
                .show();

    }

    private void addTorrent(){

        final EditText torrentURL = new EditText(this);


        new AlertDialog.Builder(this)
                .setMessage(R.string.ALERTDIALOG_MESSAGE_ADD_TORRENT)
                .setView(torrentURL)
                .setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //Toast.makeText(getApplicationContext(),torrentURL.getText().toString(),Toast.LENGTH_SHORT ).show();
                        sendIM(HOME_ADDRESS, "__add_torrent:::"+torrentURL.getText().toString());
                    }
                })
                .setNegativeButton(R.string.ALERTDIALOG_NO, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setTitle(R.string.ALERTDIALOG_TITLE_ADD_TORRENT)
                .create()
                .show();

    }

}
