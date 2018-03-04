package com.apps.lore_f.domoticcontroller;

import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DeviceViewActivity extends AppCompatActivity {

    private static final String TAG = "DeviceViewActivity";

    private enum FragmentType {
        DEVICE_INFO,
        DIRECTORY_NAVIGATOR,
        TORRENT_MANAGER,
        WOL_MANAGER,
        SSH_MANAGER,
        CAMERA_VIEWER

    }

    // Firebase Database
    private DatabaseReference incomingMessages;

    public String remoteDeviceName;
    private boolean remoteDeviceTorrent;
    private boolean remoteDeviceDirNavi;
    private boolean remoteDeviceWakeOnLan;
    private boolean remoteDeviceVideoSurveillance;


    public String thisDevice = "lorenzofailla-g3";
    public String groupName;
    private long replyTimeoutConnection = 15000L; // ms // TODO: 20-Sep-17 deve diventare un parametro di configurazione
    private long replyTimeoutBase = 2 * 60000L; // ms // TODO: 20-Sep-17 deve diventare un parametro di configurazione

    private Handler handler;

    private ProgressDialog connectionProgressDialog;
    private ProgressDialog zmProgressDialog;

    private long lastOnlineReply;
    private static final String LAST_ONLINE_REPLY = "lastOnlineReply";

    private CollectionPagerAdapter collectionPagerAdapter;
    private ViewPager viewPager;

    /* Fragments */
    private DeviceInfoFragment deviceInfoFragment;
    private TorrentViewerFragment torrentViewerFragment;
    private FileViewerFragment fileViewerFragment;
    private WakeOnLanFragment wakeOnLanFragment;
    private DeviceSSHFragment deviceSSHFragment;
    private VSCameraViewerFragment[] cameraViewFragment;
    private String[] cameraNames;
    private String[] cameraIDs;
    private int nOfAvailableCameras;
    private int homeFragment;

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            Log.i(TAG,String.format("ViewPager.OnPageChangeListener.onPageSelected(%d)", position));
            collectionPagerAdapter.initializeFragmentAction(position);

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public class CollectionPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] fragments;
        private String[] pageTitle;
        private FragmentType[] fragmentTypes;

        public CollectionPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        public CollectionPagerAdapter(
                FragmentManager fm,
                Fragment[] fragments,
                String[] titles,
                FragmentType[] types
        ) {
            super(fm);

            this.fragments = fragments;
            this.pageTitle = titles;
            this.fragmentTypes = types;

        }


        @Override
        public Fragment getItem(int i) {

            Log.i(TAG, String.format("getItem(%d)",i));
            initializeFragmentAction(i);
            return fragments[i];

        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return pageTitle[position];

        }

        public void initializeFragmentAction(int fragmentPosition){

            switch (fragmentTypes[fragmentPosition]) {

                case DEVICE_INFO:
                    requestDeviceInfo();
                    break;

                case DIRECTORY_NAVIGATOR:

                    if (fileViewerFragment.currentDirName==null) {
                        // invia al dispositivo remoto la richiesta di conoscere la directory corrente
                        sendCommandToDevice(new Message("__get_homedir", "null", thisDevice));
                    }

                    break;

                case TORRENT_MANAGER:
                    requestTorrentsList();
                    break;

                case WOL_MANAGER:
                    /* no action */
                    break;

            }

        }

    }

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

    /*
    Listener per nuovi record nel nodo dei messaggi in ingresso.
     */
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

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {

            // recupera il nome del gruppo [R.string.data_group_name] dalle shared preferences
            Context context = getApplicationContext();
            SharedPreferences sharedPref = context.getSharedPreferences(
                    getString(R.string.data_file_key), Context.MODE_PRIVATE);

            groupName=sharedPref.getString(getString(R.string.data_group_name),null);

            if(groupName==null){

            /*
            questa parte di codice non dovrebbe essere mai eseguita, viene tenuta per evitare eccezioni
             */

                // nome del gruppo non impostato, lancia l'Activity GroupSelection per selezionare il gruppo a cui connettersi
                startActivity(new Intent(this, GroupSelection.class));

                // termina l'Activity corrente
                finish();
                return;

            }

            remoteDeviceName = extras.getString("__DEVICE_TO_CONNECT");

            remoteDeviceTorrent = intent.hasExtra("__HAS_TORRENT_MANAGEMENT") && extras.getBoolean("__HAS_TORRENT_MANAGEMENT");
            remoteDeviceDirNavi = intent.hasExtra("__HAS_DIRECTORY_NAVIGATION") && extras.getBoolean("__HAS_DIRECTORY_NAVIGATION");
            remoteDeviceWakeOnLan = intent.hasExtra("__HAS_WAKEONLAN") &&extras.getBoolean("__HAS_WAKEONLAN");
            remoteDeviceVideoSurveillance = intent.hasExtra("__HAS_VIDEOSURVEILLANCE") && extras.getBoolean("__HAS_VIDEOSURVEILLANCE");

            if(remoteDeviceVideoSurveillance && intent.hasExtra("__CAMERA_NAMES") && intent.hasExtra("__CAMERA_IDS")) {

                try {

                    cameraNames = extras.getString("__CAMERA_NAMES").split(";");
                    cameraIDs = extras.getString("__CAMERA_IDS").split(";");

                    nOfAvailableCameras = cameraIDs.length;

                } catch (NullPointerException e) {

                    remoteDeviceVideoSurveillance=false;
                    nOfAvailableCameras =0;

                }

            } else {

                remoteDeviceVideoSurveillance=false;
                nOfAvailableCameras =0;

            }

            /* inizializza i fragment */

            //
            // DeviceInfoFragment
            deviceInfoFragment = new DeviceInfoFragment();
            deviceInfoFragment.parent = this;

            //
            // VideoSurveillanceCameraListFragment
            if(remoteDeviceVideoSurveillance){

                // crea una query per calcolare il numero di videocamere disponibili
                // inizializza l'array
                cameraViewFragment = new VSCameraViewerFragment[nOfAvailableCameras];

                for(int i=0; i<nOfAvailableCameras; i++){

                    VSCameraViewerFragment temp;
                    temp= new VSCameraViewerFragment();
                    temp.setCameraID(cameraIDs[i]);
                    temp.setCameraName(cameraNames[i]);
                    temp.setParent(this);

                    cameraViewFragment[i] = temp;

                }

            }

            //
            // FileViewerFragment
            if (remoteDeviceDirNavi) {
                fileViewerFragment = new FileViewerFragment();
                fileViewerFragment.parent = this;
            }

            //
            // TorrentViewerFragment
            if (remoteDeviceTorrent) {
                torrentViewerFragment = new TorrentViewerFragment();
                torrentViewerFragment.parent = this;
            }

            //
            // WakeOnLanFragment
            if(remoteDeviceWakeOnLan) {
                wakeOnLanFragment = new WakeOnLanFragment();
                wakeOnLanFragment.parent = this;
            }

        } else {

            finish();
            return;

        }

        // recupera i dati dalla sessione salvata
        if (savedInstanceState != null) {

            // recupera il tempo dell'ultima risposta online
            lastOnlineReply = savedInstanceState.getLong(LAST_ONLINE_REPLY);

        }

        // crea il CollectionPagerAdapter con le pagine video
        collectionPagerAdapter =
                new CollectionPagerAdapter(
                        getSupportFragmentManager(),
                        getAvailableFragments(),
                        getAvailableFragmentTitles(),
                        getAvailableFragmentTypes()
                );

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save the user's current game state
        savedInstanceState.putLong(LAST_ONLINE_REPLY, lastOnlineReply);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void requestDeviceInfo() {

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

        viewPager = (ViewPager) findViewById(R.id.PGR___DEVICEVIEW___MAINPAGER);
        viewPager.setAdapter(collectionPagerAdapter);
        viewPager.addOnPageChangeListener(onPageChangeListener);
        viewPager.setCurrentItem(homeFragment);

        // assegna gli OnClickListener ai pulsanti
        findViewById(R.id.BTN___DEVICEVIEW___DEVICEINFO).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___DEVICEVIEW___FILEMANAGER).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___DEVICEVIEW___TORRENTMANAGER).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___DEVICEVIEW___WAKEONLAN).setOnClickListener(onClickListener);
        findViewById(R.id.BTN___DEVICEVIEW___SSH).setOnClickListener(onClickListener);

        // ottiene un riferimento al nodo del database che contiene i messaggi in ingresso
        incomingMessages = FirebaseDatabase.getInstance().getReference("/Groups/" + groupName + "/Devices/" + thisDevice + "/IncomingCommands");

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
        findViewById(R.id.BTN___DEVICEVIEW___WAKEONLAN).setOnClickListener(null);
        findViewById(R.id.BTN___DEVICEVIEW___SSH).setOnClickListener(null);

        // rimuove l'OnPageChangeListener al ViewPager
        if(viewPager!=null)
            viewPager.removeOnPageChangeListener(onPageChangeListener);

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
                requestTorrentsList();

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "TORRENTS_LIST":

                if (torrentViewerFragment == null) {
                    break;
                }

                // imposta i parametri di visualizzazione del fragment
                torrentViewerFragment.nOfTorrents = inMsg.getBody().split("\n").length - 2;

                if (torrentViewerFragment.nOfTorrents > 0) {

                    torrentViewerFragment.rawTorrentDataLines = inMsg.getBody().split("\n");

                } else {

                    torrentViewerFragment.nOfTorrents = 0;
                    torrentViewerFragment.rawTorrentDataLines = null;

                }

                if (torrentViewerFragment.viewCreated)
                    torrentViewerFragment.updateContent();

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "HOME_DIRECTORY":

                fileViewerFragment.currentDirName = inMsg.getBody().replace("\n", "");
                if (fileViewerFragment.viewCreated)
                    fileViewerFragment.updateContent();

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

            case "FILE_READY_FOR_DOWNLOAD":

                String[] param = {"Groups/" + groupName + "/Devices/" + thisDevice + "/IncomingFiles/"};
                Intent intent = new Intent(this, DownloadFileFromDataSlots.class);
                intent.putExtra("__file_to_download", param);

                startService(intent);

                // valorizza il flag per eliminare il messaggio dalla coda
                deleteMsg = true;

                break;

            case "SSH_SHELL_READY":

                // avvia il fragment
                //startSSHFragment();
                deleteMsg = true;

                break;

        }

        // se il flag 'deleteMsg' è stato impostato su true, elimina il messaggio dalla coda
        if (deleteMsg)
            deleteMessage(msgKey);

    }

    public void sendCommandToDevice(Message command) {

        // ottiene un riferimento al nodo del database che contiene i messaggi in ingresso per il dispositivo remoto selezionato
        DatabaseReference deviceIncomingCommands = FirebaseDatabase.getInstance().getReference("/Groups/" + groupName + "/Devices");

        // aggiunge il messaggio al nodo
        deviceIncomingCommands
                .child(remoteDeviceName)
                .child("IncomingCommands")
                .child("" + System.currentTimeMillis())
                .setValue(command);

    }

    private void deleteMessage(String id) {

        // ottiene un riferimento al nodo del database che contiene i messaggi in ingresso per il dispositivo locale
        DatabaseReference deviceIncomingCommands = FirebaseDatabase.getInstance().getReference("/Groups/" + groupName + "/Devices");

        // rimuove il messaggio al nodo
        deviceIncomingCommands.child(thisDevice).child("IncomingCommands").child(id).removeValue();

    }

    private void requestTorrentsList() {

        // invia un instant message con la richiesta della lista dei torrents
        sendCommandToDevice(new Message("__listTorrents", "null", thisDevice));

    }

    private void releaseControls() {

        /*
        Mostra o nasconde i diversi pulsanti in funzione delle capacità del dispositivio
         */

        // pulsante per le informazioni del dispositivo è sempre visibile
        findViewById(R.id.BTN___DEVICEVIEW___DEVICEINFO).setVisibility(View.VISIBLE);

        // gestione torrent
        if (remoteDeviceTorrent) {
            //
            findViewById(R.id.BTN___DEVICEVIEW___TORRENTMANAGER).setVisibility(View.VISIBLE);

        } else {
            //
            findViewById(R.id.BTN___DEVICEVIEW___TORRENTMANAGER).setVisibility(View.GONE);

        }

        // gestione file manager

        if (remoteDeviceDirNavi) {
            //
            findViewById(R.id.BTN___DEVICEVIEW___FILEMANAGER).setVisibility(View.VISIBLE);

        } else {
            //
            findViewById(R.id.BTN___DEVICEVIEW___FILEMANAGER).setVisibility(View.GONE);

        }

        // wakeonlan
        if (remoteDeviceWakeOnLan) {
            //
            findViewById(R.id.BTN___DEVICEVIEW___WAKEONLAN).setVisibility(View.VISIBLE);

        } else {
            //
            findViewById(R.id.BTN___DEVICEVIEW___WAKEONLAN).setVisibility(View.GONE);

        }

    }

    private void hideControls() {

        /*
        Nasconde la pulsantiera
         */

        findViewById(R.id.BTN___DEVICEVIEW___DEVICEINFO).setVisibility(View.GONE);
        findViewById(R.id.BTN___DEVICEVIEW___TORRENTMANAGER).setVisibility(View.GONE);
        findViewById(R.id.BTN___DEVICEVIEW___FILEMANAGER).setVisibility(View.GONE);
        findViewById(R.id.BTN___DEVICEVIEW___WAKEONLAN).setVisibility(View.GONE);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.BTN___DEVICEVIEW___SSH:

                    // controlla se esiste un nodo nel database con la sessione ssh relativa al dispositivo corrente
                    // se esiste apre subito il fragment
                    // altrimenti invia al dispositivo remoto la richiesta di connessione a una shell SSH

                    // ottiene un riferimento al nodo del database Firebase con le informazioni sulle shell aperte,
                    // effettua una query per filtrare le shell aperte al dispositivo corrente
                    DatabaseReference activeShells = FirebaseDatabase.getInstance().getReference("/Groups/" + groupName + "/Devices/" + remoteDeviceName + "/SSHShells");
                    Query query = activeShells.orderByKey().equalTo(thisDevice);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot != null) {
                                //
                                // non esistono shell aperte
                                // invia al dispositivo remoto la richiesta di connessione a una shell SSH
                                sendCommandToDevice(new Message("__initialize_ssh", null, thisDevice));

                            } else {
                                //
                                // esiste una shell aperta
                                // se il fragment per la gestione delle ssh è già stato attivato lo mostra, altrimenti lo crea
                                //startSSHFragment();

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }

                    });


                    break;
            }

        }

    };


    public void rebootHost() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Add the buttons
        builder.setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button

                // invia un instant message con il comando di reboot
                sendCommandToDevice(new Message("__reboot", "null", thisDevice));

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

    public void torrentAddRequest() {

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


    public void torrentStartRequest(int torrentID) {

        // invia il commando all'host remoto
        sendCommandToDevice(new Message("__start_torrent", "" + torrentID, thisDevice));

    }


    public void torrentStopRequest(int torrentID) {

        // invia il commando all'host remoto
        sendCommandToDevice(new Message("__stop_torrent", "" + torrentID, thisDevice));
    }


    public void torrentRemoveRequest(final int torrentID) {

        removeTorrent(torrentID);

    }

    public void manageFileViewerFragmentRequest(FileInfo fileInfo) {

        if (fileInfo.getFileInfoType() == FileInfo.FileInfoType.TYPE_FILE) {

            // attiva la procedura di upload del file da parte del dispositivo remoto sulla piattaforma Firebase Storage
            sendCommandToDevice(new Message("__get_file", fileInfo.getFileRootDir() + "/" + fileInfo.getFileName(), thisDevice));

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

    private void manageRemoteDeviceNotResponding() {

        if (connectionProgressDialog.isShowing()) {

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

    private void manageZoneMinderTimeOut() {

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

    public void uploadAsDataSlot(FileInfo fileInfo) {

        sendCommandToDevice(new Message("__upload_file", fileInfo.getFileRootDir() + "/" + fileInfo.getFileName(), thisDevice));

    }

    /*
    Metodi e funzioni relativi al fragment SSH

     */
    private void sendSSHDisconnectionRequest() {

        sendCommandToDevice(new Message("__disconnect_ssh", null, thisDevice));

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.i(TAG, "Key: " + keyCode + " Event: " + event.getUnicodeChar(event.getMetaState()));

        switch (keyCode) {

            case 67: // backspace

                if (deviceSSHFragment != null)
                    deviceSSHFragment.sendBackSpace();

                return super.onKeyDown(keyCode, event);

            default:
                if (deviceSSHFragment != null)
                    deviceSSHFragment.addCharacterToBuffer(event.getUnicodeChar(event.getMetaState()));

                return super.onKeyDown(keyCode, event);

        }

    }

    private Fragment[] getAvailableFragments() {
        /*
        Restituisce un array di Fragment, contenente le varie pagine video
         */
        List<Fragment> result = new ArrayList<>();

        int count=0;

        if(wakeOnLanFragment != null) {
            result.add(wakeOnLanFragment);
            count++;
        }

        if (torrentViewerFragment != null) {
            result.add(torrentViewerFragment);
            count++;
        }

        if (fileViewerFragment != null) {
            result.add(fileViewerFragment);
            count++;
        }

        if (deviceInfoFragment != null) {
            result.add(deviceInfoFragment);
            homeFragment=count;
            count++;
        }

        for(int i=0; i<nOfAvailableCameras; i++) {
            result.add(cameraViewFragment[i]);
            count++;
        }

        return result.toArray(new Fragment[0]);

    }

    private String[] getAvailableFragmentTitles() {

        List<String> result = new ArrayList<String>();

        if(wakeOnLanFragment != null) {
            result.add("Wake-on-lan");
        }

        if (torrentViewerFragment != null) {
            result.add("Torrent manager");
        }

        if (fileViewerFragment != null) {
            result.add("File manager");
        }

        if (deviceInfoFragment != null) {
            result.add("Remote device info");

        }

        for(int i=0; i<nOfAvailableCameras; i++) {
            result.add(String.format("Videosurveillance camera: %s", cameraIDs[i]));
        }

        return result.toArray(new String[0]);

    }

    private FragmentType[] getAvailableFragmentTypes() {

        List<FragmentType> result = new ArrayList<FragmentType>();

        if(wakeOnLanFragment != null) {
            result.add(FragmentType.WOL_MANAGER);
        }

        if (torrentViewerFragment != null) {
            result.add(FragmentType.TORRENT_MANAGER);
        }

        if (fileViewerFragment != null) {
            result.add(FragmentType.DIRECTORY_NAVIGATOR);
        }

        if (deviceInfoFragment != null) {
            result.add(FragmentType.DEVICE_INFO);

        }

        for(int i=0; i<nOfAvailableCameras; i++) {
            result.add(FragmentType.CAMERA_VIEWER);
        }

        return result.toArray(new FragmentType[0]);
    }



}
