package com.apps.lore_f.domoticcontroller;

import android.app.Notification;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

public class DeviceViewActivity extends AppCompatActivity {

    // Firebase Database
    private DatabaseReference incomingMessages;

    public String remoteDeviceName;
    private boolean remoteDeviceTorrent;
    private boolean remoteDeviceDirNavi;
    private boolean remoteDeviceZoneMinder;

    public String thisDevice = "lorenzofailla-g3"; // TODO: 13-Sep-17 deve diventare un parametro di configurazione
    private long replyTimeoutConnection = 15000L; // ms // TODO: 20-Sep-17 deve diventare un parametro di configurazione
    private long replyTimeoutBase = 2 * 60000L; // ms // TODO: 20-Sep-17 deve diventare un parametro di configurazione
    private long zmReplyTimeout = 30000L; // ms

    private Handler handler;

    private ProgressDialog connectionProgressDialog;
    private ProgressDialog zmProgressDialog;

    private long lastOnlineReply;
    private static final String LAST_ONLINE_REPLY = "lastOnlineReply";

    // Fragments
    private DeviceInfoFragment deviceInfoFragment;
    private TorrentViewerFragment torrentViewerFragment;
    private FileViewerFragment fileViewerFragment;
    private ZoneMinderControlFragment zoneMinderControlFragment;
    private WakeOnLanFragment wakeOnLanFragment;

    // Runnable per chiudere l'Activity in caso il dispositivo non risponda alle chiamate entro il timeout
    private Runnable watchDog = new Runnable() {

        @Override
        public void run() {

            manageRemoteDeviceNotResponding();

        }

    };

    // Runnable per chiudere l'Activity in caso il dispositivo non risponda alle chiamate entro il timeout
    private Runnable sendWelcomeMessage = new Runnable() {

        @Override
        public void run() {

            // mostra il nome del dispositivo remoto nella TextView 'remoteHostName'
            TextView remoteHostName = (TextView) findViewById(R.id.TXV___DEVICEVIEW___HOSTNAME);
            remoteHostName.setText(R.string.DEVICEVIEW_REFRESHING_DEVICE_CONNECTION);

            // inizia la connessione al dispositivo: invia la richiesta iniziale
            sendCommandToDevice(
                    new Message(
                            "__requestWelcomeMessage",
                            "null",
                            thisDevice
                    )
            );

            handler.postDelayed(watchDog, replyTimeoutConnection);

        }

    };

    private Runnable zoneMinderTimeOut = new Runnable() {
        @Override
        public void run() {

            manageZoneMinderTimeOut();

        }

    };

    // Listener per nuovi record nel nodo dei messaggi in ingresso.
    private ChildEventListener newCommandsToProcess = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            // è arrivato un nuovo messaggio

            // recupera il nuovo messaggio nel formato della classe Message
            GenericTypeIndicator<Message> msg = new GenericTypeIndicator<Message>() {
            };
            Message incomingMessage = dataSnapshot.getValue(msg);

            // dorme per 10 ms - necessario per evitare timestamp identici negli ID delle risposte
            try {

                Thread.sleep(10);

            } catch (InterruptedException e) {

                // nessuna operazione

            }

            // processa il messaggio ricevuto
            processIncomingMessage(incomingMessage, dataSnapshot.getKey());

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_view);

        // recupera l'extra dall'intent,
        // ottiene il nome del dispositivo remoto e altri parametri

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            remoteDeviceName = extras.getString("__DEVICE_TO_CONNECT");
            remoteDeviceTorrent = extras.getBoolean("__HAS_TORRENT_MANAGEMENT");
            remoteDeviceDirNavi = extras.getBoolean("__HAS_DIRECTORY_NAVIGATION");
            remoteDeviceZoneMinder = extras.getBoolean("__HAS_ZONEMINDER_MANAGEMENT");

        } else {

            finish();
            return;

        }

        // recupera i dati dalla sessione salvata
        if (savedInstanceState != null) {

            // recupera il tempo dell'ultima risposta online
            lastOnlineReply = savedInstanceState.getLong(LAST_ONLINE_REPLY);

        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save the user's current game state
        savedInstanceState.putLong(LAST_ONLINE_REPLY, lastOnlineReply);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void showDeviceInfo() {

        // mostra il Fragment 'deviceInfoFragment'
        deviceInfoFragment = new DeviceInfoFragment();
        deviceInfoFragment.parent = this;

        showFragment(deviceInfoFragment);

        // invia al dispositivo remoto il comando per avere l'uptime
        sendCommandToDevice(
                new Message(
                        "__requestUpTime",
                        "null",
                        thisDevice
                )
        );

        // invia al dispositivo remoto il comando per avere lo spazio disponibile
        sendCommandToDevice(
                new Message(
                        "__requestFreeSpace",
                        "null",
                        thisDevice
                )
        );

    }

    @Override
    protected void onResume() {

        super.onResume();

        // assegna gli OnClickListener ai pulsanti
        findViewById(R.id.BTN___DEVICEVIEW___DEVICEINFO).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___DEVICEVIEW___FILEMANAGER).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___DEVICEVIEW___TORRENTMANAGER).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___DEVICEVIEW___ZONEMINDER).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___DEVICEVIEW___WAKEONLAN).setOnClickListener(onClickListener);

        // ottiene un riferimento al nodo del database che contiene i messaggi in ingresso
        incomingMessages = FirebaseDatabase.getInstance().getReference("/Users/lorenzofailla/Devices/" + thisDevice + "/IncomingCommands");

        // associa un ChildEventListener al nodo per poter processare i messaggi in ingresso
        incomingMessages.addChildEventListener(newCommandsToProcess);

        // calcola il tempo trascorso dall'ultima risposta online
        if (System.currentTimeMillis() - lastOnlineReply > replyTimeoutBase) {

            // nasconde i controlli
            hideControls();

            // mostra un ProgressDialog
            connectionProgressDialog = new ProgressDialog(this);
            connectionProgressDialog.setIndeterminate(true);
            connectionProgressDialog.setTitle(R.string.DEVICEVIEW_REFRESHING_DEVICE_CONNECTION);
            connectionProgressDialog.show();

        } else {

            releaseControls();

        }

        // attiva il ciclo di richieste
        handler = new Handler();
        handler.postDelayed(sendWelcomeMessage, 0);
    }

    @Override
    protected void onPause() {

        super.onPause();

        // rimuove il ChildEventListener al nodo per poter processare i messaggi in ingresso
        incomingMessages.removeEventListener(newCommandsToProcess);

        // rimuove gli OnClickListener ai pulsanti
        findViewById(R.id.BTN___DEVICEVIEW___DEVICEINFO).setOnClickListener(null);
        findViewById(R.id.BTN___DEVICEVIEW___FILEMANAGER).setOnClickListener(null);
        findViewById(R.id.BTN___DEVICEVIEW___TORRENTMANAGER).setOnClickListener(null);
        findViewById(R.id.BTN___DEVICEVIEW___ZONEMINDER).setOnClickListener(null);
        findViewById(R.id.BTN___DEVICEVIEW___WAKEONLAN).setOnClickListener(null);

        // rimuove i listener ai fragment
        // TODO: 08/10/2017 implementare 

        // rimuove gli eventuali task ritardati sull'handler
        handler.removeCallbacks(sendWelcomeMessage);
        handler.removeCallbacks(watchDog);

    }

    private void processIncomingMessage(Message inMsg, String msgKey) {

        boolean deleteMsg = false;

        switch (inMsg.getHeader()) {

            case "WELCOME_MESSAGE":

                if (connectionProgressDialog != null) {

                    if (connectionProgressDialog.isShowing()) {

                        connectionProgressDialog.dismiss();
                        releaseControls();

                    }

                }

                handler.removeCallbacks(watchDog);
                handler.postDelayed(sendWelcomeMessage, replyTimeoutBase);

                // mostra il nome del dispositivo remoto nella TextView 'remoteHostName'
                TextView remoteHostName = (TextView) findViewById(R.id.TXV___DEVICEVIEW___HOSTNAME);
                remoteHostName.setText(remoteDeviceName);

                // aggiorna il valore del tempo dell'ultima risposta
                lastOnlineReply = System.currentTimeMillis();

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "UPTIME_REPLY":
                // risposta al comando uptime

                // modifica il l'aspetto del fragment
                if (deviceInfoFragment != null) {
                    deviceInfoFragment.upTime = inMsg.getBody().replace("\n", "");

                    if (deviceInfoFragment.viewCreated) deviceInfoFragment.updateView();

                }

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "FREE_SPACE":
                // spazio a disposizione sul dispositivo remoto

                // modifica il l'aspetto del fragment
                if (deviceInfoFragment != null) {
                    deviceInfoFragment.freeSpace = inMsg.getBody().replace("\n", "");

                    if (deviceInfoFragment.viewCreated) deviceInfoFragment.updateView();

                }

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "TORRENT_STARTED":
            case "TORRENT_STOPPED":
            case "TORRENT_REMOVED":
            case "TORRENT_ADDED":

                // invia un instant message con la richiesta della lista dei torrents
                askForTorrentsList();

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "TORRENTS_LIST":

                if (torrentViewerFragment != null) {

                    torrentViewerFragment.nOfTorrents = inMsg.getBody().split("\n").length - 2;

                    if (torrentViewerFragment.nOfTorrents > 0) {

                        torrentViewerFragment.rawTorrentDataLines = inMsg.getBody().split("\n");

                    } else {

                        torrentViewerFragment.nOfTorrents = 0;
                        torrentViewerFragment.rawTorrentDataLines = null;

                    }

                    if (torrentViewerFragment.viewCreated)
                        torrentViewerFragment.updateContent();

                }

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "HOME_DIRECTORY":

                if (fileViewerFragment != null) {

                    fileViewerFragment.currentDirName = inMsg.getBody().replace("\n", "");
                    if (fileViewerFragment.viewCreated)
                        fileViewerFragment.updateContent();

                }

                // invia un instant message con la richiesta del contenuto della directory home ricevuta
                sendCommandToDevice(new Message("__get_directory_content", inMsg.getBody(), thisDevice));

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "DIRECTORY_CONTENT":

                if (fileViewerFragment != null) {

                    fileViewerFragment.rawDirData = inMsg.getBody();
                    if (fileViewerFragment.viewCreated)
                        fileViewerFragment.updateContent();

                } else {

                    startFileManager();

                }

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "GENERIC_NOTIFICATION":

                Notification notification = new NotificationCompat.Builder(this)
                        .setContentTitle("Message from " + inMsg.getReplyto())
                        .setContentText(inMsg.getBody())
                        .setSmallIcon(R.drawable.home)
                        .build();

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "ZONEMINDER_DATA_UPDATED":

                if (zmProgressDialog.isShowing()) {

                    handler.removeCallbacks(zoneMinderTimeOut);
                    zmProgressDialog.dismiss();

                    zoneMinderControlFragment = new ZoneMinderControlFragment();
                    zoneMinderControlFragment.zoneminderDBNode = FirebaseDatabase.getInstance().getReference("/Users/lorenzofailla/Devices/" + remoteDeviceName + "/ZoneMinder");
                    zoneMinderControlFragment.parent = this;

                    showFragment(zoneMinderControlFragment);

                }

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

        }

        // se il flag 'deleteMsg' è stato impostato su true, elimina il messaggio dalla coda
        if (deleteMsg)
            deleteMessage(msgKey);

    }

    private void removeFragmentsLinks() {

        if(fileViewerFragment !=null) fileViewerFragment =null;

    }

    public void sendCommandToDevice(Message command) {

        // ottiene un riferimento al nodo del database che contiene i messaggi in ingresso per il dispositivo remoto selezionato
        DatabaseReference deviceIncomingCommands = FirebaseDatabase.getInstance().getReference("/Users/lorenzofailla/Devices");

        // aggiunge il messaggio al nodo
        deviceIncomingCommands
                .child(remoteDeviceName)
                .child("IncomingCommands")
                .child("" + System.currentTimeMillis())
                .setValue(command);

    }

    private void deleteMessage(String id) {

        // ottiene un riferimento al nodo del database che contiene i messaggi in ingresso per il dispositivo locale
        DatabaseReference deviceIncomingCommands = FirebaseDatabase.getInstance().getReference("/Users/lorenzofailla/Devices");

        // rimuove il messaggio al nodo
        deviceIncomingCommands.child(thisDevice).child("IncomingCommands").child(id).removeValue();

    }

    private void showFragment(Fragment fragment) {

        /* mostra il fragment passato in argomento */
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.VIE___DEVICEVIEW___SUBVIEW, fragment);

        fragmentTransaction.commit();
    }

    private void startTorrentManager() {

        torrentViewerFragment = new TorrentViewerFragment();
        torrentViewerFragment.localTorrentsListAdapterListener = torrentsListAdapterListener;
        torrentViewerFragment.setTorrentsViewerListener(new TorrentViewerFragment.TorrentsViewerListener() {
            @Override
            public void onAddTorrentRequest() {

                addTorrent();

            }

        });

        showFragment(torrentViewerFragment);

        // invia un instant message con la richiesta della lista dei torrents
        askForTorrentsList();
    }

    private void askForTorrentsList() {

        // invia un instant message con la richiesta della lista dei torrents
        sendCommandToDevice(new Message("__listTorrents", "null", thisDevice));

    }

    private void releaseControls() {

        findViewById(R.id.BTN___DEVICEVIEW___DEVICEINFO).setVisibility(View.VISIBLE);

        if (remoteDeviceTorrent) {
            //
            findViewById(R.id.BTN___DEVICEVIEW___TORRENTMANAGER).setVisibility(View.VISIBLE);

        } else {
            //
            findViewById(R.id.BTN___DEVICEVIEW___TORRENTMANAGER).setVisibility(View.GONE);

        }

        if (remoteDeviceDirNavi) {
            //
            findViewById(R.id.BTN___DEVICEVIEW___FILEMANAGER).setVisibility(View.VISIBLE);

        } else {
            //
            findViewById(R.id.BTN___DEVICEVIEW___FILEMANAGER).setVisibility(View.GONE);

        }

        if (remoteDeviceZoneMinder){

            findViewById(R.id.BTN___DEVICEVIEW___ZONEMINDER).setVisibility(View.VISIBLE);

        } else {
            //
            findViewById(R.id.BTN___DEVICEVIEW___ZONEMINDER).setVisibility(View.GONE);

        }

    }

    private void hideControls() {

        findViewById(R.id.BTN___DEVICEVIEW___DEVICEINFO).setVisibility(View.GONE);
        findViewById(R.id.BTN___DEVICEVIEW___TORRENTMANAGER).setVisibility(View.GONE);
        findViewById(R.id.BTN___DEVICEVIEW___FILEMANAGER).setVisibility(View.GONE);
        findViewById(R.id.BTN___DEVICEVIEW___ZONEMINDER).setVisibility(View.GONE);

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.BTN___DEVICEVIEW___DEVICEINFO:

                    showDeviceInfo();

                    break;


                case R.id.BTN___DEVICEVIEW___FILEMANAGER:

                    // invia al dispositivo remoto la richiesta di conoscere la directory corrente
                    sendCommandToDevice(new Message("__get_homedir", "null", thisDevice));

                    break;

                case R.id.BTN___DEVICEVIEW___TORRENTMANAGER:

                    startTorrentManager();

                    break;

                case R.id.BTN___DEVICEVIEW___ZONEMINDER:

                    startZoneMinder();

                    break;

                case R.id.BTN___DEVICEVIEW___WAKEONLAN:

                    startWakeOnLan();

                    break;
            }

        }

    };

    private void startZoneMinder(){

        zmProgressDialog = new ProgressDialog(this);
        zmProgressDialog.setCancelable(true);
        zmProgressDialog.setTitle(R.string.ZMMGM_PD_TITLE_CONNECTING);
        zmProgressDialog.setIndeterminate(true);

        zmProgressDialog.show();

        sendCommandToDevice(new Message("__update_zoneminder_data","null",thisDevice));

        handler.postDelayed(zoneMinderTimeOut, zmReplyTimeout);

    }

    private void startWakeOnLan(){

        wakeOnLanFragment = new WakeOnLanFragment();
        wakeOnLanFragment.parent = this;

        showFragment(wakeOnLanFragment);

    }

    public void rebootHost() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Add the buttons
        builder.setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button

                // invia un instant message con il comando di reboot
                sendCommandToDevice(new Message("__reboot", "null", thisDevice));
                showDeviceInfo();

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

    public void shutdownHost() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Add the buttons
        builder.setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button

                /* invia un instant message con il comando di reboot */
                sendCommandToDevice(new Message("__shutdown", "null", thisDevice));
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

    private void startFileManager() {

        fileViewerFragment = new FileViewerFragment();
        fileViewerFragment.parent = this;

        // mostra il Fragment
        showFragment(fileViewerFragment);

    }

    private void removeTorrent(final int id) {

        new AlertDialog.Builder(this)
                .setMessage(R.string.ALERTDIALOG_MESSAGE_CONFIRM_TORRENT_REMOVAL)
                .setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        sendCommandToDevice(new Message("__remove_torrent", "" + id, thisDevice));

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

    private void addTorrent() {

        final EditText torrentURL = new EditText(this);

        new AlertDialog.Builder(this)
                .setMessage(R.string.ALERTDIALOG_MESSAGE_ADD_TORRENT)
                .setView(torrentURL)
                .setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        sendCommandToDevice(new Message("__add_torrent", torrentURL.getText().toString(), thisDevice));
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

    private TorrentsListAdapter.TorrentsListAdapterListener torrentsListAdapterListener = new TorrentsListAdapter.TorrentsListAdapterListener() {
        @Override
        public void onStartRequest(int torrentID) {

            // invia il commando all'host remoto
            sendCommandToDevice(new Message("__start_torrent", "" + torrentID, thisDevice));

        }

        @Override
        public void onStopRequest(int torrentID) {

            // invia il commando all'host remoto
            sendCommandToDevice(new Message("__stop_torrent", "" + torrentID, thisDevice));
        }

        @Override
        public void onRemoveRequest(final int torrentID) {

            removeTorrent(torrentID);

        }

    };

    private void manageFileViewerFragmentRequest(FileInfo fileInfo) {

            if (fileInfo.getFileInfoType() == FileInfo.FileInfoType.TYPE_FILE) {

                // attiva la procedura di upload del file da parte del dispositivo remoto sulla piattaforma Firebase Storage
                sendCommandToDevice(new Message("__get_file", fileInfo.getFileRoorDir() + "/" + fileInfo.getFileName(), thisDevice));

            } else {

                // è stata selezionata un'entità diversa da un file (directory, '.' o '..')

                if (fileInfo.getFileName().equals(".")) {
                    // è stato selezionato '.'
                    // nessuna modifica a currentDirName

                } else if (fileInfo.getFileName().equals("..") && !fileViewerFragment.currentDirName.equals("/")) {
                    // è stato selezionato '..'

                    // modifica currentDirName per salire al livello di directory superiore

                    String[] directoryArray = fileViewerFragment.currentDirName.split("/");

                    fileViewerFragment.currentDirName = "/";
                    for (int i = 0; i < directoryArray.length - 1; i++) {

                        if (fileViewerFragment.currentDirName.equals("/")) {

                            fileViewerFragment.currentDirName += directoryArray[i];

                        } else {

                            fileViewerFragment.currentDirName += "/" + directoryArray[i];
                        }

                    }

                } else {

                    // è stato selezionata una directory
                    // modifica currentDirName per scendere al livello di directory selezionato
                    if (fileViewerFragment.currentDirName.equals("/")) {
                        fileViewerFragment.currentDirName += fileInfo.getFileName();
                    } else {
                        fileViewerFragment.currentDirName += "/" + fileInfo.getFileName();
                    }

                }

                fileViewerFragment.hideContent();

                // invia il messaggio di richiesta dati della directory al dispositivo remoto
                sendCommandToDevice(new Message("__get_directory_content", fileViewerFragment.currentDirName, thisDevice));

            }

        }


    private void manageRemoteDeviceNotResponding(){


        if (connectionProgressDialog.isShowing()){

            connectionProgressDialog.dismiss();

        }

        // costruisce un AlertDialog e lo mostra a schermo
        new AlertDialog.Builder(this)
                .setTitle(R.string.ALERTDIALOG_TITLE_REMOTE_DEVICE_NOT_RESPONDING)
                .setMessage(R.string.ALERTDIALOG_MESSAGE_REMOTE_DEVICE_NOT_RESPONDING)
                .setPositiveButton(R.string.ALERTDIALOG_GOT_IT, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // aggiorna il database cloud impostando la proprietà online del dispositivo remoto a false
                        finish();
                    }
                })
                .create()
                .show();


    }

    private void manageZoneMinderTimeOut(){

        zmProgressDialog.cancel();

        // costruisce un AlertDialog e lo mostra a schermo
        new AlertDialog.Builder(this)
                .setTitle(R.string.ALERTDIALOG_TITLE_ZM_NOT_RESPONDING)
                .setMessage(R.string.ALERTDIALOG_MESSAGE_ZM_NOT_RESPONDING)
                .setPositiveButton(R.string.ALERTDIALOG_GOT_IT, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }

                })
                .create()
                .show();

    }

    public void uploadAsDataSlot(FileInfo fileInfo){

        sendCommandToDevice(new Message("__upload_file", fileInfo.getFileName(), thisDevice));

    }

}
